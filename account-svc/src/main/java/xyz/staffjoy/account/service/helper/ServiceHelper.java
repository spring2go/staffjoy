package xyz.staffjoy.account.service.helper;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import com.google.common.collect.Maps;
import io.intercom.api.Avatar;
import io.intercom.api.CustomAttribute;
import io.intercom.api.Event;
import io.intercom.api.User;
import io.sentry.SentryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xyz.staffjoy.account.config.AppConfig;
import xyz.staffjoy.account.model.Account;
import xyz.staffjoy.account.repo.AccountRepo;
import xyz.staffjoy.bot.client.BotClient;
import xyz.staffjoy.bot.dto.GreetingRequest;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.common.error.ServiceException;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class ServiceHelper {
    static final ILogger logger = SLoggerFactory.getLogger(ServiceHelper.class);

    private final CompanyClient companyClient;

    private final AccountRepo accountRepo;

    private final SentryClient sentryClient;

    private final BotClient botClient;

    private final EnvConfig envConfig;

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void syncUserAsync(String userId) {
        if (envConfig.isDebug()) {
            logger.debug("intercom disabled in dev & test environment");
            return;
        }

        Account account = accountRepo.findAccountById(userId);
        if (account == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, String.format("User with id %s not found", userId));
        }
        if (StringUtils.isEmpty(account.getPhoneNumber()) && StringUtils.isEmpty(account.getEmail())) {
            logger.info(String.format("skipping sync for user %s because no email or phonenumber", account.getId()));
            return;
        }

        // use a map to de-dupe
        Map<String, CompanyDto> memberships = new HashMap<>();

        GetWorkerOfResponse workerOfResponse = null;
        try {
            workerOfResponse = companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, userId);
        } catch(Exception ex) {
            String errMsg = "could not fetch workOfList";
            handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
        if (!workerOfResponse.isSuccess()) {
            handleError(logger, workerOfResponse.getMessage());
            throw new ServiceException(workerOfResponse.getMessage());
        }
        WorkerOfList workerOfList = workerOfResponse.getWorkerOfList();

        boolean isWorker = workerOfList.getTeams().size() > 0;
        for(TeamDto teamDto : workerOfList.getTeams()) {
            GenericCompanyResponse genericCompanyResponse = null;
            try {
                genericCompanyResponse = companyClient.getCompany(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, teamDto.getCompanyId());
            } catch (Exception ex) {
                String errMsg = "could not fetch companyDto from teamDto";
                handleException(logger, ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }

            if (!genericCompanyResponse.isSuccess()) {
                handleError(logger, genericCompanyResponse.getMessage());
                throw new ServiceException(genericCompanyResponse.getMessage());
            }

            CompanyDto companyDto = genericCompanyResponse.getCompany();

            memberships.put(companyDto.getId(), companyDto);
        }

        GetAdminOfResponse getAdminOfResponse = null;
        try {
            getAdminOfResponse = companyClient.getAdminOf(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, userId);
        } catch (Exception ex) {
            String errMsg = "could not fetch adminOfList";
            handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
        if (!getAdminOfResponse.isSuccess()) {
            handleError(logger, getAdminOfResponse.getMessage());
            throw new ServiceException(getAdminOfResponse.getMessage());
        }
        AdminOfList adminOfList = getAdminOfResponse.getAdminOfList();

        boolean isAdmin = adminOfList.getCompanies().size() > 0;
        for(CompanyDto companyDto : adminOfList.getCompanies()) {
            memberships.put(companyDto.getId(), companyDto);
        }

        User user = new User();
        user.setUserId(account.getId());
        user.setEmail(account.getEmail());
        user.setName(account.getName());
        user.setSignedUpAt(account.getMemberSince().toEpochMilli());
        user.setAvatar(new Avatar().setImageURL(account.getPhotoUrl()));
        user.setLastRequestAt(Instant.now().toEpochMilli());

        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("v2", true));
        user.addCustomAttribute(CustomAttribute.newStringAttribute("phonenumber", account.getPhoneNumber()));
        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("confirmed_and_active", account.isConfirmedAndActive()));
        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("is_worker", isWorker));
        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("is_admin", isAdmin));
        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("is_staffjoy_support", account.isSupport()));

        for(CompanyDto companyDto : memberships.values()) {
            user.addCompany(new io.intercom.api.Company().setCompanyID(companyDto.getId()).setName(companyDto.getName()));
        }

        this.syncUserWithIntercom(user, account.getId());
    }

    void syncUserWithIntercom(User user, String userId) {
        try {
            Map<String, String> params = Maps.newHashMap();
            params.put("user_id", userId);

            User existing = User.find(params);

            if (existing != null) {
                User.update(user);
            } else {
                User.create(user);
            }

            logger.debug("updated intercom");
        } catch (Exception ex) {
            String errMsg = "fail to create/update user on Intercom";
            handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void trackEventAsync(String userId, String eventName) {
        if (envConfig.isDebug()) {
            logger.debug("intercom disabled in dev & test environment");
            return;
        }

        Event event = new Event()
                .setUserID(userId)
                .setEventName("v2_" + eventName)
                .setCreatedAt(Instant.now().toEpochMilli());

        try {
            Event.create(event);
        } catch (Exception ex) {
            String errMsg = "fail to create event on Intercom";
            handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        logger.debug("updated intercom");
    }

    public void sendSmsGreeting(String userId) {
        BaseResponse baseResponse = null;
        try {
            GreetingRequest greetingRequest = GreetingRequest.builder().userId(userId).build();
            baseResponse = botClient.sendSmsGreeting(greetingRequest);
        } catch (Exception ex) {
            String errMsg = "could not send welcome sms";
            handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
        if (!baseResponse.isSuccess()) {
            handleError(logger, baseResponse.getMessage());
            throw new ServiceException(baseResponse.getMessage());
        }
    }

    // for time diff < 2s, treat them as almost same
    public boolean isAlmostSameInstant(Instant dt1, Instant dt2) {
        long diff = dt1.toEpochMilli() - dt2.toEpochMilli();
        diff = Math.abs(diff);
        if (diff < TimeUnit.SECONDS.toMillis(1)) {
            return true;
        }
        return false;
    }

    public void handleError(ILogger log, String errMsg) {
        log.error(errMsg);
        if (!envConfig.isDebug()) {
            sentryClient.sendMessage(errMsg);
        }
    }

    public void handleException(ILogger log, Exception ex, String errMsg) {
        log.error(errMsg, ex);
        if (!envConfig.isDebug()) {
            sentryClient.sendException(ex);
        }
    }
}

package xyz.staffjoy.company.service.helper;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.TrackEventRequest;
import xyz.staffjoy.bot.client.BotClient;
import xyz.staffjoy.bot.dto.*;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.common.error.ServiceException;
import xyz.staffjoy.company.config.AppConfig;
import xyz.staffjoy.company.dto.ShiftDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Component
public class ServiceHelper {

    static final ILogger logger = SLoggerFactory.getLogger(ServiceHelper.class);

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private BotClient botClient;

    @Autowired
    private SentryClient sentryClient;

    @Autowired
    private EnvConfig envConfig;

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void trackEventAsync(String event) {

        String userId = AuthContext.getUserId();
        if (StringUtils.isEmpty(userId)) {
            // Not an action performed by a normal user
            // (noop - not an view)
            return;
        }

        TrackEventRequest trackEventRequest = TrackEventRequest.builder()
                .userId(userId).event(event).build();

        BaseResponse resp = null;
        try {
            resp = accountClient.trackEvent(trackEventRequest);
        } catch (Exception ex) {
            String errMsg = "fail to trackEvent through accountClient";
            handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (!resp.isSuccess()) {
            handleErrorAndThrowException(logger, resp.getMessage());
        }
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void onboardWorkerAsync(OnboardWorkerRequest onboardWorkerRequest) {
        BaseResponse baseResponse = null;
        try {
            baseResponse = botClient.onboardWorker(onboardWorkerRequest);
        } catch (Exception ex) {
            String errMsg = "fail to call onboardWorker through botClient";
            handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (!baseResponse.isSuccess()) {
            handleErrorAndThrowException(logger, baseResponse.getMessage());
        }
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void alertNewShiftAsync(AlertNewShiftRequest alertNewShiftRequest) {
        BaseResponse baseResponse = null;
        try {
            baseResponse = botClient.alertNewShift(alertNewShiftRequest);
        } catch (Exception ex) {
            String errMsg = "failed to alert worker about new shift";
            handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (!baseResponse.isSuccess()) {
            handleErrorAndThrowException(logger, baseResponse.getMessage());
        }
    }


    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void alertRemovedShiftAsync(AlertRemovedShiftRequest alertRemovedShiftRequest) {
        BaseResponse baseResponse = null;
        try {
            baseResponse = botClient.alertRemovedShift(alertRemovedShiftRequest);
        } catch (Exception ex) {
            String errMsg = "failed to alert worker about removed shift";
            handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (!baseResponse.isSuccess()) {
            handleErrorAndThrowException(logger, baseResponse.getMessage());
        }
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void buildShiftNotificationAsync(Map<String, List<ShiftDto>> notifs, boolean published) {
        for(Map.Entry<String, List<ShiftDto>> entry : notifs.entrySet()) {
            String userId = entry.getKey();
            List<ShiftDto> shiftDtos = entry.getValue();
            if (published) {
                // alert published
                AlertNewShiftsRequest alertNewShiftsRequest = AlertNewShiftsRequest.builder()
                        .userId(userId)
                        .newShifts(shiftDtos)
                        .build();
                BaseResponse baseResponse = null;
                try {
                    baseResponse = botClient.alertNewShifts(alertNewShiftsRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about new shifts";
                    handleErrorAndThrowException(logger, ex, errMsg);
                }
                if (!baseResponse.isSuccess()) {
                    handleErrorAndThrowException(logger, baseResponse.getMessage());
                }
            } else {
                // alert removed
                AlertRemovedShiftsRequest alertRemovedShiftsRequest = AlertRemovedShiftsRequest.builder()
                        .userId(userId)
                        .oldShifts(shiftDtos)
                        .build();
                BaseResponse baseResponse = null;
                try {
                    baseResponse = botClient.alertRemovedShifts(alertRemovedShiftsRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about removed shifts";
                    handleErrorAndThrowException(logger, ex, errMsg);
                }
                if (!baseResponse.isSuccess()) {
                    handleErrorAndThrowException(logger, baseResponse.getMessage());
                }
            }
        }
    }

    // send bot notifications
    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void updateShiftNotificationAsync(ShiftDto origShiftDto, ShiftDto shiftDtoToUpdte) {
        if (!origShiftDto.isPublished() && shiftDtoToUpdte.isPublished()) {
            if (shiftDtoToUpdte.getStart().isAfter(Instant.now()) &&
                    !StringUtils.isEmpty(shiftDtoToUpdte.getUserId())) {
                // looks like a new shift
                AlertNewShiftRequest alertNewShiftRequest = AlertNewShiftRequest.builder()
                        .userId(shiftDtoToUpdte.getUserId())
                        .newShift(shiftDtoToUpdte)
                        .build();
                BaseResponse baseResponse = null;
                try {
                    baseResponse = botClient.alertNewShift(alertNewShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about new shift";
                    handleErrorAndThrowException(logger, ex, errMsg);
                }
                if (!baseResponse.isSuccess()) {
                    handleErrorAndThrowException(logger, baseResponse.getMessage());
                }
            }
            return;
        }

        if (origShiftDto.isPublished() && !shiftDtoToUpdte.isPublished()) {
            if (shiftDtoToUpdte.getStart().isAfter(Instant.now()) &&
                    !StringUtils.isEmpty(origShiftDto.getUserId())) {
                // removed a shift
                AlertRemovedShiftRequest alertRemovedShiftRequest = AlertRemovedShiftRequest.builder()
                        .userId(origShiftDto.getUserId())
                        .oldShift(origShiftDto)
                        .build();
                BaseResponse baseResponse = null;
                try {
                    baseResponse = botClient.alertRemovedShift(alertRemovedShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about removed shift";
                    handleErrorAndThrowException(logger, ex, errMsg);
                }
                if (!baseResponse.isSuccess()) {
                    handleErrorAndThrowException(logger, baseResponse.getMessage());
                }
            }
            return;
        }

        if (!origShiftDto.isPublished() && !shiftDtoToUpdte.isPublished()) {
            // NOOP - basically return
            return;
        }

        if ((!StringUtils.isEmpty(origShiftDto.getUserId())) && origShiftDto.getUserId().equals(shiftDtoToUpdte.getUserId())) {
            if (shiftDtoToUpdte.getStart().isAfter(Instant.now())) {
                AlertChangedShiftRequest alertChangedShiftRequest = AlertChangedShiftRequest.builder()
                        .userId(origShiftDto.getUserId())
                        .oldShift(origShiftDto)
                        .newShift(shiftDtoToUpdte)
                        .build();
                BaseResponse baseResponse = null;
                try {
                    baseResponse = botClient.alertChangedShift(alertChangedShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about changed shift";
                    handleErrorAndThrowException(logger, ex, errMsg);
                }
                if (!baseResponse.isSuccess()) {
                    handleErrorAndThrowException(logger, baseResponse.getMessage());
                }
            }
            return;
        }


        if (!origShiftDto.getUserId().equals(shiftDtoToUpdte.getUserId())) {
            if (!StringUtils.isEmpty(origShiftDto.getUserId()) && origShiftDto.getStart().isAfter(Instant.now())) {
                AlertRemovedShiftRequest alertRemovedShiftRequest = AlertRemovedShiftRequest.builder()
                        .userId(origShiftDto.getUserId())
                        .oldShift(origShiftDto)
                        .build();
                BaseResponse baseResponse = null;
                try {
                    baseResponse = botClient.alertRemovedShift(alertRemovedShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about removed shift";
                    handleErrorAndThrowException(logger, ex, errMsg);
                }
                if (!baseResponse.isSuccess()) {
                    handleErrorAndThrowException(logger, baseResponse.getMessage());
                }
            }

            if (!StringUtils.isEmpty(shiftDtoToUpdte.getUserId()) && shiftDtoToUpdte.getStart().isAfter(Instant.now())) {
                AlertNewShiftRequest alertNewShiftRequest = AlertNewShiftRequest.builder()
                        .userId(shiftDtoToUpdte.getUserId())
                        .newShift(shiftDtoToUpdte)
                        .build();
                BaseResponse baseResponse = null;
                try {
                    baseResponse = botClient.alertNewShift(alertNewShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about new shift";
                    handleErrorAndThrowException(logger, ex, errMsg);
                }
                if (!baseResponse.isSuccess()) {
                    handleErrorAndThrowException(logger, baseResponse.getMessage());
                }
            }

            return;
        }

        logger.error(String.format("unable to determine updated shift messaging - orig %s new %s", origShiftDto, shiftDtoToUpdte));
    }

    public void handleErrorAndThrowException(ILogger log, String errMsg) {
        log.error(errMsg);
        if (!envConfig.isDebug()) {
            sentryClient.sendMessage(errMsg);
        }
        throw new ServiceException(errMsg);
    }

    public void handleErrorAndThrowException(ILogger log, Exception ex, String errMsg) {
        log.error(errMsg, ex);
        if (!envConfig.isDebug()) {
            sentryClient.sendException(ex);
        }
        throw new ServiceException(errMsg, ex);
    }
}

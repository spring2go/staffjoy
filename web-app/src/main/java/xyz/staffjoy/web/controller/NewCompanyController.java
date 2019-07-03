package xyz.staffjoy.web.controller;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.account.dto.GenericAccountResponse;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.common.env.EnvConstant;
import xyz.staffjoy.common.error.ServiceException;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.web.service.HelperService;
import xyz.staffjoy.web.view.Constant;
import xyz.staffjoy.web.view.PageFactory;

@SuppressWarnings("Duplicates")
@Controller
public class NewCompanyController {

    static final ILogger logger = SLoggerFactory.getLogger(LoginController.class);

    static final String DEFAULT_TIMEZONE = "UTC";
    static final String DEFAULT_DAYWEEK_STARTS = "Monday";
    static final String DEFAULT_TEAM_NAME = "Team";
    static final String DEFAULT_TEAM_COLOR = "#744fc6";

    @Autowired
    private PageFactory pageFactory;

    @Autowired
    private EnvConfig envConfig;

    @Autowired
    private HelperService helperService;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private CompanyClient companyClient;

    @RequestMapping(value = "/new_company")
    public String newCompany(@RequestParam(value="name", required = false) String name,
                             @RequestParam(value="timezone", required = false) String timezone,
                             @RequestParam(value="team", required = false) String teamName,
                             Model model) {
        if (StringUtils.isEmpty(AuthContext.getAuthz()) || AuthConstant.AUTHORIZATION_ANONYMOUS_WEB.equals(AuthContext.getAuthz())) {
            return "redirect:/login";
        }

        if(StringUtils.hasText(name)) {
            if (!StringUtils.hasText(timezone)) {
                timezone = DEFAULT_TIMEZONE;
            }
            if (!StringUtils.hasText(teamName)) {
                teamName = DEFAULT_TEAM_NAME;
            }

            // fetch current userId
            String currentUserId = AuthContext.getUserId();
            if (currentUserId == null) {
                throw new ServiceException("current userId not found in auth context");
            }

            AccountDto currentUser = null;
            GenericAccountResponse genericAccountResponse = null;
            try {
                genericAccountResponse = accountClient.getAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, currentUserId);
            } catch(Exception ex) {
                String errMsg = "fail to get user account";
                helperService.logException(logger, ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }
            if (!genericAccountResponse.isSuccess()) {
                helperService.logError(logger, genericAccountResponse.getMessage());
                throw new ServiceException(genericAccountResponse.getMessage());
            } else {
                currentUser = genericAccountResponse.getAccount();
            }

            // Make the company
            GenericCompanyResponse genericCompanyResponse = null;
            try {
                CompanyDto companyDtoToCreate = CompanyDto.builder()
                        .name(name)
                        .defaultTimezone(timezone)
                        .defaultDayWeekStarts(DEFAULT_DAYWEEK_STARTS)
                        .build();
                genericCompanyResponse = companyClient.createCompany(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyDtoToCreate);
            } catch(Exception ex) {
                String errMsg = "fail to create company";
                helperService.logException(logger, ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }
            if (!genericCompanyResponse.isSuccess()) {
                helperService.logError(logger, genericCompanyResponse.getMessage());
                throw new ServiceException(genericCompanyResponse.getMessage());
            }

            CompanyDto companyDto = genericCompanyResponse.getCompany();

            // register current user in directory
            GenericDirectoryResponse genericDirectoryResponse1 = null;
            try {
                NewDirectoryEntry newDirectoryEntry = NewDirectoryEntry.builder()
                        .companyId(companyDto.getId())
                        .email(currentUser.getEmail())
                        .build();
                genericDirectoryResponse1 = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
            } catch(Exception ex) {
                String errMsg = "fail to create directory";
                helperService.logException(logger, ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }
            if (!genericDirectoryResponse1.isSuccess()) {
                helperService.logError(logger, genericDirectoryResponse1.getMessage());
                throw new ServiceException(genericDirectoryResponse1.getMessage());
            }

            // create admin
            GenericDirectoryResponse genericDirectoryResponse2 = null;
            try {
                DirectoryEntryRequest directoryEntryRequest = DirectoryEntryRequest.builder()
                        .companyId(companyDto.getId())
                        .userId(currentUserId)
                        .build();
                genericDirectoryResponse2 = companyClient.createAdmin(AuthConstant.AUTHORIZATION_WWW_SERVICE, directoryEntryRequest);
            } catch(Exception ex) {
                String errMsg = "fail to create admin";
                helperService.logException(logger, ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }
            if (!genericDirectoryResponse2.isSuccess()) {
                helperService.logError(logger, genericDirectoryResponse2.getMessage());
                throw new ServiceException(genericDirectoryResponse2.getMessage());
            }

            // create team
            GenericTeamResponse teamResponse = null;
            try {
                CreateTeamRequest createTeamRequest = CreateTeamRequest.builder()
                        .companyId(companyDto.getId())
                        .name(teamName)
                        .color(DEFAULT_TEAM_COLOR)
                        .build();
                teamResponse = companyClient.createTeam(AuthConstant.AUTHORIZATION_WWW_SERVICE, createTeamRequest);
            } catch(Exception ex) {
                String errMsg = "fail to create team";
                helperService.logException(logger, ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }
            if (!teamResponse.isSuccess()) {
                helperService.logError(logger, teamResponse.getMessage());
                throw new ServiceException(teamResponse.getMessage());
            }
            TeamDto teamDto = teamResponse.getTeam();

            // register as worker
            GenericDirectoryResponse directoryResponse = null;
            try {
                WorkerDto workerDto = WorkerDto.builder()
                        .companyId(companyDto.getId())
                        .teamId(teamDto.getId())
                        .userId(currentUserId)
                        .build();
                directoryResponse = companyClient.createWorker(AuthConstant.AUTHORIZATION_WWW_SERVICE, workerDto);
            } catch(Exception ex) {
                String errMsg = "fail to create worker";
                helperService.logException(logger, ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }

            if (!directoryResponse.isSuccess()) {
                helperService.logError(logger, directoryResponse.getMessage());
                throw new ServiceException(directoryResponse.getMessage());
            }

            // redirect
            logger.info(String.format("new company signup - %s", companyDto));
            String url = HelperService.buildUrl("http", "app." + envConfig.getExternalApex());

            helperService.syncUserAsync(currentUserId);
            helperService.trackEventAsync(currentUserId, "freetrial_created");

            if (EnvConstant.ENV_PROD.equals(envConfig.getName()) && !currentUser.isSupport()) {
                // Alert sales of a new account signup
                helperService.sendEmailAsync(currentUser, companyDto);
            }

            return "redirect:" + url;
        }

        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildNewCompanyPage());
        return Constant.VIEW_NEW_COMPANY;
    }

}

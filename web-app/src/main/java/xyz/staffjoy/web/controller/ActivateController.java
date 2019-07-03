package xyz.staffjoy.web.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.account.dto.GenericAccountResponse;
import xyz.staffjoy.account.dto.UpdatePasswordRequest;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.Sessions;
import xyz.staffjoy.common.crypto.Sign;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.common.error.ServiceException;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.AdminOfList;
import xyz.staffjoy.company.dto.GetAdminOfResponse;
import xyz.staffjoy.company.dto.GetWorkerOfResponse;
import xyz.staffjoy.company.dto.WorkerOfList;
import xyz.staffjoy.web.props.AppProps;
import xyz.staffjoy.web.service.HelperService;
import xyz.staffjoy.web.view.ActivatePage;
import xyz.staffjoy.web.view.Constant;
import xyz.staffjoy.web.view.PageFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("Duplicates")
@Controller
public class ActivateController {
    static final ILogger logger = SLoggerFactory.getLogger(ActivateController.class);

    @Autowired
    private PageFactory pageFactory;

    @Autowired
    private AppProps appProps;

    @Autowired
    private EnvConfig envConfig;

    @Autowired
    private HelperService helperService;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private CompanyClient companyClient;

    @RequestMapping(value = "/activate/{token}")
    public String activate(@PathVariable String token,
                           @RequestParam(value="password", required = false) String password,
                           @RequestParam(value="name", required = false) String name,
                           @RequestParam(value="tos", required = false) String tos,
                           @RequestParam(value="phonenumber", required = false) String phonenumber,
                           Model model,
                           HttpServletRequest request,
                           HttpServletResponse response) {

        ActivatePage page = pageFactory.buildActivatePage();
        page.setToken(token);

        String email = null;
        String userId = null;
        try {
            DecodedJWT jwt = Sign.verifyEmailConfirmationToken(token, appProps.getSigningSecret());
            email = jwt.getClaim(Sign.CLAIM_EMAIL).asString();
            userId = jwt.getClaim(Sign.CLAIM_USER_ID).asString();
        } catch (Exception ex) {
            String errMsg = "Failed to verify email confirmation token";
            helperService.logException(logger, ex, errMsg);
            return "redirect:" + ResetController.PASSWORD_RESET_PATH;
        }

        GenericAccountResponse genericAccountResponse1 = null;
        try {
            genericAccountResponse1 = accountClient.getAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId);
        } catch (Exception ex) {
            String errMsg = "fail to get user account";
            helperService.logException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
        if (!genericAccountResponse1.isSuccess()) {
            helperService.logError(logger, genericAccountResponse1.getMessage());
            throw new ServiceException(genericAccountResponse1.getMessage());
        }
        AccountDto account = genericAccountResponse1.getAccount();

        page.setEmail(email);
        page.setName(account.getName());
        page.setPhonenumber(account.getPhoneNumber());

        if (!HelperService.isPost(request)) {
            model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, page);
            return Constant.VIEW_ACTIVATE;
        }

        // POST
        // update form in case we fail
        page.setName(name);
        page.setPhonenumber(phonenumber);

        if (password.length() < 6) {
            page.setErrorMessage("Your password must be at least 6 characters long");
        }

        if (StringUtils.isEmpty(tos)) {
            page.setErrorMessage("You must agree to the terms and conditions by selecting the checkbox.");
        }

        if (page.getErrorMessage() != null) {
            model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, page);
            return Constant.VIEW_ACTIVATE;
        }

        account.setEmail(email);
        account.setConfirmedAndActive(true);
        account.setName(name);
        account.setPhoneNumber(phonenumber);

        GenericAccountResponse genericAccountResponse2 = null;
        try {
            genericAccountResponse2 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, account);
        } catch (Exception ex) {
            String errMsg = "fail to update user account";
            helperService.logException(logger, ex, errMsg);
            page.setErrorMessage(errMsg);
            model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, page);
            return Constant.VIEW_ACTIVATE;
        }
        if (!genericAccountResponse2.isSuccess()) {
            helperService.logError(logger, genericAccountResponse2.getMessage());
            page.setErrorMessage(genericAccountResponse2.getMessage());
            model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, page);
            return Constant.VIEW_ACTIVATE;
        }

        // Update password
        BaseResponse baseResponse = null;
        try {
            UpdatePasswordRequest updatePasswordRequest = UpdatePasswordRequest.builder()
                    .userId(userId)
                    .password(password)
                    .build();
            baseResponse = accountClient.updatePassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        } catch (Exception ex) {
            String errMsg = "fail to update password";
            helperService.logException(logger, ex, errMsg);
            page.setErrorMessage(errMsg);
            model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, page);
            return Constant.VIEW_ACTIVATE;
        }
        if (!baseResponse.isSuccess()) {
            helperService.logError(logger, baseResponse.getMessage());
            page.setErrorMessage(baseResponse.getMessage());
            model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, page);
            return Constant.VIEW_ACTIVATE;
        }

        // login user
        Sessions.loginUser(account.getId(),
                account.isSupport(),
                false,
                appProps.getSigningSecret(),
                envConfig.getExternalApex(),
                response);
        logger.info("user activated account and logged in", "user_id", account.getId());


        // Smart redirection - for onboarding purposes
        GetWorkerOfResponse workerOfResponse = null;
        try {
            workerOfResponse = companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, account.getId());
        } catch (Exception ex) {
            String errMsg = "fail to get worker of list";
            helperService.logException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
        if (!workerOfResponse.isSuccess()) {
            helperService.logError(logger, workerOfResponse.getMessage());
            throw new ServiceException(workerOfResponse.getMessage());
        }
        WorkerOfList workerOfList = workerOfResponse.getWorkerOfList();

        GetAdminOfResponse getAdminOfResponse = null;
        try {
            getAdminOfResponse = companyClient.getAdminOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, account.getId());
        } catch (Exception ex) {
            String errMsg = "fail to get admin of list";
            helperService.logException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
        if (!getAdminOfResponse.isSuccess()) {
            helperService.logError(logger, getAdminOfResponse.getMessage());
            throw new ServiceException(getAdminOfResponse.getMessage());
        }
        AdminOfList adminOfList = getAdminOfResponse.getAdminOfList();

        String destination = null;
        if (adminOfList.getCompanies().size() != 0 || account.isSupport()) {
            destination = helperService.buildUrl("http", "app." + envConfig.getExternalApex());
        } else if (workerOfList.getTeams().size() != 0) {
            destination = helperService.buildUrl("http", "myaccount." + envConfig.getExternalApex());
        } else {
            // onboard
            destination = helperService.buildUrl("http", "www." + envConfig.getExternalApex(), "/new_company/");
        }

        return "redirect:" + destination;
    }
}

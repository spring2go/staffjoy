package xyz.staffjoy.web.controller;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.staffjoy.common.config.StaffjoyProps;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.web.view.Constant;
import xyz.staffjoy.web.view.error.ErrorPage;
import xyz.staffjoy.web.view.error.ErrorPageFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
@SuppressWarnings(value = "Duplicates")
public class GlobalErrorController implements ErrorController {

    static final ILogger logger = SLoggerFactory.getLogger(GlobalErrorController.class);

    @Autowired
    ErrorPageFactory errorPageFactory;
    @Autowired
    SentryClient sentryClient;
    @Autowired
    StaffjoyProps staffjoyProps;
    @Autowired
    EnvConfig envConfig;

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        ErrorPage errorPage = null;
        if (statusCode != null && (Integer)statusCode == HttpStatus.NOT_FOUND.value()) {
            errorPage = errorPageFactory.buildNotFoundPage();
        } else {
            errorPage = errorPageFactory.buildInternalServerErrorPage();
        }

        if (exception != null) {
            if (envConfig.isDebug()) {  // no sentry aop in debug mode
                logger.error("Global error handling", exception);
            } else {
                sentryClient.sendException((Exception)exception);
                UUID uuid = sentryClient.getContext().getLastEventId();
                errorPage.setSentryErrorId(uuid.toString());
                errorPage.setSentryPublicDsn(staffjoyProps.getSentryDsn());
                logger.warn("Reported error to sentry", "id", uuid.toString(), "error", exception);
            }
        }

        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, errorPage);

        return "error";
    }

}

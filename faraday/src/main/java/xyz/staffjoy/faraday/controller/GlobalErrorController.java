package xyz.staffjoy.faraday.controller;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.ResourceAccessException;
import xyz.staffjoy.common.config.StaffjoyProps;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.faraday.exceptions.ForbiddenException;
import xyz.staffjoy.faraday.view.ErrorPage;
import xyz.staffjoy.faraday.view.ErrorPageFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.net.SocketTimeoutException;
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

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        ErrorPage errorPage = null;
        if (exception instanceof ForbiddenException) {
            errorPage = errorPageFactory.buildForbiddenErrorPage();
        } else if (exception instanceof ResourceAccessException) {
            ResourceAccessException resourceAccessException =
                    (ResourceAccessException)exception;
            if (resourceAccessException.contains(SocketTimeoutException.class)) {
                errorPage = errorPageFactory.buildTimeoutErrorPage();
            }
        }

        if (errorPage == null) {
            errorPage = errorPageFactory.buildInternalServerErrorPage();
        }

        if (exception != null) {
            if (envConfig.isDebug()) {  // no sentry in debug mode
                logger.error("Global error handling", exception);
            } else {
                sentryClient.sendException((Exception)exception);
                UUID uuid = sentryClient.getContext().getLastEventId();
                errorPage.setSentryErrorId(uuid.toString());
                errorPage.setSentryPublicDsn(staffjoyProps.getSentryDsn());
                logger.warn("Reported error to sentry", "id", uuid.toString(), "error", exception);
            }
        }

        model.addAttribute("page", errorPage);

        return "error";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}

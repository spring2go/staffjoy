package xyz.staffjoy.faraday.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorPageFactory {

    @Autowired
    AssetLoader assetLoader;

    public ErrorPage buildTimeoutErrorPage() {
        return ErrorPage.builder()
                .title("Timeout Error")
                .explanation("Sorry, our servers seem to be slow. Please try again in a moment.")
                .headerCode(HttpStatus.GATEWAY_TIMEOUT.value())
                .linkText("Click here to check out our system status page")
                .linkHref("https://status.staffjoy.xyz")
                .imageBase64(assetLoader.getImageBase64())
                .build();
    }

    public ErrorPage buildForbiddenErrorPage() {
        return ErrorPage.builder()
                .title("Access Forbidden")
                .explanation("Sorry, it looks like you do not have permission to access this page.")
                .headerCode(HttpStatus.FORBIDDEN.value())
                .linkText("Contact our support team for help")
                .linkHref("mailto:help@staffjoy.xyz")
                .imageBase64(assetLoader.getImageBase64())
                .build();
    }

    public ErrorPage buildInternalServerErrorPage() {
        return ErrorPage.builder()
                .title("Internal Server Error")
                .explanation("Oops! Something broke. We're paging our engineers to look at it immediately.")
                .headerCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .linkText("Click here to check out our system status page")
                .linkHref("https://status.staffjoy.xyz")
                .imageBase64(assetLoader.getImageBase64())
                .build();
    }
}

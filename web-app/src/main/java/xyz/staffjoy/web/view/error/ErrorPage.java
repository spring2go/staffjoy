package xyz.staffjoy.web.view.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorPage {
    private String title; // Used in <title> and <h1>
    private String explanation; // Tell the user what's wrong
    private int headerCode; // http status code
    private String linkText; // Where do you want user to go?
    private String linkHref; // what's the link?
    private String sentryErrorId; // What do we track the view as on the backend?
    private String sentryPublicDsn; // Config for app
    private String imageBase64;
}

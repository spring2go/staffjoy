package xyz.staffjoy.web.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginPage extends Page {
    private boolean denied;
    private String previousEmail;
    private String returnTo;

    // lombok inheritance workaround, details here: https://www.baeldung.com/lombok-builder-inheritance
    @Builder(builderMethodName = "childBuilder")
    public LoginPage(String title, String description, String templateName, String cssId, String version, boolean denied, String previousEmail, String returnTo) {
        super(title, description, templateName, cssId, version);
        this.denied = denied;
        this.previousEmail = previousEmail;
        this.returnTo = returnTo;
    }
}

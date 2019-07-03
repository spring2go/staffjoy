package xyz.staffjoy.web.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmResetPage extends Page {
    private String errorMessage;
    private String token;

    // lombok inheritance workaround, details here: https://www.baeldung.com/lombok-builder-inheritance
    @Builder(builderMethodName = "childBuilder")
    public ConfirmResetPage(String title,
                            String description,
                            String templateName,
                            String cssId,
                            String version,
                            String errorMessage,
                            String token) {
        super(title, description, templateName, cssId, version);
        this.errorMessage = errorMessage;
        this.token = token;
    }
}

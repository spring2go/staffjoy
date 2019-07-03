package xyz.staffjoy.web.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivatePage extends Page {

    private String errorMessage;
    private String email;
    private String name;
    private String phonenumber;
    private String token;

    // lombok inheritance workaround, details here: https://www.baeldung.com/lombok-builder-inheritance
    @Builder(builderMethodName = "childBuilder")
    public ActivatePage(String title,
                        String description,
                        String templateName,
                        String cssId,
                        String version,
                        String errorMessage,
                        String email,
                        String name,
                        String phonenumber,
                        String token) {
        super(title, description, templateName, cssId, version);
        this.errorMessage = errorMessage;
        this.email = email;
        this.name = name;
        this.phonenumber = phonenumber;
        this.token = token;
    }
}

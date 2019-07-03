package xyz.staffjoy.web.view;

import lombok.*;

@Getter
@Setter
@Builder
public class Page {
    private String title;
    private String description;
    private String templateName;
    private String cssId;
    @Builder.Default
    private String version = "3.0";
}

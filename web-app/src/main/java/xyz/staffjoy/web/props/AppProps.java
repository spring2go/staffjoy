package xyz.staffjoy.web.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix="staffjoy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProps {
    @NotNull
    private String recaptchaPublic;
    @NotNull
    private String signingSecret;
}

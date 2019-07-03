package xyz.staffjoy.faraday.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix="staffjoy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffjoyPropreties {
    @NotNull
    private String signingSecret;
}

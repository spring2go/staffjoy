package xyz.staffjoy.sms.props;

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

    // aliyun directmail props
    @NotNull private String aliyunAccessKey;
    @NotNull private String aliyunAccessSecret;
    @NotNull private String aliyunSmsSignName;

    private boolean whiteListOnly;
    private String whiteListPhoneNumbers;
    private int concurrency;

}

package xyz.staffjoy.sms.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import xyz.staffjoy.common.config.StaffjoyRestConfig;
import xyz.staffjoy.sms.SmsConstant;
import xyz.staffjoy.sms.props.AppProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Import(value = StaffjoyRestConfig.class)
public class AppConfig {

    public static final String ASYNC_EXECUTOR_NAME = "asyncExecutor";

    private static ILogger logger = SLoggerFactory.getLogger(AppConfig.class);

    @Autowired
    AppProps appProps;

    @Bean
    public IAcsClient acsClient(@Autowired SentryClient sentryClient) {
        IClientProfile profile = DefaultProfile.getProfile(SmsConstant.ALIYUN_REGION_ID, appProps.getAliyunAccessKey(), appProps.getAliyunAccessSecret());
        try {
            DefaultProfile.addEndpoint(SmsConstant.ALIYUN_SMS_ENDPOINT_NAME, SmsConstant.ALIYUN_REGION_ID, SmsConstant.ALIYUN_SMS_PRODUCT, SmsConstant.ALIYUN_SMS_DOMAIN);
        } catch (ClientException ex) {
            sentryClient.sendException(ex);
            logger.error("Fail to create acsClient ", ex);
        }
        IAcsClient client = new DefaultAcsClient(profile);
        return client;
    }

    @Bean(name=ASYNC_EXECUTOR_NAME)
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(appProps.getConcurrency());
        executor.setMaxPoolSize(appProps.getConcurrency());
        executor.setQueueCapacity(SmsConstant.DEFAULT_EXECUTOR_QUEUE_CAPACITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
}

package xyz.staffjoy.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import xyz.staffjoy.common.aop.SentryClientAspect;

/**
 * Use this common config for Web App
 */
@Configuration
@Import(value = {StaffjoyConfig.class, SentryClientAspect.class,})
public class StaffjoyWebConfig {
}

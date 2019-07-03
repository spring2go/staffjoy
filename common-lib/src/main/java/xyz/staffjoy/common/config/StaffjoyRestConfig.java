package xyz.staffjoy.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import xyz.staffjoy.common.error.GlobalExceptionTranslator;
import xyz.staffjoy.common.aop.*;

/**
 * Use this common config for Rest API
 */
@Configuration
@Import(value = {StaffjoyConfig.class, SentryClientAspect.class, GlobalExceptionTranslator.class})
public class StaffjoyRestConfig  {
}

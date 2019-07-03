package xyz.staffjoy.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.staffjoy.common.env.EnvConfig;

@Aspect
@Slf4j
public class SentryClientAspect {

    @Autowired
    EnvConfig envConfig;

    @Around("execution(* io.sentry.SentryClient.send*(..))")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        // no sentry logging in debug mode
        if (envConfig.isDebug()) {
            log.debug("no sentry logging in debug mode");
            return;
        }
        joinPoint.proceed();
    }
}

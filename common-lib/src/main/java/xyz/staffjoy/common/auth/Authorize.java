package xyz.staffjoy.common.auth;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorize {
    // allowed consumers
    String[] value();
}

package xyz.staffjoy.faraday.core.interceptor;

import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.http.ResponseData;

public interface PostForwardResponseInterceptor {
    void intercept(ResponseData data, MappingProperties mapping);
}

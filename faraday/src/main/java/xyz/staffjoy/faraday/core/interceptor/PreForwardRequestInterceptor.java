package xyz.staffjoy.faraday.core.interceptor;

import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.http.RequestData;

public interface PreForwardRequestInterceptor {
    void intercept(RequestData data, MappingProperties mapping);
}

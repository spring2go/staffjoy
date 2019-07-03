package xyz.staffjoy.faraday.core.interceptor;

import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.http.ResponseData;

public class NoOpPostForwardResponseInterceptor implements PostForwardResponseInterceptor {
    @Override
    public void intercept(ResponseData data, MappingProperties mapping) {

    }
}

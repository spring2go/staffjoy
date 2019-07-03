package xyz.staffjoy.faraday.core.trace;

import org.springframework.http.HttpHeaders;

public abstract class HttpEntity {

    protected HttpHeaders headers;

    public HttpHeaders getHeaders() { return headers; }

    protected void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }
}

package xyz.staffjoy.faraday.core.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

import static xyz.staffjoy.faraday.core.utils.BodyConverter.convertBodyToString;

public class UnmodifiableRequestData {

    protected HttpMethod method;
    protected String uri;
    protected String host;
    protected HttpHeaders headers;
    protected byte[] body;
    protected HttpServletRequest originRequest;

    public UnmodifiableRequestData(RequestData requestData) {
        this(
                requestData.getMethod(),
                requestData.getHost(),
                requestData.getUri(),
                requestData.getHeaders(),
                requestData.getBody(),
                requestData.getOriginRequest()
        );
    }

    public UnmodifiableRequestData(HttpMethod method,
                                   String host,
                                   String uri,
                                   HttpHeaders headers,
                                   byte[] body,
                                   HttpServletRequest request
    ) {
        this.method = method;
        this.host = host;
        this.uri = uri;
        this.headers = headers;
        this.body = body;
        this.originRequest = request;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getHost() { return host; }

    public String getUri() {
        return uri;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public HttpServletRequest getOriginRequest() { return this.originRequest; }

    public String getBodyAsString() {
        return convertBodyToString(body);
    }

}

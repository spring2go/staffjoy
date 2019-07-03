package xyz.staffjoy.faraday.core.trace;

import org.springframework.http.HttpMethod;

public class IncomingRequest extends HttpEntity {

    protected HttpMethod method;
    protected String uri;
    protected String host;

    public HttpMethod getMethod() { return method; }

    protected void setMethod(HttpMethod method) { this.method = method;}

    public String getUri() { return uri; }

    protected void setUri(String uri) { this.uri = uri; }

    public String getHost() { return host; }

    protected  void setHost(String host) { this.host = host; }
}

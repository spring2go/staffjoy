package xyz.staffjoy.faraday.core.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static xyz.staffjoy.faraday.core.utils.BodyConverter.convertBodyToString;
import static xyz.staffjoy.faraday.core.utils.BodyConverter.convertStringToBody;

public class ResponseData {
    protected HttpStatus status;
    protected HttpHeaders headers;
    protected byte[] body;
    protected UnmodifiableRequestData requestData;

    public ResponseData(HttpStatus status, HttpHeaders headers, byte[] body, UnmodifiableRequestData requestData) {
        this.status = status;
        this.headers = new HttpHeaders();
        this.headers.putAll(headers);
        this.body = body;
        this.requestData = requestData;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getBodyAsString() {
        return convertBodyToString(body);
    }

    public void setBody(String body) {
        this.body = convertStringToBody(body);
    }

    public UnmodifiableRequestData getRequestData() {
        return requestData;
    }
}

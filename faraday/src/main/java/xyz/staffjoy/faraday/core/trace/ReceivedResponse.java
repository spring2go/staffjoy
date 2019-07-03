package xyz.staffjoy.faraday.core.trace;

import org.springframework.http.HttpStatus;

import static xyz.staffjoy.faraday.core.utils.BodyConverter.convertBodyToString;

public class ReceivedResponse extends HttpEntity {

    protected HttpStatus status;
    protected byte[] body;

    public HttpStatus getStatus() { return status; }
    protected void setStatus(HttpStatus status) { this.status = status; }

    public String getBodyAsString() { return convertBodyToString(body); }

    public byte[] getBody() { return body; }

    protected void setBody(byte[] body) { this.body = body; }

}

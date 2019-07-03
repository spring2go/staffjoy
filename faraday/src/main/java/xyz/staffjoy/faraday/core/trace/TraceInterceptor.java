package xyz.staffjoy.faraday.core.trace;

public interface TraceInterceptor {

    void onRequestReceived(String traceId, IncomingRequest request);

    void onNoMappingFound(String traceId, IncomingRequest request);

    void onForwardStart(String traceId, ForwardRequest request);

    void onForwardError(String traceId, Throwable error);

    void onForwardComplete(String traceId, ReceivedResponse response);
}

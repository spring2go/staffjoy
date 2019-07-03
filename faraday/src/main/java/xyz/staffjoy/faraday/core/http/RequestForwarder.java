package xyz.staffjoy.faraday.core.http;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import xyz.staffjoy.faraday.config.FaradayProperties;
import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.balancer.LoadBalancer;
import xyz.staffjoy.faraday.core.interceptor.PostForwardResponseInterceptor;
import xyz.staffjoy.faraday.core.mappings.MappingsProvider;
import xyz.staffjoy.faraday.core.trace.ProxyingTraceInterceptor;
import xyz.staffjoy.faraday.exceptions.FaradayException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static java.lang.System.nanoTime;
import static java.time.Duration.ofNanos;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.ResponseEntity.status;

public class RequestForwarder {

    private static final ILogger log = SLoggerFactory.getLogger(RequestForwarder.class);

    protected final ServerProperties serverProperties;
    protected final FaradayProperties faradayProperties;
    protected final HttpClientProvider httpClientProvider;
    protected final MappingsProvider mappingsProvider;
    protected final LoadBalancer loadBalancer;
    protected final Optional<MeterRegistry> meterRegistry;
    protected final ProxyingTraceInterceptor traceInterceptor;
    protected final PostForwardResponseInterceptor postForwardResponseInterceptor;

    public RequestForwarder(
            ServerProperties serverProperties,
            FaradayProperties faradayProperties,
            HttpClientProvider httpClientProvider,
            MappingsProvider mappingsProvider,
            LoadBalancer loadBalancer,
            Optional<MeterRegistry> meterRegistry,
            ProxyingTraceInterceptor traceInterceptor,
            PostForwardResponseInterceptor postForwardResponseInterceptor
    ) {
        this.serverProperties = serverProperties;
        this.faradayProperties = faradayProperties;
        this.httpClientProvider = httpClientProvider;
        this.mappingsProvider = mappingsProvider;
        this.loadBalancer = loadBalancer;
        this.meterRegistry = meterRegistry;
        this.traceInterceptor = traceInterceptor;
        this.postForwardResponseInterceptor = postForwardResponseInterceptor;
    }

    public ResponseEntity<byte[]> forwardHttpRequest(RequestData data, String traceId, MappingProperties mapping) {
        ForwardDestination destination = resolveForwardDestination(data.getUri(), mapping);
        prepareForwardedRequestHeaders(data, destination);
        traceInterceptor.onForwardStart(traceId, destination.getMappingName(),
                data.getMethod(), data.getHost(), destination.getUri().toString(),
                data.getBody(), data.getHeaders());
        RequestEntity<byte[]> request = new RequestEntity<>(data.getBody(), data.getHeaders(), data.getMethod(), destination.getUri());
        ResponseData response = sendRequest(traceId, request, mapping, destination.getMappingMetricsName(), data);

        log.debug(String.format("Forwarded: %s %s %s -> %s %d", data.getMethod(), data.getHost(), data.getUri(), destination.getUri(), response.getStatus().value()));

        traceInterceptor.onForwardComplete(traceId, response.getStatus(), response.getBody(), response.getHeaders());
        postForwardResponseInterceptor.intercept(response, mapping);
        prepareForwardedResponseHeaders(response);

        return status(response.getStatus())
                .headers(response.getHeaders())
                .body(response.getBody());

    }

    /**
     * Remove any protocol-level headers from the remote server's response that
     * do not apply to the new response we are sending.
     *
     * @param response
     */
    protected void prepareForwardedResponseHeaders(ResponseData response) {
        HttpHeaders headers = response.getHeaders();
        headers.remove(TRANSFER_ENCODING);
        headers.remove(CONNECTION);
        headers.remove("Public-Key-Pins");
        headers.remove(SERVER);
        headers.remove("Strict-Transport-Security");
    }

    /**
     * Remove any protocol-level headers from the clients request that
     * do not apply to the new request we are sending to the remote server.
     *
     * @param request
     * @param destination
     */
    protected void prepareForwardedRequestHeaders(RequestData request, ForwardDestination destination) {
        HttpHeaders headers = request.getHeaders();
        //headers.set(HOST, destination.getUri().getAuthority());
        headers.remove(TE);
    }

    protected ForwardDestination resolveForwardDestination(String originUri, MappingProperties mapping) {
        return new ForwardDestination(createDestinationUrl(originUri, mapping), mapping.getName(), resolveMetricsName(mapping));
    }

    protected URI createDestinationUrl(String uri, MappingProperties mapping) {
        String host = loadBalancer.chooseDestination(mapping.getDestinations());
        try {
            return new URI(host + uri);
        } catch(URISyntaxException e) {
            throw new FaradayException("Error creating destination URL from HTTP request URI: " + uri + " using mapping " + mapping, e);
        }
    }

    protected ResponseData sendRequest(String traceId, RequestEntity<byte[]> request, MappingProperties mapping, String mappingMetricsName, RequestData requestData ) {
        ResponseEntity<byte[]> response;
        long startingTime = nanoTime();
        try {
            response = httpClientProvider.getHttpClient(mapping.getName()).exchange(request, byte[].class);
            recordLatency(mappingMetricsName, startingTime);
        } catch (HttpStatusCodeException e) {
            recordLatency(mappingMetricsName, startingTime);
            response = status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            recordLatency(mappingMetricsName, startingTime);
            traceInterceptor.onForwardFailed(traceId, e);
            throw e;
        }
        UnmodifiableRequestData data = new UnmodifiableRequestData(requestData);
        return new ResponseData(response.getStatusCode(), response.getHeaders(), response.getBody(), data);
    }

    protected void recordLatency(String metricName, long startingTime) {
        meterRegistry.ifPresent(meterRegistry -> meterRegistry.timer(metricName).record(ofNanos(nanoTime() - startingTime)));
    }

    protected String resolveMetricsName(MappingProperties mapping) {
        return faradayProperties.getMetrics().getNamesPrefix() + "." + mapping.getName();
    }
}

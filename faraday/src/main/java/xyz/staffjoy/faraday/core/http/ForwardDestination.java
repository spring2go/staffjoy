package xyz.staffjoy.faraday.core.http;

import java.net.URI;

public class ForwardDestination {

    protected final URI uri;
    protected final String mappingName;
    protected final String mappingMetricsName;

    public ForwardDestination(URI uri, String mappingName, String mappingMetricsName) {
        this.uri = uri;
        this.mappingName = mappingName;
        this.mappingMetricsName = mappingMetricsName;
    }

    public URI getUri() { return uri; }

    public String getMappingName() { return mappingName; }

    public String getMappingMetricsName() { return mappingMetricsName; }
}

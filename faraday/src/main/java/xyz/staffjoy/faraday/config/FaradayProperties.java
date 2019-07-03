package xyz.staffjoy.faraday.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * Faraday configuration properties
 */
@ConfigurationProperties("faraday")
public class FaradayProperties {
    /**
     * Faraday servlet filter order.
     */
    private int filterOrder = HIGHEST_PRECEDENCE + 100;
    /**
     * Enable programmatic mapping or not,
     * false only in dev environment, in dev we use mapping via configuration file
     */
    private boolean enableProgrammaticMapping = true;
    /**
     * Properties responsible for collecting metrics during HTTP requests forwarding.
     */
    @NestedConfigurationProperty
    private MetricsProperties metrics = new MetricsProperties();
    /**
     * Properties responsible for tracing HTTP requests proxying processes.
     */
    @NestedConfigurationProperty
    private TracingProperties tracing = new TracingProperties();
    /**
     * List of proxy mappings.
     */
    @NestedConfigurationProperty
    private List<MappingProperties> mappings = new ArrayList<>();

    public int getFilterOrder() {
        return filterOrder;
    }

    public void setFilterOrder(int filterOrder) {
        this.filterOrder = filterOrder;
    }

    public MetricsProperties getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricsProperties metrics) {
        this.metrics = metrics;
    }

    public TracingProperties getTracing() {
        return tracing;
    }

    public void setTracing(TracingProperties tracing) {
        this.tracing = tracing;
    }

    public List<MappingProperties> getMappings() {
        return mappings;
    }

    public void setMappings(List<MappingProperties> mappings) {
        this.mappings = mappings;
    }

    public boolean isEnableProgrammaticMapping() {
        return this.enableProgrammaticMapping;
    }

    public void setEnableProgrammaticMapping(boolean enableProgrammaticMapping) {
        this.enableProgrammaticMapping = enableProgrammaticMapping;
    }
}

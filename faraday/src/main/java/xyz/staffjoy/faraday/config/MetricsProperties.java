package xyz.staffjoy.faraday.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsProperties {
    /**
     * Global metrics names prefix.
     */
    private String namesPrefix = "faraday";
}

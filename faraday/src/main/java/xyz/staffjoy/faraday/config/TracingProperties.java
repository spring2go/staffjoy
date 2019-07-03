package xyz.staffjoy.faraday.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TracingProperties {
    /**
     * Flag for enabling and disabling tracing HTTP requests proxying processes.
     */
    private boolean enabled;
}

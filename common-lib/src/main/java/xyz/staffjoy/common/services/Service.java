package xyz.staffjoy.common.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Service is an app on Staffjoy that runs on a subdomain
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    private int security; // Public, Authenticated, or Admin
    private boolean restrictDev; // If true, service is suppressed in stage and prod
    private String backendDomain;  // Backend service to query
    private boolean noCacheHtml; // If true, injects a header for HTML responses telling the browser not to cache HTML
}

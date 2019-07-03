package xyz.staffjoy.faraday.core.filter;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.staffjoy.common.env.EnvConfig;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SecurityFilter extends OncePerRequestFilter {
    private static final ILogger log = SLoggerFactory.getLogger(SecurityFilter.class);

    private final EnvConfig envConfig;

    public SecurityFilter(EnvConfig envConfig) {
        this.envConfig = envConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // TODO - Determine how to force SSL. Depends on frontend load balancer config.
        String origin = request.getHeader("Origin");
        if (!isEmpty(origin)) {
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, PUT, DELETE");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Content-Type, Content-Length, Cookie, Accept-Encoding, X-CSRF-Token, Authorization");
        }

        // Stop here if its Preflighted OPTIONS request
        if ("OPTIONS".equals(request.getMethod())) {
            return;
        }

        if (!envConfig.isDebug()) {
            // Check if secure
            boolean isSecure = request.isSecure();
            if (!isSecure) {
                // Check if frontend proxy proxied it
                if ("https".equals(request.getHeader("X-Forwarded-Proto"))) {
                    isSecure = true;
                }
            }

            // If not secure, then redirect
            if (!isSecure) {
                log.info("Insecure quest in uat&prod environment, redirect to https");
                try {
                    URI redirectUrl = new URI("https",
                            request.getServerName(),
                            request.getRequestURI(), null);
                    response.sendRedirect(redirectUrl.toString());
                } catch (URISyntaxException e) {
                    log.error("fail to build redirect url", e);
                }
                return;
            }

            // HSTS - force SSL
            response.setHeader("Strict-Transport-Security", "max-age=315360000; includeSubDomains; preload");
            // No iFrames
            response.setHeader("X-Frame-Options", "DENY");
            // Cross-site scripting protection
            response.setHeader("X-XSS-Protection", "1; mode=block");
        }

        filterChain.doFilter(request, response);
    }
}

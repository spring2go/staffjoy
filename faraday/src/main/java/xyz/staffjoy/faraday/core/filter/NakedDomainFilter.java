package xyz.staffjoy.faraday.core.filter;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.staffjoy.common.env.EnvConfig;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class NakedDomainFilter extends OncePerRequestFilter {

    private static final ILogger log = SLoggerFactory.getLogger(NakedDomainFilter.class);

    private final EnvConfig envConfig;
    private static final String DEFAULT_SERVICE = "www";

    public NakedDomainFilter(EnvConfig envConfig) {
        this.envConfig = envConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // if you're hitting naked domain - go to www
        // e.g. staffjoy.xyz/foo?true=1 should redirect to www.staffjoy.xyz/foo?true=1
        if (envConfig.getExternalApex().equals(request.getServerName())) {
            // It's hitting naked domain - redirect to www
            log.info("hitting naked domain - redirect to www");
            String scheme = "http";
            if (!envConfig.isDebug()) {
                scheme = "https";
            }
            try {
                URI redirectUrl = new URI(scheme,
                        null,
                        DEFAULT_SERVICE + "." + envConfig.getExternalApex(),
                        request.getServerPort(),
                        "/login/", null, null);
                response.sendRedirect(redirectUrl.toString());
            } catch (URISyntaxException e) {
                log.error("fail to build redirect url", e);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

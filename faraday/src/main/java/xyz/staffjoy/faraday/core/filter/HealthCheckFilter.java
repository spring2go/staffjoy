package xyz.staffjoy.faraday.core.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HealthCheckFilter extends OncePerRequestFilter {
    // HEALTH_CHECK_PATH is the standard healthcheck path in our app
    static final String HEALTH_CHECK_PATH = "/health";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (HEALTH_CHECK_PATH.equals(request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("OK");
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

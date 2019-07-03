package xyz.staffjoy.faraday.core.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FaviconFilter extends OncePerRequestFilter {

    static final String FAVICON_PATH = "/favicon.ico";

    private final byte[] faviconFile;

    public FaviconFilter(byte[] faviconFile) {
        this.faviconFile = faviconFile;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (FAVICON_PATH.equals(request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().write(faviconFile);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

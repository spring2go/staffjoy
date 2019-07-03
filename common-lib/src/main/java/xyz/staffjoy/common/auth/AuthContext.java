package xyz.staffjoy.common.auth;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * A context holder class for holding the current userId and authz info
 *
 * @author bobo
 */
public class AuthContext {

    private static String getRequetHeader(String headerName) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            String value = request.getHeader(headerName);
            return value;
        }
        return null;
    }

    public static String getUserId() {
        return getRequetHeader(AuthConstant.CURRENT_USER_HEADER);
    }

    public static String getAuthz() {
        return getRequetHeader(AuthConstant.AUTHORIZATION_HEADER);
    }

}

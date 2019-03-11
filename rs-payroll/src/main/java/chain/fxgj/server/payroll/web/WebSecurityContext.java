package chain.fxgj.server.payroll.web;

import io.netty.util.concurrent.FastThreadLocal;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;

/**
 * @author chain
 * create by chain on 2018/8/20 下午10:34
 **/

public class WebSecurityContext implements SecurityContext {

    private static FastThreadLocal<WebSecurityContext> currentWebSecurityContext;
    private final UserPrincipal principal;
    private final UriInfo uriInfo;

    public WebSecurityContext(UriInfo uriInfo, UserPrincipal principal) {
        this.principal = principal;
        this.uriInfo = uriInfo;
    }

    public static void clearCurrentWebSecurityContext() {
        if (currentWebSecurityContext != null) {
            currentWebSecurityContext.remove();
        }
    }

    public static WebSecurityContext getCurrentWebSecurityContext() {
        if (currentWebSecurityContext == null) {
            throw new NullPointerException("currentWebSecurityContext is null");
        }
        return currentWebSecurityContext.get();
    }

    public static void setCurrentWebSecurityContext(WebSecurityContext webSecurityContext) {
        if (currentWebSecurityContext == null) {
            currentWebSecurityContext = new FastThreadLocal<>();
        }
        currentWebSecurityContext.set(webSecurityContext);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    public UserPrincipal getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        String[] var2 = this.principal.getRoles();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String uRole = var2[var4];
            if (uRole.equals(role)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isSecure() {
        return "https".equals(this.uriInfo.getRequestUri().getScheme());
    }

    @Override
    public String getAuthenticationScheme() {
        return "DIGEST";
    }

    public String toString() {
        return "WebSecurityContext{principal=" + this.principal + ", uriInfo=" + this.uriInfo + '}';
    }
}

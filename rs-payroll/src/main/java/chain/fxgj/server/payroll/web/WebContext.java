package chain.fxgj.server.payroll.web;


import chain.fxgj.server.payroll.dto.base.HeaderDTO;
import chain.utils.commons.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * @author chain
 * create by chain on 2018/12/12 5:53 PM
 **/
public class WebContext {

    private static ThreadLocal<HeaderDTO> currentHeader = new ThreadLocal<>();

    private static ThreadLocal<UserPrincipal> currentLogUser = new ThreadLocal<>();

    public static void clearCurrentWebSecurityContext() {
        if (currentLogUser != null) {
            currentLogUser.remove();
        }
        if (currentHeader != null) {
            currentHeader.remove();
        }
    }

    public static UserPrincipal getCurrentUser() {
        return currentLogUser.get();
    }

    public static void setCurrentUser(UserPrincipal sysUser) {
        currentLogUser.set(sysUser);
    }

    public static HeaderDTO getCurrentHeader() {
        return currentHeader.get();
    }

    public static void setCurrentHeader(HeaderDTO header) {
        currentHeader.set(header);
    }


}

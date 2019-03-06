package chain.fxgj.server.payroll.web;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author chain
 * create by chain on 2018/8/20 下午10:30
 **/
public class UserPrincipal implements Principal {
    String sessionId;
    String name;
    String[] roles;
    Duration sessionTimeOut;

    String userName;

    LocalDateTime loginDateTime;

    LocalDateTime lastOptDateTime;
    boolean isAdmin;

    String orgId;

    public UserPrincipal() {
        this.loginDateTime = LocalDateTime.now();
    }

    public UserPrincipal(String useId, String userName, String sessionId, Duration sessionTimeOut, String[] roles, boolean isAdmin) {
        this.name = useId;
        this.userName = userName;
        this.sessionId = sessionId;
        this.sessionTimeOut = sessionTimeOut;
        this.roles = roles;
        this.loginDateTime = LocalDateTime.now();
        this.lastOptDateTime = LocalDateTime.now();
        this.isAdmin = isAdmin;
    }

    public Duration getSessionTimeOut() {
        return sessionTimeOut;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String[] getRoles() {
        return roles;
    }


    @Override
    public String getName() {
        return name;
    }


    public String getUserName() {
        return userName;
    }


    @Override
    public String toString() {
        return "UserPrincipal{" +
                "sessionId='" + sessionId + '\'' +
                ", name='" + name + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", sessionTimeOut=" + sessionTimeOut +
                ", userName='" + userName + '\'' +
                ", loginDateTime=" + loginDateTime +
                ", lastOptDateTime=" + lastOptDateTime +
                ", isAdmin=" + isAdmin +
                ", orgId='" + orgId + '\'' +
                '}';
    }

    public LocalDateTime getLoginDateTime() {
        return loginDateTime;
    }

    public void setLoginDateTime(LocalDateTime loginDateTime) {
        this.loginDateTime = loginDateTime;
    }

    public LocalDateTime getLastOptDateTime() {
        return lastOptDateTime;
    }

    public void setLastOptDateTime(LocalDateTime lastOptDateTime) {
        this.lastOptDateTime = lastOptDateTime;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}

package chain.fxgj.server.payroll.annotation.loginlog.service;

public interface LoginLogService {


    /**
     * 登录日志入库
     */
    void saveLoginLog(String openId);

}

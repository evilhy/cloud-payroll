package chain.fxgj.server.payroll.service;

public interface LoginLogService {


    /**
     * 登录日志入库
     */
    void saveLoginLog(String openId);

}

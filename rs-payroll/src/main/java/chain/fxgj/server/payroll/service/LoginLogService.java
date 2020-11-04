package chain.fxgj.server.payroll.service;

public interface LoginLogService {


    /**
     * 登录日志入库
     * @param openId openId
     * @param methodName 增加日志所在方法的方法名
     */
    void saveLoginLog(String openId, String methodName);

}

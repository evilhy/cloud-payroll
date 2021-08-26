package chain.fxgj.server.payroll.service;

import core.dto.wechat.EmployeeWechatDTO;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/23 18:47
 */
public interface EmployeeWechatService {

    /**
     * 获取登陆人信息
     *
     * @param jsessionId
     * @return
     */
    EmployeeWechatDTO findByJsessionId(String jsessionId);
}

package chain.fxgj.core.common.service;

import chain.fxgj.core.jpa.model.EmployeeWechatInfo;
import chain.fxgj.server.payroll.web.UserPrincipal;
import org.springframework.cache.annotation.CachePut;
import org.springframework.transaction.annotation.Transactional;

public interface MerchantService {

    /**
     * 保存工资条绑定信息
     *
     * @param employeeWechatInfo 员工微信信息
     * @return
     */
    @Transactional
    EmployeeWechatInfo saveMerchant(EmployeeWechatInfo employeeWechatInfo);


    /**
     * 工资条绑定信息
     *
     * @param employeeWechatInfo 员工微信信息
     * @return
     */
    EmployeeWechatInfo findMerchant(EmployeeWechatInfo employeeWechatInfo);

    /**
     * 用户登录信息
     *
     * @param jsessionId
     * @return
     */
    @CachePut(cacheNames = "wechat", key = "'jsession:'.concat(#jsessionId)")
    UserPrincipal setWechatInfo(String jsessionId, EmployeeWechatInfo employeeWechatInfo) throws Exception;


}
package chain.fxgj.server.payroll.service;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.jpa.model.CardbinInfo;
import chain.fxgj.core.jpa.model.EmployeeWechatInfo;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.EmployeeDTO;
import chain.fxgj.server.payroll.dto.request.UpdBankCardDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface EmpWechatService {
    /**
     * 获取微信绑定信息 WageUserPrincipal 接收
     *
     * @return
     */
    @Cacheable(cacheNames = "wechat", key = "'jsession:'.concat(#jsessionId)")
    WageUserPrincipal getWechatInfoDetail(String jsessionId);


}

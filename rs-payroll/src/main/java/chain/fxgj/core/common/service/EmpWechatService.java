package chain.fxgj.core.common.service;

import chain.fxgj.core.jpa.model.CardbinInfo;
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
     * 获取微信绑定信息
     *
     * @return
     */
    @Cacheable(cacheNames = "wechat", key = "'jsession:'.concat(#jsessionId)")
    UserPrincipal getWechatInfo(String jsessionId);

    /**
     * 用户登录信息
     *
     * @param jsessionId
     * @return
     */
    @CachePut(cacheNames = "wechat", key = "'jsession:'.concat(#jsessionId)")
    UserPrincipal setWechatInfo(String jsessionId, String openId, String nickname, String headimgurl, String idNumber) throws Exception;

    /**
     * 获取员工信息
     *
     * @param idNumber
     * @return
     */
    List<EmployeeDTO> getEmpList(String idNumber);

    /**
     * 验证银行卡
     * @param updBankCardDTO
     * @return
     */
    CardbinInfo checkCard(String idNumber, UpdBankCardDTO updBankCardDTO);

    String getWechatId(String openId);

}
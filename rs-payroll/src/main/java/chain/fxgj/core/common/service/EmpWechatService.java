package chain.fxgj.core.common.service;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.jpa.model.CardbinInfo;
import chain.fxgj.core.jpa.model.EmployeeWechatInfo;
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
     * 查询 用户绑定信息
     *
     * @param idNumber
     * @param appPartner
     * @return
     */
    EmployeeWechatInfo getEmployeeWechatInfo(String idNumber, AppPartnerEnum appPartner);

    /**
     * 查询 用户绑定信息
     *
     * @param openId
     * @param idNumber
     * @param appPartner
     * @return
     */
    EmployeeWechatInfo getEmployeeWechatInfo(String openId, String idNumber, AppPartnerEnum appPartner);

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
    UserPrincipal setWechatInfo(String jsessionId, String openId, String nickname, String headimgurl, String idNumber, AppPartnerEnum appPartner) throws Exception;

    /**
     * 获取员工信息
     *
     * @param idNumber
     * @return
     */
    List<EmployeeDTO> getEmpList(String idNumber);

    /**
     * 验证银行卡
     *
     * @param updBankCardDTO
     * @return
     */
    CardbinInfo checkCard(String idNumber, UpdBankCardDTO updBankCardDTO);

    String getWechatId(String openId);

}

package chain.fxgj.server.payroll.service;

import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.securities.request.ReqSecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesRedisDTO;
import org.springframework.cache.annotation.CachePut;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SecuritiesService {
    /**
     * 获取微信绑定信息 WageUserPrincipal 接收
     *
     * @return
     */
    @CachePut(cacheNames = "wechat", key = "'jsession:'.concat(#jsessionId)")
    WageUserPrincipal getWechatInfoDetail(String jsessionId, String openId, String phone);

    /**
     * 根据openId查询是证券活动否登录
     * @param openId
     */
    SecuritiesRedisDTO qrySecuritiesCustInfo(String openId);

    /**
     *
     * @param
     * @return
     */
    boolean securitiesLogin(String openId, String  nickname, String  headimgurl, ReqSecuritiesLoginDTO reqSecuritiesLoginDTO);


}

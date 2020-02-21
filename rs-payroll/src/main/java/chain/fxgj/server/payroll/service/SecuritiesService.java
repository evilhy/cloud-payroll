package chain.fxgj.server.payroll.service;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.feign.dto.response.WageDetailInfoDTO;
import chain.fxgj.feign.dto.response.WageRes100703;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesRedisDTO;
import chain.payroll.dto.response.PayrollRes100703DTO;
import chain.payroll.dto.response.PayrollWageDetailDTO;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SecuritiesService {
    /**
     * 获取微信绑定信息 WageUserPrincipal 接收
     *
     * @return
     */
    @Cacheable(cacheNames = "wechat", key = "'jsession:'.concat(#jsessionId)")
    WageUserPrincipal getWechatInfoDetail(String jsessionId, String openId, String nickName, String headImgurl);

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
    void securitiesLogin(String openId, String  nickname, String  headimgurl, SecuritiesLoginDTO securitiesLoginDTO);


}

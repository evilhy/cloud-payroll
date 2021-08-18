package chain.fxgj.server.payroll.service;

import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.fxgj.constant.DictEnums.AppPartnerEnum;
import core.dto.response.PayrollRes100703DTO;
import core.dto.response.PayrollWageDetailDTO;
import core.dto.sync.WageDetailInfoDTO;
import core.dto.wechat.CacheUserPrincipal;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface WechatRedisService {
    /**
     * 薪资列表 - mongo
     *
     * @param idNumber
     * @param groupId
     * @param year
     * @return
     */
    //@Cacheable(cacheNames = "wechat", key = "'wageListMongo:'.concat(#idNumber).concat(#groupId).concat(#year).concat(#type)")
    PayrollRes100703DTO wageListByMongo(String idNumber, String groupId, String year, String type);

    /**
     * 工资详情 - mongo
     *
     * @return
     */
    //@Cacheable(cacheNames = "wechat", key = "'wageDetailMongo:'.concat(#idNumber).concat(#groupId).concat(#wageSheetId)")
    List<PayrollWageDetailDTO> getWageDetailByMongo(String idNumber, String groupId, String wageSheetId);

    /**
     * 工资详情 - mysql
     *
     * @return
     */
    //@Cacheable(cacheNames = "wechat", key = "'wageDetailMysql:'.concat(#idNumber).concat(#groupId).concat(#wageSheetId)")
    List<WageDetailInfoDTO> getWageDetailByMysql(String idNumber, String groupId, String wageSheetId);

    /**
     * 根据code获取凭证
     * 缓存 有效期为 5分钟
     *
     * @param wechatGroupEnum
     * @param code
     * @return
     */
    //@Cacheable(value = "weixinOauth2", key = "#root.methodName+':'+#code")
    AccessTokenDTO oauth2AccessToken(WechatGroupEnum wechatGroupEnum, String code);

    /**
     * 根据网页授权openId、accessToken获取用户信息
     * access_token 有效期为  7200秒 ，2个小时，
     * 如果在 2个小时内，获取用户信息，从缓存中查取
     *
     * @param accessToken
     * @param openId
     * @return
     */
    @Cacheable(value = "weixinOauth2AccessTokenOpenid", key = "#root.methodName+':'+#openId")
    UserInfoDTO getUserInfo(String accessToken, String openId);

    /**
     * 工资条登录
     */
    @CachePut(cacheNames = "wechat", key = "'jsession:'.concat(#jsessionId)")
    CacheUserPrincipal registeWechatPayroll(String jsessionId, String openId, String nickName, String headImgurl, String idNumber, AppPartnerEnum appPartner) throws Exception;

    /**
     * 年会红包设置超时时间
     */
    @CachePut(cacheNames = "wechat", key = "'jsession:'.concat(#jsessionId)")
    CacheUserPrincipal setActivitySessionTimeOut(String jsessionId, String openId) throws Exception;

}

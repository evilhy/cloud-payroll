package chain.fxgj.server.payroll.service;

import chain.fxgj.server.payroll.dto.securities.request.ReqSecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesRedisDTO;
import chain.wisales.core.constant.dictEnum.UserTypeEnum;
import chain.wisales.core.dto.securities.*;
import chain.wisales.core.dto.securitiesIntegral.SecuritiesIntegralRewardDTO;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface SecuritiesService {

    /**
     * 根据openId查询是证券活动否登录
     *
     * @param jsessionId
     * @param openId
     */
    @CachePut(cacheNames = "securities", key = "'jsession:'.concat(#jsessionId)")
    SecuritiesRedisDTO qrySecuritiesCustInfo(String jsessionId, String openId);

    /**
     * 更新缓存
     *
     * @param jsessionId
     * @param securitiesRedisDTO
     */
    @CachePut(cacheNames = "securities", key = "'jsession:'.concat(#jsessionId)")
    SecuritiesRedisDTO upSecuritiesRedis(String jsessionId, SecuritiesRedisDTO securitiesRedisDTO);

    /**
     * 查询缓存信息
     *
     * @param jsessionId
     */
    @Cacheable(cacheNames = "securities", key = "'jsession:'.concat(#jsessionId)")
    SecuritiesRedisDTO qrySecuritiesRedis(String jsessionId);

    /**
     * 证券登录入库
     *
     * @param
     * @return
     */
    String securitiesLogin(SecuritiesRedisDTO securitiesRedisDTO);

    /**
     * 邀请奖励列表查询
     *
     * @param custIdOrManagerId
     * @return
     */
    List<SecuritiesInvitationAwardDTO> qryInvitationAward(String custIdOrManagerId);

    /**
     * 查询金豆个数
     *
     * @param custId
     */
    BigDecimal qryGoldenBean(String custId, UserTypeEnum userType);

    /**
     * 查询数据同步时间列表(每个公司的数据同步时间)
     */
    List<SecuritiesDataSynTimeDTO> qryDataSynTimeList();

    /**
     * 查询数据同步时间列表(每个公司的数据同步时间)
     */
    List<SecuritiesOpenRewardDTO> qryOpenRewardList(String custId);

    /**
     * 投资奖励列表List
     *
     * @param custId
     * @return
     */
    List<SecuritiesRewardResDTO> qryInvestmentRewardList(String custId);

    /**
     * 跳转第三方证券公司活动页面
     *
     * @param reqSecuritiesLoginDTO
     * @return
     */
    String transferThirdPage(ReqSecuritiesLoginDTO reqSecuritiesLoginDTO);

    /**
     * 证券开户活动我的邀请查询
     *
     * @param managerId 客户经理
     * @return list
     */
    List<SecuritiesIntegralRewardDTO> querySecuritiesInvitation(String managerId);

    /**
     * 证券工分邀请奖励列表查询
     *
     * @param managerId 客户经理ID
     * @return list
     */
    List<SecuritiesIntegralRewardDTO> queryInvitationIntegral(String managerId);
}

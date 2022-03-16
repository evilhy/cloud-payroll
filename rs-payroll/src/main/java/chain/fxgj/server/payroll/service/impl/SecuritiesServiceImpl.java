package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.server.payroll.dto.securities.request.ReqSecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesRedisDTO;
import chain.fxgj.server.payroll.service.SecuritiesService;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.wisales.client.feign.SecuritiesActivityFeignService;
import chain.wisales.core.constant.dictEnum.SecuritiesPlatformEnum;
import chain.wisales.core.constant.dictEnum.UserTypeEnum;
import chain.wisales.core.dto.securities.*;
import chain.wisales.core.dto.securitiesIntegral.SecuritiesIntegralRewardDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class SecuritiesServiceImpl implements SecuritiesService {

    @Autowired
    SecuritiesActivityFeignService securitiesActivityFeignService;

    @Override
    public SecuritiesRedisDTO qrySecuritiesCustInfo(String jsessionId, String openId) {
        log.info("qrySecuritiesCustInfo.jsessionId[{}], openId:[{}]", jsessionId, openId);

        SecuritiesCustInfoDTO securitiesCustInfoDTO = securitiesActivityFeignService.loginCheck(openId);

        SecuritiesRedisDTO securitiesRedisDTO = new SecuritiesRedisDTO();
        securitiesRedisDTO.setLoginStatus(securitiesCustInfoDTO.getLoginStatus()?1:0);
        securitiesRedisDTO.setLoginStatusVal(securitiesCustInfoDTO.getLoginStatus()?"已登录":"未登录");
        securitiesRedisDTO.setOpenId(openId);
        securitiesRedisDTO.setJsessionId(jsessionId);
        securitiesRedisDTO.setCustomerId("");
        securitiesRedisDTO.setInvitationId("");
        String phoneNo = securitiesCustInfoDTO.getPhoneNo();
        securitiesRedisDTO.setPhone(StringUtils.isEmpty(phoneNo)?"": phoneNo);

        String custId = securitiesCustInfoDTO.getCustId();
        securitiesRedisDTO.setCustId(StringUtils.isEmpty(custId)?"": custId);

        String userLogo = securitiesCustInfoDTO.getUserLogo();
        securitiesRedisDTO.setHeadimgurl(StringUtils.isEmpty(userLogo)?"": userLogo);

        String userNickName = securitiesCustInfoDTO.getUserNickName();
        securitiesRedisDTO.setNickname(StringUtils.isEmpty(userNickName)?"": userNickName);

        String custActivityParticId = securitiesCustInfoDTO.getCustActivityParticId();
        securitiesRedisDTO.setCustActivityParticId(StringUtils.isEmpty(custActivityParticId)?"": custActivityParticId);

        String wxUserId = securitiesCustInfoDTO.getWxUserId();
        securitiesRedisDTO.setWxUserId(StringUtils.isEmpty(wxUserId)?"": wxUserId);

        return securitiesRedisDTO;
    }

    @Override
    public SecuritiesRedisDTO upSecuritiesRedis(String jsessionId, SecuritiesRedisDTO securitiesRedisDTO) {
        log.info("securities更新缓存");
        return securitiesRedisDTO;
    }

    @Override
    public SecuritiesRedisDTO qrySecuritiesRedis(String jsessionId) {
        return null;
    }

    /**
     * 调用唯销数据入库
     * @param securitiesRedisDTO
     * @return
     */
    @Override
    public String securitiesLogin(SecuritiesRedisDTO securitiesRedisDTO) {
        log.info("securitiesLogin.securitiesRedisDTO:[{}]", JacksonUtil.objectToJson(securitiesRedisDTO));
        SecuritiesLoginDTO securitiesLoginDTO = new SecuritiesLoginDTO();
        securitiesLoginDTO.setOpenId(securitiesRedisDTO.getOpenId());
        securitiesLoginDTO.setPhoneNo(securitiesRedisDTO.getPhone());
        securitiesLoginDTO.setUserNickName(securitiesRedisDTO.getNickname());
        securitiesLoginDTO.setUserLogo(securitiesRedisDTO.getHeadimgurl());
        securitiesLoginDTO.setReferrerId(securitiesRedisDTO.getInvitationId());
        securitiesLoginDTO.setManagerId(securitiesRedisDTO.getCustomerId());
        securitiesLoginDTO.setSex(securitiesRedisDTO.getSex());
        securitiesLoginDTO.setCity(securitiesRedisDTO.getCity());
        securitiesLoginDTO.setCountry(securitiesRedisDTO.getCountry());

        log.info("securitiesLogin.securitiesLoginDTO:[{}]", JacksonUtil.objectToJson(securitiesLoginDTO));
        String custId = securitiesActivityFeignService.login(securitiesLoginDTO);
        return custId;
    }

    @Override
    public List<SecuritiesInvitationAwardDTO> qryInvitationAward(String custIdOrManagerId) {

        List<SecuritiesInvitationAwardDTO> securitiesInvitationAwardDTOList = securitiesActivityFeignService.qryInvitationAward(custIdOrManagerId);

        return securitiesInvitationAwardDTOList;
    }

    @Override
    public BigDecimal qryGoldenBean(String custId, UserTypeEnum userType) {
        BigDecimal goldenBean = securitiesActivityFeignService.qryGoldenBean(custId, userType);
        return goldenBean;
    }

    @Override
    public List<SecuritiesDataSynTimeDTO> qryDataSynTimeList() {
        List<SecuritiesDataSynTimeDTO> securitiesDataSynTimeDTOList = securitiesActivityFeignService.qryDataSynTimeList();
        return securitiesDataSynTimeDTOList;
    }

    @Override
    public List<SecuritiesOpenRewardDTO> qryOpenRewardList(String custId) {
        List<SecuritiesOpenRewardDTO> securitiesOpenRewardDTOList = securitiesActivityFeignService.qryOpenRewardList(custId);
        return securitiesOpenRewardDTOList;
    }

    @Override
    public List<SecuritiesRewardResDTO> qryInvestmentRewardList(String custId) {
        List<SecuritiesRewardResDTO> securitiesRewardResDTOList = securitiesActivityFeignService.qryInvestmentRewardList(custId);
        return securitiesRewardResDTOList;
    }

    /**
     * 跳转第三方证券公司活动页面
     *
     * @param reqSecuritiesLoginDTO
     * @return
     */
    @Override
    public String transferThirdPage(ReqSecuritiesLoginDTO reqSecuritiesLoginDTO) {
        log.info("SecuritiesServiceImpl.transferThirdPage reqSecuritiesLoginDTO:[{}]", JacksonUtil.objectToJson(reqSecuritiesLoginDTO));
        SecuritiesLoginDTO securitiesLoginDTO = new SecuritiesLoginDTO();
        securitiesLoginDTO.setManagerId(reqSecuritiesLoginDTO.getCustomerId());
        securitiesLoginDTO.setReferrerId(reqSecuritiesLoginDTO.getInvitationId());
        securitiesLoginDTO.setPhoneNo(reqSecuritiesLoginDTO.getPhone());
        securitiesLoginDTO.setCustId(reqSecuritiesLoginDTO.getCustId());
        securitiesLoginDTO.setSecuritiesPlatform(SecuritiesPlatformEnum.values()[reqSecuritiesLoginDTO.getSecuritiesPlatform()]);
        log.info("SecuritiesServiceImpl.transferThirdPage securitiesLoginDTO:[{}]", JacksonUtil.objectToJson(securitiesLoginDTO));
        String url = securitiesActivityFeignService.transferThirdPage(securitiesLoginDTO);
        return url;
    }

    /**
     * 证券开户活动我的邀请查询
     *
     * @param managerId 客户经理ID
     * @return list
     */
    @Override
    public List<SecuritiesIntegralRewardDTO> querySecuritiesInvitation(String managerId) {
        log.info("SecuritiesServiceImpl.querySecuritiesInvitation managerId:[{}]", JacksonUtil.objectToJson(managerId));
        List<SecuritiesIntegralRewardDTO> list = securitiesActivityFeignService.querySecuritiesInvitation(managerId);
        log.info("SecuritiesServiceImpl.querySecuritiesInvitation list:[{}]", JacksonUtil.objectToJson(list));
        return list;
    }

    /**
     * 证券工分邀请奖励列表查询
     *
     * @param managerId 客户经理ID
     * @return list
     */
    @Override
    public List<SecuritiesIntegralRewardDTO> queryInvitationIntegral(String managerId) {
        log.info("SecuritiesServiceImpl.querySecuritiesInvitation managerId:[{}]", JacksonUtil.objectToJson(managerId));
        List<SecuritiesIntegralRewardDTO> list = securitiesActivityFeignService.queryInvitationIntegral(managerId);
        log.info("SecuritiesServiceImpl.querySecuritiesInvitation list:[{}]", JacksonUtil.objectToJson(list));
        return list;
    }

}

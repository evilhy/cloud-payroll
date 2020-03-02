package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.securities.request.ReqSecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesRedisDTO;
import chain.fxgj.server.payroll.service.SecuritiesService;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.wisales.client.feign.SecuritiesActivityFeignService;
import chain.wisales.core.constant.dictEnum.SecuritiesPlatformEnum;
import chain.wisales.core.constant.dictEnum.StandardEnum;
import chain.wisales.core.constant.dictEnum.UserTypeEnum;
import chain.wisales.core.dto.securities.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    public List<SecuritiesRewardResDTO> qryInvestmentRewardList(SecuritiesRewardReqDTO securitiesRewardReqDTO) {
        List<SecuritiesRewardResDTO> securitiesRewardResDTOList = securitiesActivityFeignService.qryInvestmentRewardList(securitiesRewardReqDTO);
        return securitiesRewardResDTOList;
    }
}

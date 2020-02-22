package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.securities.request.ReqSecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesRedisDTO;
import chain.fxgj.server.payroll.service.SecuritiesService;
import chain.wisales.client.feign.SecuritiesActivityFeignService;
import chain.wisales.core.dto.securities.SecuritiesCustInfoDTO;
import chain.wisales.core.dto.securities.SecuritiesLoginDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        securitiesRedisDTO.setPhone(securitiesCustInfoDTO.getPhoneNo());
        securitiesRedisDTO.setCustId(securitiesRedisDTO.getCustId());
        securitiesRedisDTO.setHeadimgurl(securitiesRedisDTO.getHeadimgurl());
        securitiesRedisDTO.setNickname(securitiesRedisDTO.getNickname());
        securitiesRedisDTO.setOpenId(openId);
        securitiesRedisDTO.setJsessionId(jsessionId);
        securitiesRedisDTO.setCustActivityParticId(securitiesRedisDTO.getCustActivityParticId());
        securitiesRedisDTO.setCustomerId(securitiesRedisDTO.getCustomerId());
        securitiesRedisDTO.setInvitationId(securitiesRedisDTO.getInvitationId());
        securitiesRedisDTO.setWxUserId(securitiesRedisDTO.getWxUserId());
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

    @Override
    public String securitiesLogin(SecuritiesRedisDTO securitiesRedisDTO) {
        //todo 调用唯销数据入库

        SecuritiesLoginDTO securitiesLoginDTO = new SecuritiesLoginDTO();
        securitiesLoginDTO.setOpenId(securitiesRedisDTO.getOpenId());
        securitiesLoginDTO.setPhoneNo(securitiesRedisDTO.getPhone());
        securitiesLoginDTO.setUserNickName(securitiesRedisDTO.getNickname());
        securitiesLoginDTO.setUserLogo(securitiesRedisDTO.getHeadimgurl());
        securitiesLoginDTO.setReferrerId(securitiesRedisDTO.getInvitationId());

        String custId = securitiesActivityFeignService.login(securitiesLoginDTO);
        return custId;
    }







    @Override
    public WageUserPrincipal getWechatInfoDetail(String jsessionId, String openId, String phone) {

        WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
        wageUserPrincipal.setOpenId(openId);
        wageUserPrincipal.setPhone(phone);
//        wageUserPrincipal.setNickname(nickName);
//        wageUserPrincipal.setHeadimgurl(headImgurl);
        return wageUserPrincipal;
    }


}

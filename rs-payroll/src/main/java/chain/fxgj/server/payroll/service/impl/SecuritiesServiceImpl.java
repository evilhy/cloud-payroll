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
    public SecuritiesRedisDTO qrySecuritiesCustInfo(String openId) {

        SecuritiesCustInfoDTO securitiesCustInfoDTO = securitiesActivityFeignService.loginCheck(openId);
        SecuritiesRedisDTO securitiesRedisDTO = new SecuritiesRedisDTO();
        securitiesRedisDTO.setLoginStatus(securitiesCustInfoDTO.getLoginStatus()?IsStatusEnum.YES:IsStatusEnum.NO);
        securitiesRedisDTO.setPhone(securitiesCustInfoDTO.getPhoneNo());
        return securitiesRedisDTO;
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

    @Override
    public boolean securitiesLogin(String openId, String  nickname, String  headimgurl, ReqSecuritiesLoginDTO reqSecuritiesLoginDTO) {
        //todo 调用唯销数据入库

        SecuritiesLoginDTO securitiesLoginDTO = new SecuritiesLoginDTO();
        securitiesLoginDTO.setOpenId(openId);
        securitiesLoginDTO.setPhoneNo(reqSecuritiesLoginDTO.getPhone());
        Boolean login = securitiesActivityFeignService.login(securitiesLoginDTO);
        return login;
    }
}

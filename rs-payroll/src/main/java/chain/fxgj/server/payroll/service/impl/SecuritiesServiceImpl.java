package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesRedisDTO;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.SecuritiesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecuritiesServiceImpl implements SecuritiesService {

    @Override
    public SecuritiesRedisDTO qrySecuritiesCustInfo(String openId) {

        SecuritiesRedisDTO securitiesRedisDTO = new SecuritiesRedisDTO();
        //todo 通过openId 调唯销接口，验证是否登录
        securitiesRedisDTO.setLoginStatus(IsStatusEnum.YES);
        return securitiesRedisDTO;
    }

    @Override
    public WageUserPrincipal getWechatInfoDetail(String jsessionId, String openId, String nickName, String headImgurl) {
        WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
        wageUserPrincipal.setOpenId(openId);
        wageUserPrincipal.setNickname(nickName);
        wageUserPrincipal.setHeadimgurl(headImgurl);
        return wageUserPrincipal;
    }

    @Override
    public void securitiesLogin(String openId, String  nickname, String  headimgurl, SecuritiesLoginDTO securitiesLoginDTO) {

        //todo 调用唯销数据入库


    }
}

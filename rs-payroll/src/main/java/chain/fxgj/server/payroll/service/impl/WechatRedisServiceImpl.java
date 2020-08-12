package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.feign.client.WechatFeignService;
import chain.fxgj.feign.dto.response.WageDetailInfoDTO;
import chain.fxgj.feign.dto.response.WageRes100703;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.feign.dto.wechat.WageRegisteWechatPayrollDTO;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.pub.client.feign.WechatFeignClient;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import core.dto.response.PayrollRes100703DTO;
import core.dto.response.PayrollWageDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WechatRedisServiceImpl implements WechatRedisService {

    @Autowired
    WechatFeignClient wechatFeignClient;
    @Autowired
    private WechatFeignService wechatFeignService;

    @Override
    public PayrollRes100703DTO wageListByMongo(String idNumber, String groupId, String year, String type) {
        return null;
    }

    @Override
    public WageRes100703 wageListByMysql(String idNumber, String groupId, String year, String type) {
        return null;
    }

    @Override
    public List<PayrollWageDetailDTO> getWageDetailByMongo(String idNumber, String groupId, String wageSheetId) {
        List<PayrollWageDetailDTO> payrollWageDetailDTOS = new ArrayList<>();
        return payrollWageDetailDTOS;
    }

    @Override
    public List<WageDetailInfoDTO> getWageDetailByMysql(String idNumber, String groupId, String wageSheetId) {
        List<WageDetailInfoDTO> wageDetailInfoDTOS = new ArrayList<>();
        return wageDetailInfoDTOS;
    }

    @Override
    public AccessTokenDTO oauth2AccessToken(WechatGroupEnum wechatGroupEnum, String code) {
        AccessTokenDTO accessTokenDTO = wechatFeignClient.oauth2AccessToken(wechatGroupEnum, code);
        return accessTokenDTO;
    }

    @Override
    public UserInfoDTO getUserInfo(String accessToken, String openId) {
        UserInfoDTO userInfo = wechatFeignClient.getUserInfo(accessToken, openId);
        return userInfo;
    }

    @Override
    public WageUserPrincipal registeWechatPayroll(String jsessionId, String openId, String nickName, String headImg, String headImgurl, AppPartnerEnum appPartner) throws Exception {

        WageRegisteWechatPayrollDTO wageRegisteWechatPayrollDTO = new WageRegisteWechatPayrollDTO();
        wageRegisteWechatPayrollDTO.setJsessionId(jsessionId);
        wageRegisteWechatPayrollDTO.setOpenId(openId);
        wageRegisteWechatPayrollDTO.setNickName(nickName);
        wageRegisteWechatPayrollDTO.setHeadImgurl(headImgurl);
        wageRegisteWechatPayrollDTO.setAppPartnerEnum(appPartner);
        log.info("wageRegisteWechatPayrollDTO:[{}]", JacksonUtil.objectToJson(wageRegisteWechatPayrollDTO));
        WageUserPrincipal wageUserPrincipal = wechatFeignService.registeWechatPayroll(wageRegisteWechatPayrollDTO);
        log.info("wageUserPrincipal:[{}]", JacksonUtil.objectToJson(wageUserPrincipal));
        return wageUserPrincipal;
    }
}

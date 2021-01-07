package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.payroll.client.feign.WechatFeignController;
import chain.pub.client.feign.WechatFeignClient;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import core.dto.response.PayrollRes100703DTO;
import core.dto.response.PayrollWageDetailDTO;
import core.dto.sync.WageDetailInfoDTO;
import core.dto.wechat.CacheRegisteWechatPayrollDTO;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WechatRedisServiceImpl implements WechatRedisService {

    @Autowired
    WechatFeignClient wechatFeignClient;
    @Autowired
    WechatFeignController wechatFeignController;

    @Override
    public PayrollRes100703DTO wageListByMongo(String idNumber, String groupId, String year, String type) {
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
    public CacheUserPrincipal registeWechatPayroll(String jsessionId, String openId, String nickName, String headImg, String headImgurl, chain.utils.fxgj.constant.DictEnums.AppPartnerEnum appPartner) throws Exception {

        CacheRegisteWechatPayrollDTO cacheRegisteWechatPayrollDTO = new CacheRegisteWechatPayrollDTO();
        cacheRegisteWechatPayrollDTO.setJsessionId(jsessionId);
        cacheRegisteWechatPayrollDTO.setOpenId(openId);
        cacheRegisteWechatPayrollDTO.setNickName(nickName);
        cacheRegisteWechatPayrollDTO.setHeadImgurl(headImgurl);
        cacheRegisteWechatPayrollDTO.setAppPartnerEnum(appPartner);
        log.info("wageRegisteWechatPayrollDTO:[{}]", JacksonUtil.objectToJson(cacheRegisteWechatPayrollDTO));
        CacheUserPrincipal cacheUserPrincipal = wechatFeignController.registeWechatPayroll(cacheRegisteWechatPayrollDTO);
        log.info("wageUserPrincipal:[{}]", JacksonUtil.objectToJson(cacheUserPrincipal));
        return cacheUserPrincipal;
    }

    @Override
    public CacheUserPrincipal setActivitySessionTimeOut(String jsessionId, String openId) throws Exception {
        CacheUserPrincipal userPrincipal = CacheUserPrincipal.builder()
                .sessionId(jsessionId)
                .openId(openId)
                .sessionTimeOut(LocalDateTime.now().plusHours(8))
                .build();
        return userPrincipal;
    }
}

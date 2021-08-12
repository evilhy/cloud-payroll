package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.dto.wechat.WechatCallBackDTO;
import chain.fxgj.server.payroll.service.LoginLogService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.payroll.client.feign.InsideFeignController;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import chain.utils.fxgj.constant.DictEnums.AppPartnerEnum;
import chain.utils.fxgj.constant.DictEnums.IsStatusEnum;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;

/**
 * 微信通讯
 */
@RestController
@RequestMapping("/wechat")
@Slf4j
public class WechatIndexController {

    @Autowired
    WechatRedisService wechatRedisService;
    @Autowired
    InsideFeignController insideFeignController;
    @Autowired
    LoginLogService loginLogService;

    /**
     * 微信回调接口（Post方式）
     */
    @PostMapping("/wxCallback")
    @TrackLog
    @PermitAll
    public Mono<Res100705> wxCallback(@RequestHeader(value = "encry-salt", required = false) String salt,
                                      @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                      @RequestBody WechatCallBackDTO wechatCallBackDTO) throws Exception {
        String code = wechatCallBackDTO.getCode();
        AppPartnerEnum appPartner = wechatCallBackDTO.getAppPartner();
        MDC.put("apppartner_desc", appPartner.getDesc());
        MDC.put("apppartner", appPartner.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            String jsessionId = UUIDUtil.createUUID32();
            Res100705 res100705 = Res100705.builder()
                    .jsessionId(jsessionId)
                    .apppartner(appPartner)
                    .apppartnerDesc(appPartner.getDesc())
                    .build();
            if ("authdeny".equals(code)) {
                return res100705;
            }
            //【一】根据code获取openId、accessToken
            WechatGroupEnum wechatGroup = WechatGroupEnum.valueOf(appPartner.name());
            log.info("wechatGroup:[{}][{}], code:[{}]", wechatGroup.getId(), wechatGroup.getDesc(), code);

            AccessTokenDTO accessTokenDTO = wechatRedisService.oauth2AccessToken(wechatGroup, code);
            log.info("accessTokenDTO:[{}]", JacksonUtil.objectToJson(accessTokenDTO));
            String openId = accessTokenDTO.getOpenid();
            String accessToken = accessTokenDTO.getAccessToken();
            if (StringUtils.isEmpty(openId)) {
                throw new ParamsIllegalException(ErrorConstant.AUTH_ERR.getErrorMsg());
            }

            //【二】根据openId、accessToken获取用户信息
            UserInfoDTO userInfo = wechatRedisService.getUserInfo(accessToken, openId);
            String nickName = userInfo.getNickname();
            String headImgurl = userInfo.getHeadimgurl();
            log.info("userInfo:[{}]", JacksonUtil.objectToJson(userInfo));
            if (null == userInfo || StringUtils.isEmpty(userInfo.getNickname())) {
                log.info("根据openId、accessToken获取用户信息失败");
            } else {
                try {
                    nickName = URLEncoder.encode(userInfo.getNickname(), "UTF-8");
                } catch (Exception e) {
                    log.info("获取昵称出现异常！");
                }
                headImgurl = userInfo.getHeadimgurl();
            }

            //【三】用户登录工资条
            CacheUserPrincipal cacheUserPrincipal = wechatRedisService.registeWechatPayroll(jsessionId, openId, nickName, headImgurl, "", appPartner);
            if (StringUtils.isNotBlank(cacheUserPrincipal.getIdNumber())) {
                res100705.setBindStatus("1");
                res100705.setIdNumber(cacheUserPrincipal.getIdNumberEncrytor());
                res100705.setIfPwd(StringUtils.isEmpty(StringUtils.trimToEmpty(cacheUserPrincipal.getQueryPwd())) ? IsStatusEnum.NO.getCode() : IsStatusEnum.YES.getCode());
                res100705.setName(cacheUserPrincipal.getName());
                res100705.setPhone(cacheUserPrincipal.getPhone());
                res100705.setEntId(cacheUserPrincipal.getEntId());
                MDC.put("idNumberEncrytor", cacheUserPrincipal.getIdNumberEncrytor());
            }
            res100705.setHeadimgurl(headImgurl);
            //加密处理
            res100705.setBindStatus(EncrytorUtils.encryptField(res100705.getBindStatus(), salt, passwd));
            res100705.setSalt(salt);
            res100705.setPasswd(passwd);
            Optional.ofNullable(insideFeignController.getSkin(jsessionId, appPartner.getCode().toString())).ifPresent(tuple -> res100705.setThemeId(tuple.getThemeId()));
            log.info("res100705:[{}]", JacksonUtil.objectToJson(res100705));

            //异步记录登录日志
            loginLogService.saveLoginLog(openId, "wxCallback");

            return res100705;
        }).subscribeOn(Schedulers.boundedElastic());
    }

}

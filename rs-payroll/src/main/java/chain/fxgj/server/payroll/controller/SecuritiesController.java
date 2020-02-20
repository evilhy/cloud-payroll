package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.feign.client.PayRollFeignService;
import chain.fxgj.feign.client.SynTimerFeignService;
import chain.fxgj.feign.dto.CheckCardDTO;
import chain.fxgj.feign.dto.response.*;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.response.*;
import chain.fxgj.server.payroll.dto.securities.SecuritiesCustInfoDTO;
import chain.fxgj.server.payroll.service.SecuritiesService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.fxgj.server.payroll.util.TransferUtil;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.PayrollFeignController;
import chain.payroll.dto.response.*;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 证券开户活动
 */
@RestController
@Validated
@RequestMapping(value = "/securityes")
@Slf4j
@SuppressWarnings("unchecked")
public class SecuritiesController {

    @Resource
    RedisTemplate redisTemplate;
    @Autowired
    SecuritiesService securitiesService;

    /**
     * 拖拽式滑块图形验证
     *
     * @return
     */
    @GetMapping("/pictureCheck")
    @TrackLog
    public Mono<String> pictureCheck() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //todo 拖拽式滑块图形验证
            securitiesService.pictureCheck();

            return "";
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 登录校验
     * @param code
     * @param appPartner
     * @return
     */
    @GetMapping("/loginCheck")
    @TrackLog
    public Mono<SecuritiesCustInfoDTO> loginCheck(@RequestParam("code") String code,
                                     @RequestParam(value = "appPartner", required = true, defaultValue = "FXGJ") AppPartnerEnum appPartner) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
//            //【一】根据code获取openId、accessToken
//            WechatGroupEnum wechatGroup = WechatGroupEnum.valueOf(appPartner.name());
//            log.info("wechatGroup:[{}][{}], code:[{}]", wechatGroup.getId(), wechatGroup.getDesc(), code);
//            AccessTokenDTO accessTokenDTO = wechatRedisService.oauth2AccessToken(wechatGroup, code);
//            log.info("accessTokenDTO:[{}]", JacksonUtil.objectToJson(accessTokenDTO));
//            String openId = accessTokenDTO.getOpenid();
//            String accessToken = accessTokenDTO.getAccessToken();
//            if (chain.utils.commons.StringUtils.isEmpty(openId)) {
//                throw new ParamsIllegalException(chain.fxgj.server.payroll.constant.ErrorConstant.AUTH_ERR.getErrorMsg());
//            }
//            //【二】根据 openId、accessToken 获取用户信息
//            UserInfoDTO userInfo = wechatRedisService.getUserInfo(accessToken, openId);
//            String nickName = userInfo.getNickname();
//            String headImgurl = userInfo.getHeadimgurl();
//            log.info("userInfo:[{}]", JacksonUtil.objectToJson(userInfo));
//            if (null == userInfo || chain.utils.commons.StringUtils.isEmpty(userInfo.getNickname())) {
//                log.info("根据openId、accessToken获取用户信息失败");
//            } else {
//                try {
//                    nickName = URLEncoder.encode(userInfo.getNickname(), "UTF-8");
//                } catch (Exception e) {
//                    log.info("获取昵称出现异常！");
//                }
//                headImgurl = userInfo.getHeadimgurl();
//            }
            //todo 根据openId查询唯销用户信息，有值则组装数据，入缓存，并返回给前端
            //唯销没有值 ，查询本地Mysql 微信表，有数据则返回 jsessionId、手机号
            SecuritiesCustInfoDTO securitiesCustInfoDTO = new SecuritiesCustInfoDTO();
            securitiesCustInfoDTO.setJsessionId("123123");
            securitiesCustInfoDTO.setPhone("13400000000");
            return securitiesCustInfoDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 证券登录
     * @return
     */
    @GetMapping("/securitiesLogin")
    @TrackLog
    public Mono<Boolean> securitiesLogin() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //todo 拖拽式滑块图形验证

            boolean b = true;
            return b;
        }).subscribeOn(Schedulers.elastic());
    }

}

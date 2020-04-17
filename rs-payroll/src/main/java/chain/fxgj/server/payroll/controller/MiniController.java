package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.server.payroll.dto.request.ReqMiniInfo;
import chain.fxgj.server.payroll.dto.response.ResMiniUserInfo;
import chain.utils.commons.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.util.Map;

/**
 * 小程序// todo zhuchangjian 确定可以删除，稍后整理
 */
@RestController
@RequestMapping("/mini")
@Slf4j
public class MiniController {

    /**
     * 根据code 获取用户信息
     *
     */
    @GetMapping("/miniUserInfo")
    @TrackLog
    @PermitAll
    public Mono<ResMiniUserInfo> miniUserInfo(@RequestParam("code") String code,
                                      @RequestParam(value = "appPartner", required = true, defaultValue = "FXGJ") AppPartnerEnum appPartner)
            throws Exception {
        MDC.put("apppartner_desc", appPartner.getDesc());
        MDC.put("apppartner", appPartner.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            /**
             * // todo
             * 1.根据code查缓存，如果有值则直接返回
             * 2.如果没有值，则调用ids获取用户信息(ids返回信息包括：openId、sessionKey、用户昵称、头像)，
             *      然后缓存到redis，最后再带上jsessionId 返回
             */
            String jsessionId = UUIDUtil.createUUID32();
            ResMiniUserInfo resMiniUserInfo = new ResMiniUserInfo();
            resMiniUserInfo.setHeadimgurl("http://xxx.xxx...");
            resMiniUserInfo.setJsessionId("123123123");
            resMiniUserInfo.setPhone("13400000000");
            return resMiniUserInfo;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 获取手机号
     */
    @PostMapping("/getPhoneNumber")
    @TrackLog
    @PermitAll
    public Mono<ResMiniUserInfo> getPhoneNumber(@RequestBody ReqMiniInfo reqMiniInfo) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            /**
             * // todo
             * 1.验证数据的有效性
             * 2.解密数据，获取手机号
             * 3.根据jsessionId查询缓存，将手机号放到缓存中
             * 4.返回缓存对象
             */
            ResMiniUserInfo resMiniUserInfo = new ResMiniUserInfo();
            resMiniUserInfo.setPhone("13400000001");
            resMiniUserInfo.setJsessionId("123123123");
            resMiniUserInfo.setHeadimgurl("http://xxx.xxx..");
            return resMiniUserInfo;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 步数上传
     */
    @PostMapping("/upSteps")
    @TrackLog
    @PermitAll
    public Mono<ReqMiniInfo> upSteps(@RequestBody ReqMiniInfo reqMiniInfo) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            // todo 调用唯销数据透传，具体入参，出参待定
            ResMiniUserInfo resMiniUserInfo = new ResMiniUserInfo();
            return reqMiniInfo;
        }).subscribeOn(Schedulers.elastic());
    }

}

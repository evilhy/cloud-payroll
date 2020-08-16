package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.payroll.client.feign.VirusFeignService;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.fxgj.constant.DictEnums.AppPartnerEnum;
import core.dto.PageDTO;
import core.dto.request.virus.VirusRequestDto;
import core.dto.response.virus.NcpVirusPromiseListDto;
import core.dto.response.virus.VirusPromiseAddResDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.ws.rs.DefaultValue;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author flash
 * @date 2020/2/10 10:02
 */
@RestController
@Validated
@RequestMapping(value = "/virus")
@Slf4j
public class VirusController {

    @Autowired
    VirusFeignService virusFeignService;
    @Autowired
    WechatRedisService wechatRedisService;

    /**
     * 查询列表
     * @param pageNum
     * @param size
     * @return
     */
    @GetMapping
    @TrackLog
    public Mono<PageDTO<NcpVirusPromiseListDto>> list(@RequestHeader("page-num") @DefaultValue("1") int pageNum,
                                                      @RequestHeader("limit") @DefaultValue("20") int size,
                                                      @RequestParam(required = false) String openid) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return virusFeignService.list(pageNum,size,openid);
        }).subscribeOn(Schedulers.elastic());
    }

    @PostMapping
    @TrackLog
    public Mono<VirusPromiseAddResDto> post(@RequestBody VirusRequestDto virusRequestDto){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return virusFeignService.post(virusRequestDto);
        }).subscribeOn(Schedulers.elastic());
    }

    @GetMapping("/userInfo")
    @TrackLog
    public Mono<UserInfoDTO> getUserInfo(@RequestParam("code") String code,
                                         @RequestParam(value = "appPartner", defaultValue = "FXGJ") AppPartnerEnum appPartner,
                                         @RequestParam(value = "routeName", required = false) String routeName){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {

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
            log.info("userInfo:[{}]", JacksonUtil.objectToJson(userInfo));
            if (null == userInfo || StringUtils.isEmpty(userInfo.getNickname())) {
                log.info("根据openId、accessToken获取用户信息失败");
            }
        return userInfo;
        }).subscribeOn(Schedulers.elastic());
    }

}

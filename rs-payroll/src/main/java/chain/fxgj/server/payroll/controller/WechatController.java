package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.payroll.client.feign.WechatFeignController;
import chain.pub.client.feign.WechatFeignClient;
import chain.pub.common.dto.wechat.*;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import chain.utils.fxgj.constant.DictEnums.AppPartnerEnum;
import core.dto.wechat.CacheProcessRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 微信通讯
 */
@RestController
@RequestMapping("/weixin")
@Slf4j
public class WechatController {

    @Autowired
    WechatFeignClient wechatFeignClient;
    @Autowired
    WechatRedisService wechatRedisService;
    @Autowired
    WechatFeignController wechatFeignController;

    /**
     * 微信菜单创建
     *
     * @param id 合作方定义(FXGJ-放薪管家, ZRL-中人联, YDNSH-尧都农商, SJZHRB-汇融银行)
     *
     */
    @GetMapping("/creatMenu")
    @TrackLog
    public Mono<WechatMenuResponseDTO> creatMenu(@RequestParam(value = "id") AppPartnerEnum id) {
        MDC.put("apppartner_desc", id.getDesc());
        MDC.put("appPartner", id.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WechatMenuResponseDTO menuById = wechatFeignClient.createMenuById(WechatGroupEnum.valueOf(id.name()));
            log.info("微信菜单创建:[{}] ", JacksonUtil.objectToJson(menuById));
            return menuById;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 微信菜单删除
     *
     * @param id 合作方定义(FXGJ-放薪管家, ZRL-中人联, YDNSH-尧都农商, SJZHRB-汇融银行)
     */
    @GetMapping("/deleteMenu")
    @TrackLog
    public Mono<WechatMenuResponseDTO> deleteMenu(@RequestParam(value = "id") AppPartnerEnum id) {
        MDC.put("apppartner_desc", id.getDesc());
        MDC.put("appPartner", id.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WechatMenuResponseDTO wechatMenuResponseDTO = wechatFeignClient.deleteMenu(WechatGroupEnum.valueOf(id.name()));
            log.info("微信菜单删除:[{}] ", JacksonUtil.objectToJson(wechatMenuResponseDTO));
            return wechatMenuResponseDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 微信菜单获取
     *
     * @param id 合作方定义(FXGJ-放薪管家, ZRL-中人联, YDNSH-尧都农商, SJZHRB-汇融银行)
     */
    @GetMapping("/getMenu")
    @TrackLog(maxLogLength = 1000)
    public Mono<WechatQueryMenuDTO> getMenu(@RequestParam(value = "id") AppPartnerEnum id) {
        MDC.put("apppartner_desc", id.getDesc());
        MDC.put("appPartner", id.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WechatQueryMenuDTO menu = wechatFeignClient.getMenu(WechatGroupEnum.valueOf(id.name()));
            log.info("微信菜单获取:[{}] ", JacksonUtil.objectToJson(menu));
            return menu;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * (GET方式)验证消息的确来自微信服务器 仅仅是验签
     * 使用场景：</p>
     * 微信直接发送 ->  后台服务器
     * </p>
     *
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param echostr   随机字符串
     * @param id        对接渠道id
     * @return
     * @throws BusiVerifyException
     */
    @GetMapping("/signature")
    @TrackLog
    @PermitAll
    public Mono<String> signatureGet(@RequestParam("signature") String signature,
                                     @RequestParam("timestamp") String timestamp,
                                     @RequestParam("nonce") String nonce,
                                     @RequestParam("echostr") String echostr,
                                     @RequestParam(value = "id", required = true, defaultValue = "FXGJ") AppPartnerEnum id
    ) {
        MDC.put("apppartner_desc", id.getDesc());
        MDC.put("appPartner", id.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("微信服务器发送的消:signature:[{}], timestamp:[{}], nonce:[{}], echostr:[{}], id:[{}]", signature, timestamp, nonce, echostr, id);
            String name = id.name();
            String resultEchostr = wechatFeignClient.signature(WechatGroupEnum.valueOf(name), signature, timestamp, nonce, echostr);
            if (!echostr.equals(resultEchostr)) {
                log.info("====>验签失败！");
                throw new ParamsIllegalException(ErrorConstant.WZWAGE_011.getErrorMsg());
            }
            log.info("====>echostrRet:[{}]，验签成功！", resultEchostr);
            return echostr;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * (POST方式)验证消息的确来自微信服务器 验签+业务处理
     * 用户发送信息->微信接收信息后再调用->后台服务(后台验签，通过后返回对应消息)->微信->用户</p>
     *
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param id        对接渠道id
     * @param xml
     * @return
     * @throws BusiVerifyException
     */
    @PostMapping(value = "/signature", consumes = {MediaType.TEXT_XML_VALUE})
    @TrackLog
    public Mono<String> signaturegPost(@RequestParam("signature") String signature,
                                       @RequestParam("timestamp") String timestamp,
                                       @RequestParam("nonce") String nonce,
                                       @RequestParam(value = "id", required = true, defaultValue = "FXGJ") chain.utils.fxgj.constant.DictEnums.AppPartnerEnum id,
                                       @RequestBody String xml) {
        log.info("signature:[{}], timestamp:[{}], nonce:[{}], appPartner:[{}],[{}]", signature, timestamp, nonce, id.getCode(), id.getDesc());
        MDC.put("apppartnerdesc", id.getDesc());
        MDC.put("apppartner", id.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String name = id.name();
            WechatGroupEnum wechatGroup = WechatGroupEnum.valueOf(name);

            //【一】先验签， 通过之后再处理微信请求
            String echostr = UUIDUtil.createUUID32();
            log.info("====>echostr:[{}]，验签开始！", echostr);
            String resultEchostr = wechatFeignClient.signature(wechatGroup, signature, timestamp, nonce, echostr);
            if (!echostr.equals(resultEchostr)) {
                log.info("====>验签失败！");
                throw new ParamsIllegalException(ErrorConstant.WZWAGE_011.getErrorMsg());
            } else {
                log.info("====>echostrRet:[{}]，验签成功！", resultEchostr);
            }

            //【二】调用put-client获取微信配置，构造网络授权连接
            WechatConfigDTO wechatConfigDTO = wechatFeignClient.getConfig(wechatGroup);
            log.info("wechatConfigDTO:[{}]", wechatConfigDTO);
            String appId = wechatConfigDTO.getAppId();
            String oauthUrl = wechatConfigDTO.getOauthUrl();
            String authorizeurl = PayrollConstants.OAUTH_AUTHORIZE_URL;
            authorizeurl = authorizeurl.replace("APPID", appId);
            authorizeurl = authorizeurl.replace("REDIRECT_URI", URLEncoder.encode(oauthUrl, "UTF-8"));
            authorizeurl = authorizeurl.replace("STATE", StringUtils.trimToEmpty(""));
            authorizeurl = authorizeurl.replace("SCOPE", PayrollConstants.SNSAPI_USERINFO);

            //【三】处理微信用户请求
            CacheProcessRequestDTO cacheProcessRequestDTO = new CacheProcessRequestDTO();
            cacheProcessRequestDTO.setAuthorizeurl(authorizeurl);
            cacheProcessRequestDTO.setXml(xml);
            cacheProcessRequestDTO.setAppPartner(id);
            log.info("wageProcessRequestDTO:[{}]", JacksonUtil.objectToJson(cacheProcessRequestDTO));
            String sendContent = wechatFeignController.processRequest(cacheProcessRequestDTO);
            log.info("sendContent:[{}]", sendContent);
            return sendContent;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * JS分享产生分享签名 (未测试是否可用)
     *
     */
    @GetMapping("/getJsapiSignature")
    @TrackLog
    @PermitAll
    public Mono<core.dto.wechat.WeixinJsapiDTO> getJsapiSignature(@RequestParam("url") String url) throws BusiVerifyException {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("getJsapiSignature url:[{}]", url);

            AppPartnerEnum id = AppPartnerEnum.FXGJ;
            WechatGroupEnum wechatGroup = WechatGroupEnum.valueOf(id.name());
            WechatJsapiRequestDTO wechatJsapiRequestDTO = wechatFeignClient.jsapiSignature(wechatGroup, url);
            log.info("wechatJsapiRequestDTO:[{}]", JacksonUtil.objectToJson(wechatJsapiRequestDTO));

            //设置返回参数
            core.dto.wechat.WeixinJsapiDTO weixinJsapiDTO = new core.dto.wechat.WeixinJsapiDTO();
            weixinJsapiDTO.setAppid(wechatJsapiRequestDTO.getAppid());
            weixinJsapiDTO.setExpiresIn(wechatJsapiRequestDTO.getExpiresIn());
            weixinJsapiDTO.setJsapiTicket(wechatJsapiRequestDTO.getJsapiTicket());
            weixinJsapiDTO.setNoncestr(wechatJsapiRequestDTO.getNoncestr());
            weixinJsapiDTO.setRedirectUrl(wechatJsapiRequestDTO.getRedirectUrl());
            weixinJsapiDTO.setSignature(wechatJsapiRequestDTO.getSignature());
            weixinJsapiDTO.setTimestamp(wechatJsapiRequestDTO.getTimestamp());
            weixinJsapiDTO.setUrl(wechatJsapiRequestDTO.getUrl());
            return weixinJsapiDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * JS分享产生分享签名(新增接口，前端增加 AppPartnerEnum)
     *
     */
    @GetMapping("/getJsapiSignatureById")
    @TrackLog
    @PermitAll
    public Mono<WechatJsapiRequestDTO> getJsapiSignatureById(
            @RequestParam("url") String url,
            @RequestParam(value = "id", required = true, defaultValue = "FXGJ") AppPartnerEnum id)
            throws BusiVerifyException {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("getJsapiSignature url:[{}],id:[{}]", url, id);
            WechatGroupEnum wechatGroup = WechatGroupEnum.valueOf(id.name());
            WechatJsapiRequestDTO wechatJsapiRequestDTO = wechatFeignClient.jsapiSignature(wechatGroup, url);
            log.info("wechatJsapiRequestDTO:[{}]", JacksonUtil.objectToJson(wechatJsapiRequestDTO));
            return wechatJsapiRequestDTO;
        }).subscribeOn(Schedulers.elastic());
    }
}

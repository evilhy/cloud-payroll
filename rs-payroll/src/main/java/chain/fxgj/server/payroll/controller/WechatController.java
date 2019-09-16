package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.feign.client.WechatFeignService;
import chain.fxgj.feign.dto.base.WageWeixinJsapiDTO;
import chain.fxgj.feign.dto.response.WageRes100705;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.base.*;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import javax.annotation.security.PermitAll;
import java.util.Map;

/**
 * 微信通讯
 */
@RestController
@Validated
@RequestMapping("/weixin")
@Slf4j
public class WechatController {

    @Autowired
    private WechatFeignService wechatFeignService;

    /**
     * (GET方式)验证消息的确来自微信服务器
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
        MDC.put("apppartner_desc",id.getDesc());
        MDC.put("appPartner",id.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("====>微信服务器发送的消: signature ={} , timestamp ={} ,nonce ={} ,echostr ={},id={}", signature, timestamp, nonce, echostr, id);
            String resultEchostr=wechatFeignService.signatureGet(signature, timestamp, nonce, echostr,id);
            if (!echostr.equals(resultEchostr)) {
                log.info("====>验签失败！");
                throw new ParamsIllegalException(ErrorConstant.WZWAGE_011.getErrorMsg());
            }
            log.info("====>echostrRet:[{}]，验签成功！", resultEchostr);
            return echostr;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * (POST方式)验证消息的确来自微信服务器
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
                                       @RequestParam(value = "id", required = true, defaultValue = "FXGJ") AppPartnerEnum id,
                                       @RequestBody String xml) {
        MDC.put("apppartnerdesc",id.getDesc());
        MDC.put("apppartner",id.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("====>apppartnerdesc={},appPartner={}",MDC.get("apppartnerdesc"),MDC.get("apppartner"));
            //验签
            String sendContent = wechatFeignService.signaturegPost(signature,timestamp,nonce,id,xml);
            log.info("signaturegPost-->{}",sendContent);
            return sendContent;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 微信回调接口
     */
    @GetMapping("/wxCallback")
    @TrackLog
    @PermitAll
    public Mono<Res100705> wxCallback(@RequestParam("code") String code,
                                      @RequestParam(value = "wageSheetId", required = false) String wageSheetId,
                                      @RequestParam(value = "appPartner", required = true, defaultValue = "FXGJ") AppPartnerEnum appPartner,
                                      @RequestParam(value = "routeName", required = false) String routeName) throws Exception {
        MDC.put("apppartner_desc",appPartner.getDesc());
        MDC.put("apppartner",appPartner.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Res100705 res100705=null;
            WageRes100705 wageRes100705 =wechatFeignService.wxCallback(code,wageSheetId,appPartner,routeName);
            log.info("wxCallback--->{}",wageRes100705);
            if (wageRes100705!=null){
                res100705=new Res100705();
                BeanUtils.copyProperties(wageRes100705,res100705);
            }
            //todo 重定向地址
            log.info("====>res100705返回的所有值:[{}]", res100705.toString());
            return res100705;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * JS分享产生分享签名
     */
    @GetMapping("/getJsapiSignature")
    @TrackLog
    @PermitAll
    public Mono<WeixinJsapiDTO> getJsapiSignature(@RequestParam("url") String url) throws BusiVerifyException {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //WeixinJsapiDTO weixinJsapiDTO = iwechatFeignService.getJsapiSignature(payrollProperties.getId(),url);
            WeixinJsapiDTO weixinJsapiDTO = null;
            WageWeixinJsapiDTO jsapiDTO=wechatFeignService.getJsapiSignature(url);
            log.info("getJsapiSignature--result:{}",jsapiDTO);
            if (jsapiDTO!=null){
                weixinJsapiDTO=new WeixinJsapiDTO();
                BeanUtils.copyProperties(jsapiDTO,weixinJsapiDTO);
            }
            log.info("====>ret.weixinJsapiDTO:[{}]", JacksonUtil.objectToJson(weixinJsapiDTO));
            return weixinJsapiDTO;
        }).subscribeOn(Schedulers.elastic());
    }


}

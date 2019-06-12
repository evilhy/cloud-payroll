package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.core.common.service.CallInsideService;
import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.core.common.service.PayRollAsyncService;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.EventDTO;
import chain.fxgj.server.payroll.dto.base.*;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.dto.wechat.WeixinTextMsgBaseDTO;
import chain.fxgj.server.payroll.dto.wechat.WeixinXMLDTO;
import chain.fxgj.server.payroll.util.WeixinMsgUtil;
import chain.fxgj.server.payroll.util.XmlUtil;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * 微信通讯
 */
@CrossOrigin
@RestController
@Validated
@RequestMapping("/weixin")
@Slf4j
public class WechatRS {

    @Autowired
    PayrollProperties payrollProperties;
    //@Autowired
    //IwechatFeignService iwechatFeignService;
    @Autowired
    CallInsideService callInsideService;
    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    PayRollAsyncService payRollAsyncService;

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
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            log.info("====>微信服务器发送的消: signature ={} , timestamp ={} ,nonce ={} ,echostr ={},id={}", signature, timestamp, nonce, echostr, id);
            //接口调用
            //String echostrRet = iwechatFeignService.signature(payrollProperties.getId(), signature, timestamp, nonce, echostr);
            WeixinSignatureDTO weixinSignatureDTO = new WeixinSignatureDTO(signature, timestamp, nonce, echostr);
            WeixinSignatureDTO weixinSignatureDTO1 = callInsideService.checkWechatSignature(weixinSignatureDTO);
            String echostrRet = weixinSignatureDTO1.getEchostr();
            if (!echostr.equals(echostrRet)) {
                log.info("====>验签失败！");
                throw new ParamsIllegalException(ErrorConstant.WZWAGE_011.getErrorMsg());
            }
            log.info("====>echostrRet:[{}]，验签成功！", echostrRet);
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
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            String uuid32 = UUIDUtil.createUUID32();
            //验签
            //String echostrRet = iwechatFeignService.signature(id, signature, timestamp, nonce, uuid32);
            WeixinSignatureDTO weixinSignatureDTO = new WeixinSignatureDTO(signature, timestamp, nonce, uuid32);
            WeixinSignatureDTO weixinSignatureDTO1 = callInsideService.checkWechatSignature(weixinSignatureDTO);
            String echostrRet = weixinSignatureDTO1.getEchostr();
            String sendContent = "";
            if (uuid32.equals(echostrRet)) {  //只有通过验证的才返回消息
                try {
                    log.info("====>id={},入参xml:[{}]", id, xml);
                    WeixinXMLDTO weixinXMLDTO = (WeixinXMLDTO) XmlUtil.xmlToBean(xml, WeixinXMLDTO.class);
                    String event = weixinXMLDTO.getEvent();
                    String msgType = weixinXMLDTO.getMsgType();
                    String content = weixinXMLDTO.getContent();
                    String fromUserName = weixinXMLDTO.getFromUserName();
                    String toUserName = weixinXMLDTO.getToUserName();

                    WeixinTextMsgBaseDTO textMessage = new WeixinTextMsgBaseDTO();
                    textMessage.setToUserName(fromUserName);
                    textMessage.setFromUserName(toUserName);
                    textMessage.setCreateTime((new Date()).getTime());
                    textMessage.setMsgType("text");
                    textMessage.setContent(content);

                    //获取扩展属性
                    //List<WeixinExtResponeDTO> oauthUrlList = iwechatFeignService.getWechatCfg(id, "oauthUrl");
                    //String oauthUrl = "";
                    //if (oauthUrlList.size() == 1) {
                    //    oauthUrl = (String) oauthUrlList.get(0).getValue();
                    //}
                    WeixinCfgResponeDTO wechatCfg = callInsideService.getWechatCfg();
                    String oauthUrl = wechatCfg.getOauthUrl();
                    log.info("====>获取配置文件扩展属性oauthUrl:[{}]", oauthUrl);
                    WeixinAuthorizeUrlDTO weixinAuthorizeUrlDTO = new WeixinAuthorizeUrlDTO();
                    weixinAuthorizeUrlDTO.setUrl(oauthUrl);
                    WeixinAuthorizeUrlDTO oAuthUrl = callInsideService.getOAuthUrl(weixinAuthorizeUrlDTO);
                    String authorizeurl = oAuthUrl.getAuthorizeurl();
                    //构造网页授权链接
                    //weixinAuthorizeUrlDTO = iwechatFeignService.getOAuthUrl(id,weixinAuthorizeUrlDTO);
                    //String authorizeurl = weixinAuthorizeUrlDTO.getAuthorizeurl();
                    log.info("====>构造网页授权链接oauth_url:[{}]", authorizeurl);

                    sendContent = WeixinMsgUtil.processRequest(textMessage, content, authorizeurl, event, msgType);
                    log.info("====>sentCount={}", sendContent);

                    //事件类型，subscribe(订阅)、unsubscribe(取消订阅)
                    if ("subscribe".equalsIgnoreCase(event) || "unsubscribe".equalsIgnoreCase(event)) {
                        log.info("====>微信关注/取关:{},{}", event, fromUserName);
                        try {
                            EventDTO eventDTO = EventDTO.builder()
                                    .openId(fromUserName)
                                    .event(event)
                                    .build();
                            //请求关注/取关
                            payRollAsyncService.eventHandle(eventDTO);
                        } catch (Exception e) {
                            log.error("====>微信关注/取关入库异常！");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            String jsessionId = UUIDUtil.createUUID32();
            Res100705 res100705 = Res100705.builder().jsessionId(jsessionId).build();

            // 用户同意授权
            if (!"authdeny".equals(code)) {
                log.info("=========>wageSheetId={},code={},routeName={}", StringUtils.trimToEmpty(wageSheetId), code, routeName);

                //网页授权接口访问凭证
                //WeixinOauthTokenResponeDTO weixinOauthTokenResponeDTO = iwechatFeignService.oauth2Acces(payrollProperties.getId(), code);
                WeixinOauthTokenResponeDTO weixinOauthTokenResponeDTO = callInsideService.getOauth2AccessToken(code);
                String openId = weixinOauthTokenResponeDTO.getOpenid();
                String accessToken = weixinOauthTokenResponeDTO.getAccessToken();
                log.info("============>openId={},accessToken={}", openId, accessToken);

                if (StringUtils.isEmpty(openId)) {
                    log.info("====>获取openId失败");
                    throw new ParamsIllegalException(ErrorConstant.AUTH_ERR.getErrorMsg());
                }
                //获取用户信息
                String nickName = "";
                String headImg = "";
                //WeixinUserInfoResponeDTO weixinUserInfoResponeDTO = iwechatFeignService.getUserInfo(accessToken,openId);
                WeixinUserInfoResponeDTO weixinUserInfoResponeDTO = callInsideService.getUserInfo(accessToken, openId);
                if (weixinUserInfoResponeDTO == null || StringUtils.isEmpty(weixinUserInfoResponeDTO.getNickname())) {
                    log.info("====>openId:{},获取用户信息失败");
                } else {
                    try {
                        nickName = URLEncoder.encode(weixinUserInfoResponeDTO.getNickname(), "UTF-8");
                    } catch (Exception e) {
                        log.error("获取昵称出现异常！");
                    }
                    headImg = weixinUserInfoResponeDTO.getHeadimgurl();
                }

                //登录工资条
                UserPrincipal userPrincipal = empWechatService.setWechatInfo(jsessionId, openId, nickName, headImg, "", appPartner);
                if (StringUtils.isNotBlank(userPrincipal.getIdNumber())) {
                    res100705.setBindStatus("1");
                    res100705.setIdNumber(userPrincipal.getIdNumberEncrytor());
                    res100705.setIfPwd(StringUtils.isEmpty(StringUtils.trimToEmpty(userPrincipal.getQueryPwd())) ? IsStatusEnum.NO.getCode() : IsStatusEnum.YES.getCode());
                    res100705.setName(userPrincipal.getName());
                    res100705.setPhone(userPrincipal.getPhone());
                }
                res100705.setHeadimgurl(headImg);
                log.info("====>微信：{},{},{},{},{}", res100705.getJsessionId(), userPrincipal.getPhone(), res100705.getBindStatus(), userPrincipal.getIdNumber(), res100705.getIfPwd());
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
            WeixinJsapiDTO weixinJsapiDTO = callInsideService.getJsapiSignature(url);
            log.info("====>ret.weixinJsapiDTO:[{}]", JacksonUtil.objectToJson(weixinJsapiDTO));
            return weixinJsapiDTO;
        }).subscribeOn(Schedulers.elastic());
    }


}

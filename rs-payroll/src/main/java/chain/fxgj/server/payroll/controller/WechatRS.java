package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.core.common.dto.weixin.WeixinCfgResponeDTO;
import chain.fxgj.core.common.dto.weixin.msg.WeixinTextMsgBaseDTO;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.base.weixin.WeixinXMLDTO;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.util.WeixinMsgUtil;
import chain.fxgj.server.payroll.util.XmlUtil;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.outside.common.dto.wechat.*;
import chain.outside.common.dto.wechat.util.MsgUtil;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import chain.wechat.client.feign.IwechatFeignService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import javax.ws.rs.core.Context;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;

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
    IwechatFeignService iwechatFeignService;

    /**
     * 微信公众号分组id(配置文件)
     */
    @Value("${property.id}")
    String id;

    /**
     * (GET方式)验证消息的确来自微信服务器
     * 使用场景：</p>
     * 微信直接发送 ->  后台服务器
     * </p>
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echostr 随机字符串
     * @return
     * @throws BusiVerifyException
     */
    @GetMapping("/signature")
    @TrackLog
    @PermitAll
    public Mono<String> signatureGet(@RequestParam("signature") String signature,
                                     @RequestParam("timestamp") String timestamp,
                                     @RequestParam("nonce") String nonce,
                                     @RequestParam("echostr") String echostr
    ) throws BusiVerifyException {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        Map<String, String> mdcContent = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            MDC.setContextMap(mdcContent);
            log.info("微信服务器发送的消: signature ={} , timestamp ={} ,nonce ={} ,echostr ={}", signature, timestamp, nonce, echostr);
            //接口调用
            String echostrRet = iwechatFeignService.signature(id, signature, timestamp, nonce, echostr);
            if (!echostr.equals(echostrRet)) {
                log.info("验签失败！");
                throw new ParamsIllegalException(ErrorConstant.WZWAGE_011.getErrorMsg());
            }
            log.info("echostrRet:[{}]，验签成功！",echostrRet);
            return echostr;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * (POST方式)验证消息的确来自微信服务器
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echostr 随机字符串
     * @param xml
     * @return
     * @throws BusiVerifyException
     */
    @PostMapping("/signature")
    @TrackLog
    @PermitAll
    public Mono<String> signaturegPost(@RequestParam("signature") String signature,
                                       @RequestParam("timestamp") String timestamp,
                                       @RequestParam("nonce") String nonce,
                                       @RequestParam("echostr") String echostr,
                                       @RequestBody String xml) throws BusiVerifyException {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        Map<String, String> mdcContent = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            MDC.setContextMap(mdcContent);

            //验签
            String echostrRet = "";//iwechatFeignService.signature(id,signature, timestamp, nonce,echostr);
            String sendContent = "";
            if (echostr.equals(echostrRet)) {  //只有通过验证的才返回消息
                try {
                    log.info("入参xml:[{}]",xml);
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
                    List<WeixinExtResponeDTO> oauthUrlList = iwechatFeignService.getWechatCfg(id, "oauthUrl");
                    String oauthUrl = "";
                    if (oauthUrlList.size() == 1) {
                        oauthUrl = (String) oauthUrlList.get(0).getValue();
                    }
                    log.info("获取配置文件扩展属性oauthUrl:[{}]",oauthUrl);
                    WeixinAuthorizeUrlDTO weixinAuthorizeUrlDTO = new WeixinAuthorizeUrlDTO();
                    weixinAuthorizeUrlDTO.setUrl(oauthUrl);
                    weixinAuthorizeUrlDTO = iwechatFeignService.getOAuthUrl(id,weixinAuthorizeUrlDTO);
                    String authorizeurl = weixinAuthorizeUrlDTO.getAuthorizeurl();
                    log.info("构造网页授权链接oauth_url:[{}]", authorizeurl);
                    Map<String,String> requestMap = new HashMap<>();
                    requestMap.put("oauth_url", authorizeurl);
                    sendContent = WeixinMsgUtil.processRequest(textMessage, content, requestMap, event, msgType);
                    log.info("sentCount={}", sendContent);

                    //zcj todo 入库
//                    //事件类型，subscribe(订阅)、unsubscribe(取消订阅)
//                    if (Event.equalsIgnoreCase("subscribe") || Event.equalsIgnoreCase("unsubscribe")) {
//                        String FromUserName = requestMap.get("FromUserName").toString();
//                        log.info("微信关注/取关:{},{}", Event, FromUserName);
//                        try {
//                            EventDTO eventDTO = new EventDTO();
//                            eventDTO.setOpenId(FromUserName);
//                            eventDTO.setEvent(Event);
//                            //请求关注/取关
//                            payRollAsyncService.eventHandle(eventDTO);
//
//                        } catch (Exception e) {
//                        }
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return sendContent;
        }).subscribeOn(Schedulers.elastic());
    }

    public static String transMapToString(Map map) {
        Map.Entry entry;
        StringBuffer sb = new StringBuffer();
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            entry = (Map.Entry) iterator.next();
            sb.append(entry.getKey().toString()).append("'").append(null == entry.getValue() ? "" :
                    entry.getValue().toString()).append(iterator.hasNext() ? "^" : "");
        }
        return sb.toString();
    }

    /**
     * 微信回调接口
     */
    @GetMapping("/wxCallback")
    @TrackLog
    @PermitAll
    public Mono<Res100705> wxCallback(@RequestParam("code") String code,
                                      @RequestParam("wageSheetId") String wageSheetId,
                                      @RequestParam("routeName") String routeName) throws Exception {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        Map<String, String> mdcContent = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            MDC.setContextMap(mdcContent);

            String jsessionId = UUIDUtil.createUUID32();
            Res100705 res100705 = new Res100705();
            res100705.setJsessionId(jsessionId);
            res100705.setBindStatus("0");

            // 用户同意授权
            if (!"authdeny".equals(code)) {

                log.info("=========wageSheetId={},code={},routeName={}", StringUtils.trimToEmpty(wageSheetId), code, routeName);

                //网页授权接口访问凭证
                WeixinOauthTokenResponeDTO weixinOauthTokenResponeDTO = iwechatFeignService.oauth2Acces(id, code);
                String openId = weixinOauthTokenResponeDTO.getOpenid();
                String accessToken = weixinOauthTokenResponeDTO.getAccessToken();
                log.info("============openId={}", openId);
                log.info("============accessToken={}", accessToken);
                if (StringUtils.isEmpty(openId)) {
                    throw new ParamsIllegalException(ErrorConstant.AUTH_ERR.getErrorMsg());
                }
                //获取用户信息
                String nickName = "";
                String headImg = "";
//                WeixinUserInfoResponeDTO weixinUserInfoResponeDTO = wechatService.getUserInfo(accessToken, openId);
                WeixinUserInfoResponeDTO weixinUserInfoResponeDTO = iwechatFeignService.getUserInfo(accessToken,openId);
                if (weixinUserInfoResponeDTO == null || StringUtils.isEmpty(weixinUserInfoResponeDTO.getNickname())) {
                    log.info("openId:{},获取用户信息失败");
                } else {
                    try {
                        nickName = URLEncoder.encode(weixinUserInfoResponeDTO.getNickname(), "UTF-8");
                    } catch (Exception e) {
                    }
                    headImg = weixinUserInfoResponeDTO.getHeadimgurl();
                }
                //Thread.sleep(9000);
                //登录工资条
                //zcj todo  userPrincipal取值
//                UserPrincipal userPrincipal = empWechatService.setWechatInfo(jsessionId, openId, nickName, headImg);
                UserPrincipal userPrincipal = new UserPrincipal();
                if (StringUtils.isNotBlank(userPrincipal.getIdNumber())) {
                    res100705.setBindStatus("1");
                    res100705.setIdNumber(userPrincipal.getIdNumberEncrytor());
                    res100705.setIfPwd(StringUtils.isEmpty(StringUtils.trimToEmpty(userPrincipal.getQueryPwd())) ? IsStatusEnum.NO.getCode() : IsStatusEnum.YES.getCode());
                    res100705.setName(userPrincipal.getName());
                    res100705.setPhone(userPrincipal.getPhone());
                }
                res100705.setHeadimgurl(headImg);

                log.info("微信：{},{},{},{},{}", res100705.getJsessionId(), userPrincipal.getPhone(), res100705.getBindStatus(), userPrincipal.getIdNumber(), res100705.getIfPwd());
            }
            //todo 重定向地址
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
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        Map<String, String> mdcContent = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            MDC.setContextMap(mdcContent);
//            WeixinJsapiDTO weixinJsapiDTO = wechatService.getJsapiSignature(url);

            WeixinJsapiDTO weixinJsapiDTO = iwechatFeignService.getJsapiSignature(id,url);
            log.info("weixinJsapiDTO:[{}]", JacksonUtil.objectToJson(weixinJsapiDTO));
            return weixinJsapiDTO;
        }).subscribeOn(Schedulers.elastic());
    }
}

package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.outside.common.dto.wechat.WeixinJsapiDTO;
import chain.outside.common.dto.wechat.WeixinOauthTokenResponeDTO;
import chain.outside.common.dto.wechat.WeixinUserInfoResponeDTO;
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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Iterator;
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
//    @Autowired
//    PayrollProperties payrollProperties;
//
//    @Autowired
//    WechatService wechatService;
//    @Autowired
//    EmpWechatService empWechatService;
//    @Autowired
//    PayRollAsyncService payRollAsyncService;

    @Autowired
    IwechatFeignService iwechatFeignService;

    @Value("${property.id}")
    String id;
    /**
     * 微信直接发送 ->  后台服务器
     * </p>
     * 要 传递 id（属于哪个微信公众号平台）
     * @GET
     * @Path("signature")
     * @Produces(MediaType.TEXT_PLAIN)
     * @Consumes(MediaType.APPLICATION_JSON) (GET方式)验证消息的确来自微信服务器
     */
    @GetMapping("/signature")
    @TrackLog
    @PermitAll
    public Mono<String> signatureGet(@RequestParam(required = false) String signature,
                                     @RequestParam(required = false) String timestamp,
                                     @RequestParam(required = false) String nonce,
                                     @RequestParam(required = false) String echostr
    ) throws BusiVerifyException {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        Map<String, String> mdcContent = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            MDC.setContextMap(mdcContent);
            log.info("微信服务器发送的消: signature ={} , timestamp ={} ,nonce ={} ,echostr ={}, id = [{}]  ", signature, timestamp, nonce, echostr,id);
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
     *      @POST
     *      @Path("signature")
     *      @Produces(MediaType.APPLICATION_JSON)   //注释代表的是一个资源可以返回的 MIME 类型。
     *      @Consumes(MediaType.TEXT_XML)   //注释代表的是一个资源可以接受的 MIME 类型。
     *      @ApiOperation(value = "(POST方式)验证消息的确来自微信服务器")
     *
     * (POST方式)验证消息的确来自微信服务器
     */
    @PostMapping("/signature")
    @TrackLog
    @PermitAll
    public Mono<String> signaturegPost(@RequestParam(required = false) String signature,
                                       @RequestParam(required = false) String timestamp,
                                       @RequestParam(required = false) String nonce,
                                       @RequestParam(required = false) String echostr,
                                       @RequestBody String xml) throws BusiVerifyException {

        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        Map<String, String> mdcContent = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            MDC.setContextMap(mdcContent);

            //先将String类型xml格式的数据去除换行符"\n"，再转换成流，最后转换成Map
            String error_jsonString = xml;
            error_jsonString = error_jsonString.replaceAll("[\\t\\n\\r]", id).replaceAll("\\{ ", "{");
            InputStream inputStream = new ByteArrayInputStream(error_jsonString.getBytes());
            Map<String, String> requestMap = MsgUtil.parseXml(inputStream);

            String Event = (String) requestMap.get("Event");//事件

            String sendContent = iwechatFeignService.signaturegPost(signature, timestamp, nonce, id, xml);

            //zcj todo 入库
//            //事件类型，subscribe(订阅)、unsubscribe(取消订阅)
//            if (Event.equalsIgnoreCase("subscribe") || Event.equalsIgnoreCase("unsubscribe")) {
//                String FromUserName = requestMap.get("FromUserName").toString();
//                log.info("微信关注/取关:{},{}", Event, FromUserName);
//                try {
//                    EventDTO eventDTO = new EventDTO();
//                    eventDTO.setOpenId(FromUserName);
//                    eventDTO.setEvent(Event);
//                    //请求关注/取关
//                    payRollAsyncService.eventHandle(eventDTO);
//
//                } catch (Exception e) {
//                    log.info("入库异常");
//                }
//            }

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
    public Mono<Res100705> wxCallback(@RequestParam(required = false) String code,
                                      @RequestParam(required = false) String wageSheetId,
                                      @RequestParam(required = false) String routeName) throws Exception {
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
//                WeixinOauthTokenResponeDTO weixinOauthTokenResponeDTO = wechatService.getOauth2AccessToken(code);
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
    public Mono<WeixinJsapiDTO> getJsapiSignature(
            @RequestParam(required = false) String url) throws BusiVerifyException {
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

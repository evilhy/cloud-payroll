package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.feign.client.WechatFeignService;
import chain.fxgj.feign.dto.base.WageWeixinJsapiDTO;
import chain.fxgj.feign.dto.response.WageRes100705;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.feign.dto.wechat.WageProcessRequestDTO;
import chain.fxgj.feign.dto.wechat.WageSignaturegPostDTO;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.base.*;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.pub.client.feign.WechatFeignClient;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.dto.wechat.WechatConfigDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
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
    private WechatFeignService wechatFeignService;
    @Autowired
    WechatFeignClient wechatFeignClient;
    @Autowired
    WechatRedisService wechatRedisService;
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
//            String resultEchostr=wechatFeignService.signatureGet(signature, timestamp, nonce, echostr,id);
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
        log.info("signature:[{}], timestamp:[{}], nonce:[{}], appPartner:[{}],[{}]", signature ,timestamp ,nonce ,id.getCode(), id.getDesc());
        MDC.put("apppartnerdesc",id.getDesc());
        MDC.put("apppartner",id.getCode().toString());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageSignaturegPostDTO wageSignaturegPostDTO = new WageSignaturegPostDTO();
            wageSignaturegPostDTO.setAppPartnerEnum(id);
            wageSignaturegPostDTO.setNonce(nonce);
            wageSignaturegPostDTO.setSignature(signature);
            wageSignaturegPostDTO.setTimestamp(timestamp);
            wageSignaturegPostDTO.setXml(xml);
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
            WageProcessRequestDTO wageProcessRequestDTO = new WageProcessRequestDTO();
            wageProcessRequestDTO.setAuthorizeurl(authorizeurl);
            wageProcessRequestDTO.setXml(xml);
            log.info("wageProcessRequestDTO:[{}]", JacksonUtil.objectToJson(wageProcessRequestDTO));
            String sendContent = wechatFeignService.processRequest(wageProcessRequestDTO);
            log.info("sendContent:[{}]",sendContent);
            return sendContent;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 微信回调接口
     */
    @GetMapping("/wxCallbackOld")
    @TrackLog
    @PermitAll
    public Mono<Res100705> wxCallbackOld(@RequestParam("code") String code,
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

            String jsessionId = UUIDUtil.createUUID32();
            Res100705 res100705 = Res100705.builder()
                    .jsessionId(jsessionId)
                    .apppartner(appPartner.getCode())
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
            WageUserPrincipal wageUserPrincipal = wechatRedisService.registeWechatPayroll(jsessionId, openId, nickName, headImgurl, "", appPartner);
            if (StringUtils.isNotBlank(wageUserPrincipal.getIdNumber())) {
                res100705.setBindStatus("1");
                res100705.setIdNumber(wageUserPrincipal.getIdNumberEncrytor());
                res100705.setIfPwd(StringUtils.isEmpty(StringUtils.trimToEmpty(wageUserPrincipal.getQueryPwd())) ? IsStatusEnum.NO.getCode() : IsStatusEnum.YES.getCode());
                res100705.setName(wageUserPrincipal.getName());
                res100705.setPhone(wageUserPrincipal.getPhone());
            }
            res100705.setHeadimgurl(headImgurl);
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

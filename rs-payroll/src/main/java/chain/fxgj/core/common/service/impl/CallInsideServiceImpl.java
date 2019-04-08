package chain.fxgj.core.common.service.impl;

import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.constant.DictEnums.MsgBuisTypeEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.dto.msg.MsgCodeLogCheckRequestDTO;
import chain.fxgj.core.common.dto.msg.MsgCodeLogRequestDTO;
import chain.fxgj.core.common.dto.msg.MsgCodeLogResponeDTO;
import chain.fxgj.server.payroll.dto.base.*;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.fxgj.core.common.service.CallInsideService;
import chain.fxgj.server.payroll.dto.EventDTO;
import chain.fxgj.server.payroll.dto.response.Res100302;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Service
@Slf4j
public class CallInsideServiceImpl implements CallInsideService {

    @Autowired
    PayrollProperties payrollProperties;
    @Autowired
    @Qualifier("wechatClient")
    Client client;


    @Override
    public void subscribe(EventDTO eventDTO) {
        WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "weixin/subscribe");
        log.info("管家url:{}", webTarget.getUri());
        Response response = webTarget.request()
                .header(FxgjDBConstant.LOGTOKEN,StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .post(Entity.entity(eventDTO, MediaType.APPLICATION_JSON_TYPE));
        log.debug("{},{}", response.getStatus(), response.readEntity(String.class));
    }

    @Override
    public WeixinAuthorizeUrlDTO getOAuthUrl(WeixinAuthorizeUrlDTO weixinAuthorizeUrlDTO) {
        // 验证消息 消息
        Response response = client.target(payrollProperties.getInsideUrl() + "weixin/getOAuthUrl")
                .request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .post(Entity.entity(weixinAuthorizeUrlDTO, MediaType.APPLICATION_JSON_TYPE));

        weixinAuthorizeUrlDTO = response.readEntity(WeixinAuthorizeUrlDTO.class);
        return weixinAuthorizeUrlDTO;
    }

    /**
     * 验证消息 是否来自微信，
     *
     * @param weixinSignatureDTO
     * @return
     */
    @Override
    public WeixinSignatureDTO checkWechatSignature(WeixinSignatureDTO weixinSignatureDTO) {
        // 验证消息 消息
        Response response = client.target(payrollProperties.getInsideUrl() + "weixin/checkSignature")
                .request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .post(Entity.entity(weixinSignatureDTO, MediaType.APPLICATION_JSON_TYPE));
        log.info("status:{}", response.getStatus());
        WeixinSignatureDTO signatureDTO = response.readEntity(WeixinSignatureDTO.class);

        return signatureDTO;
    }

    /**
     * 微信配置信息
     */
    @Override
    public WeixinCfgResponeDTO getWechatCfg() {
        // 验证消息 消息
        Response response = client.target(payrollProperties.getInsideUrl() + "weixin/getWechatCfg")
                .request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .get();

        WeixinCfgResponeDTO weixinCfgResponeDTO = response.readEntity(WeixinCfgResponeDTO.class);

        return weixinCfgResponeDTO;
    }

    /**
     * 获取网页授权凭证access token
     */
    @Override
    public WeixinOauthTokenResponeDTO getOauth2AccessToken(String code) {
        // 验证消息 消息
        Response response = client.target(payrollProperties.getInsideUrl() + "weixin/Oauth2AccessToken")
                .queryParam("code", code)
                .request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .get();

         WeixinOauthTokenResponeDTO weixinOauthTokenResponeDTO = response.readEntity(WeixinOauthTokenResponeDTO.class);

        return weixinOauthTokenResponeDTO;
    }

    /**
     * 通过网页授权获取用户信息
     */
    @Override
    public WeixinUserInfoResponeDTO getUserInfo(String access_token, String openid) {
        // 验证消息 消息
        Response response = client.target(payrollProperties.getInsideUrl() + "weixin/getUserInfo")
                .queryParam("access_token", access_token)
                .queryParam("openid", openid)
                .request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .get();

        WeixinUserInfoResponeDTO weixinUserInfoResponeDTO = response.readEntity(WeixinUserInfoResponeDTO.class);

        return weixinUserInfoResponeDTO;
    }
    /**
     * JS分享产生分享签名
     */
    @Override
    public WeixinJsapiDTO getJsapiSignature(String url) {
        // 验证消息 消息
        Response response = client.target(payrollProperties.getInsideUrl() + "weixin/getJsapiSignature")
                .queryParam("url", url)
                .request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .get();

        WeixinJsapiDTO weixinJsapiDTO = response.readEntity(WeixinJsapiDTO.class);

        return weixinJsapiDTO;
    }

    @Override
    public MsgCodeLogResponeDTO sendCode(MsgCodeLogRequestDTO msgCodeLogRequestDTO) {
        WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "msgCode/smsCode");
        Response response = webTarget.request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .post(Entity.entity(msgCodeLogRequestDTO, MediaType.APPLICATION_JSON_TYPE));
        MsgCodeLogResponeDTO responeDTO = response.readEntity(MsgCodeLogResponeDTO.class);

        Res100302 res100302 = new Res100302();
        res100302.setCodeId(responeDTO.getCodeId());
        res100302.setCode(responeDTO.getCode());
        return responeDTO;
    }

    @Override
    public void checkPhoneCode(String phone, String code) {
        //验证短信码
        MsgCodeLogCheckRequestDTO dto = new MsgCodeLogCheckRequestDTO();
        dto.setSystemId(0);
        dto.setCheckType(1);
        dto.setBusiType(MsgBuisTypeEnum.SMS_01.getCode());
        dto.setCode(code);
        dto.setMsgMedium(phone);
        WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "msgCode/smsCodeCheck");
        log.info("管家url:{}", webTarget.getUri());
        Response response = webTarget.request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .post(Entity.entity(dto, MediaType.APPLICATION_JSON_TYPE));
        log.debug("{}", response.getStatus());
        if (response.getStatus() == 500) {
            throw new ParamsIllegalException(ErrorConstant.WECHAR_008.getErrorMsg());
        }
        if (response.getStatus() != 200) {
            ErrorDTO errorDTO = response.readEntity(ErrorDTO.class);
            throw new ParamsIllegalException(new ErrorMsg(errorDTO.getErrorCode(), errorDTO.getErrorMsg()));
        }
        MsgCodeLogResponeDTO msgCodeLogResponeDTO = response.readEntity(MsgCodeLogResponeDTO.class);
        if (msgCodeLogResponeDTO.getMsgStatus() != 1) {
            throw new ParamsIllegalException(ErrorConstant.Error0004.getErrorMsg());
        }
    }
}

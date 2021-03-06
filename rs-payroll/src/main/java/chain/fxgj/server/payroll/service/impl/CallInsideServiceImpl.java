package chain.fxgj.server.payroll.service.impl;

import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.constant.PayrollDBConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.MsgCodeLogRequestDTO;
import chain.fxgj.server.payroll.dto.MsgCodeLogResponeDTO;
import chain.fxgj.server.payroll.dto.base.*;
import chain.fxgj.server.payroll.service.CallInsideService;
import chain.fxgj.server.payroll.dto.EventDTO;
import chain.fxgj.server.payroll.dto.response.Res100302;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
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
        log.info("====>管家url:{}", webTarget.getUri());
        Response response = webTarget.request()
                .header(PayrollDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(PayrollDBConstant.LOG_TOKEN)))
                .header("appPartner", StringUtils.trimToEmpty(MDC.get("apppartner")))
                .post(Entity.entity(eventDTO, MediaType.APPLICATION_JSON_TYPE));
        log.debug("====>{},{}", response.getStatus(), response.readEntity(String.class));
    }

    @Override
    public WeixinAuthorizeUrlDTO getOAuthUrl(WeixinAuthorizeUrlDTO weixinAuthorizeUrlDTO) {
        // 验证消息 消息
        Response response = client.target(payrollProperties.getInsideUrl() + "weixin/getOAuthUrl")
                .request()
                .header(PayrollDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(PayrollDBConstant.LOG_TOKEN)))
                .header("appPartner", StringUtils.trimToEmpty(MDC.get("apppartner")))
                .post(Entity.entity(weixinAuthorizeUrlDTO, MediaType.APPLICATION_JSON_TYPE));

        weixinAuthorizeUrlDTO = response.readEntity(WeixinAuthorizeUrlDTO.class);
        return weixinAuthorizeUrlDTO;
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
                .header(PayrollDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(PayrollDBConstant.LOG_TOKEN)))
                .header("appPartner", StringUtils.trimToEmpty(MDC.get("apppartner")))
                .get();

        WeixinOauthTokenResponeDTO weixinOauthTokenResponeDTO = response.readEntity(WeixinOauthTokenResponeDTO.class);

        return weixinOauthTokenResponeDTO;
    }

    @Override
    public MsgCodeLogResponeDTO sendCode(MsgCodeLogRequestDTO msgCodeLogRequestDTO, String clientIp) {
        WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "msgCode/smsCode");
        Response response = webTarget.request()
                .header(PayrollConstants.LOG_TOKEN, StringUtils.trimToEmpty(MDC.get(PayrollConstants.LOG_TOKEN)))
                .header("clientIp", clientIp)
                .post(Entity.entity(msgCodeLogRequestDTO, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() != 200) {
            ErrorDTO errorDTO = response.readEntity(ErrorDTO.class);
            throw new ParamsIllegalException(new ErrorMsg(errorDTO.getErrorCode(), errorDTO.getErrorMsg()));
        }
        MsgCodeLogResponeDTO responeDTO = response.readEntity(MsgCodeLogResponeDTO.class);
        Res100302 res100302 = new Res100302();
        res100302.setCodeId(responeDTO.getCodeId());
        return responeDTO;
    }
}

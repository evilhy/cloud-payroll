package chain.fxgj.server.payroll.service;

import chain.fxgj.server.payroll.dto.EventDTO;
import chain.fxgj.server.payroll.dto.MsgCodeLogRequestDTO;
import chain.fxgj.server.payroll.dto.MsgCodeLogResponeDTO;
import chain.fxgj.server.payroll.dto.base.*;
import core.dto.wechat.WeixinJsapiDTO;
import org.springframework.cache.annotation.Cacheable;

public interface CallInsideService {

    public void subscribe(EventDTO eventDTO);

    WeixinAuthorizeUrlDTO getOAuthUrl(WeixinAuthorizeUrlDTO weixinAuthorizeUrlDTO);

    /**
     * 获取网页授权凭证access token
     * 缓存 有效期为 5分钟
     */
    @Cacheable(value = "weixinOauth2", key = "#root.methodName+':'+#code")
    WeixinOauthTokenResponeDTO getOauth2AccessToken(String code);

    /**
     * 短信验证码
     *
     * @param msgCodeLogRequestDTO
     * @return
     */
    MsgCodeLogResponeDTO sendCode(MsgCodeLogRequestDTO msgCodeLogRequestDTO, String clientIp);

}

package chain.fxgj.server.payroll.service;

import chain.fxgj.core.common.dto.msg.MsgCodeLogRequestDTO;
import chain.fxgj.core.common.dto.msg.MsgCodeLogResponeDTO;
import chain.fxgj.server.payroll.dto.EventDTO;
import chain.fxgj.server.payroll.dto.base.*;
import core.dto.wechat.WeixinJsapiDTO;
import org.springframework.cache.annotation.Cacheable;

public interface CallInsideService {

    public void subscribe(EventDTO eventDTO);

    WeixinAuthorizeUrlDTO getOAuthUrl(WeixinAuthorizeUrlDTO weixinAuthorizeUrlDTO);

    /**
     * 验证消息 是否来自微信，
     * 说明：接口配置信息
     * 微信 公众号  请填写接口配置信息，填写的URL需要正确响应微信发送的Token验证
     *
     * @param weixinSignatureDTO
     * @return
     */
    WeixinSignatureDTO checkWechatSignature(WeixinSignatureDTO weixinSignatureDTO);

    /**
     * 构造网页授权链接
     */
    WeixinCfgResponeDTO getWechatCfg();

    /**
     * 获取网页授权凭证access token
     * 缓存 有效期为 5分钟
     */
    @Cacheable(value = "weixinOauth2", key = "#root.methodName+':'+#code")
    WeixinOauthTokenResponeDTO getOauth2AccessToken(String code);

    /**
     * 通过网页授权获取用户信息
     * access_token 有效期为  7200秒 ，2个小时，
     * 如果在 2个小时内，获取用户信息，从缓存中查取
     */
    @Cacheable(value = "weixinOauth2AccessTokenOpenid", key = "#root.methodName+':'+#openid")
    WeixinUserInfoResponeDTO getUserInfo(String access_token, String openid);

    /**
     * JS分享产生分享签名
     */
    WeixinJsapiDTO getJsapiSignature(String url);


    /**
     * 短信验证码
     *
     * @param msgCodeLogRequestDTO
     * @return
     */
    MsgCodeLogResponeDTO sendCode(MsgCodeLogRequestDTO msgCodeLogRequestDTO, String clientIp);


    void checkPhoneCode(String phone, String code);
}

package chain.fxgj.server.payroll.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 通过code换取网页授权access_token(结果)
 *
 * @author lius
 * <p>
 **/
@XmlRootElement(name = "WeixinOauthTokenResponeDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WeixinOauthTokenResponeDTO extends WeixinHeaderResponseDTO {
    /**
     * 网页授权接口调用凭证
     */
    @JsonProperty("access_token")
    String accessToken;  //网页授权接口调用凭证,注意：此access_token与基础支持的access_token不同
    /**
     * 接口调用凭证超时时间，单位：秒
     */
    @JsonProperty("expires_in")
    Integer expiresIn;  //接口调用凭证超时时间，单位（秒）
    /**
     * 用户刷新access_token
     */
    @JsonProperty("refresh_token")
    String refreshToken;  //用户刷新access_token
    /**
     * 用户唯一标识
     */
    String openid;  //用户唯一标识，请注意，在未关注公众号时，用户访问公众号的网页，也会产生一个用户和公众号唯一的OpenID
    /**
     * 用户授权的作用域，使用逗号（,）分隔
     */
    String scope;  //用户授权的作用域，使用逗号（,）分隔

}

package chain.fxgj.server.payroll.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

/**
 * @author lius
 * <p>
 **/
@XmlRootElement(name = "WeixinJsapiDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * JS-SDK使用权限签名算法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeixinJsapiDTO {
    /**
     * 公众号用于调用微信JS接口的临时票据
     */
    @JsonProperty("jsapi_ticket")
    String jsapiTicket;  //公众号用于调用微信JS接口的临时票据
    /**
     * 有效时间
     */
    @JsonProperty("expires_in")
    Integer expiresIn;
    /**
     * appid
     */
    String appid;
    /**
     * 当前网页的URL，不包含#及其后面部分（前端传值）
     */
    String url;
    /**
     * 随机字符串（后台生成）
     */
    String noncestr;
    /**
     * 时间戳（后台生成）
     */
    String timestamp;
    /**
     * 签名算法
     */
    String signature;
    /**
     * 跳转url
     */
    String redirectUrl;


    public WeixinJsapiDTO(String jsapiTicket, String url, String appid, Integer expiresIn) {
        this.jsapiTicket = jsapiTicket;
        this.url = url;
        this.appid = appid;
        this.expiresIn = expiresIn;
        this.noncestr = UUID.randomUUID().toString();
        this.timestamp = Long.toString(System.currentTimeMillis() / 1000);

    }

}

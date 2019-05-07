package chain.fxgj.server.payroll.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author lius
 **/
@XmlRootElement(name = "WeixinAuthorizeUrlDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeixinAuthorizeUrlDTO {
    /**
     * 第三方用户唯一凭证
     */
    String appid;
    /**
     * 请求url
     */
    String url;
    /**
     * 构造网页授权链接
     */
    String authorizeurl;
    /**
     * 重定向后会带上state参数
     */
    String state;

    public WeixinAuthorizeUrlDTO(String appid, String url) {
        this.appid = appid;
        this.url = url;
    }

}

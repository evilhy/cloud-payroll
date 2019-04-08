package chain.fxgj.server.payroll.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author lius
 * <p>
 **/
@XmlRootElement(name = "WeixinCfgResponeDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * 微信配置信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WeixinCfgResponeDTO extends WeixinHeaderResponseDTO {

    /**
     * 身份证 跳转URL
     */
    String url;

    /**
     * 推送工资条 跳转URL
     */
    String oauthUrl;

    /**
     * 公众号  menu跳转URL
     */
    String menuUrl;

}

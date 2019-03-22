package chain.fxgj.server.payroll.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author lius
 **/
@XmlRootElement(name = "WeixinSignatureDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@EqualsAndHashCode
public class WeixinSignatureDTO {

    /**
     * 微信加密签名
     * signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     */
    String signature; //微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
    /**
     * 时间戳
     */
    String timestamp;
    /**
     * 随机数
     */
    String nonce;
    /**
     * 随机字符串
     */
    String echostr;
    /**
     * Token验证 是否正确，true=验证通过
     */
    Boolean tokencheck;


    public WeixinSignatureDTO(String signature, String timestamp, String nonce, String echostr) {
        this.signature = signature;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.echostr = echostr;
    }

}

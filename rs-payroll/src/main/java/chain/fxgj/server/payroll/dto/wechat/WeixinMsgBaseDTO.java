package chain.fxgj.server.payroll.dto.wechat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "WeixinMsgBaseDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * 消息基类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeixinMsgBaseDTO {

    /**
     * 开发者微信号
     */
    private String ToUserName;

    /**
     * 发送方帐号（一个OpenID）
     */
    private String FromUserName;

    /**
     * 消息创建时间 （整型）
     */
    private Long CreateTime;

    /**
     * 消息类型  text
     */
    private String MsgType;  //消息类型（text/image/voice/video/shortvideo/location/link）

    /**
     * 消息id，64位整型
     */
    private String MsgId;

}

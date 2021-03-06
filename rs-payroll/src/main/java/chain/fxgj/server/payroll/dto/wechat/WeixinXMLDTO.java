package chain.fxgj.server.payroll.dto.wechat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "xml")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class WeixinXMLDTO {

    @XmlElement(name = "Event")
    String event;
    @XmlElement(name = "MsgType")
    String msgType;
    @XmlElement(name = "Content")
    String content;
    @XmlElement(name = "FromUserName")
    String fromUserName;
    @XmlElement(name = "ToUserName")
    String toUserName;
    @XmlElement(name = "CreateTime")
    String createTime;
    @XmlElement(name = "MsgId")
    String msgId;

}

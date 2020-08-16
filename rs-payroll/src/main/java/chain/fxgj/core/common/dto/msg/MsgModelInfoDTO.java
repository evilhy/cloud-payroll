package chain.fxgj.core.common.dto.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

/**
 * 消息模板
 * <p>
 **/
@XmlRootElement(name = "MsgModelInfoDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsgModelInfoDTO {

    /**
     * 唯一标识
     */
    String id;
    /**
     * 使用系统(0  放薪管家)
     */
    Integer systemId;
    /**
     * 校验业务类型(0  邮箱  1 短信 )
     */
    Integer checkType;
    /**
     * 业务类型(0 忘记密码 1修改邮箱)
     */
    Integer busiType;
    /**
     * 标题
     */
    String title;
    /**
     * 消息模板
     */
    String content;
    /**
     * 微信消息模板
     */
    private String templateId;


    // 切库注释
//    public MsgModelInfoDTO(MsgModelInfo msgModelInfo) {
//        if (msgModelInfo != null) {
//            this.id = msgModelInfo.getId();
//            this.systemId = msgModelInfo.getSystemId().getCode();
//            this.checkType = msgModelInfo.getCheckType().getCode();
//            this.busiType = msgModelInfo.getBusiType().getCode();
//            this.content = msgModelInfo.getContent();
//            this.title = msgModelInfo.getTitle();
//            this.templateId = msgModelInfo.getTemplateId();
//        }
//    }
//
//    public MsgModelInfo createsgModelInfo() {
//
//        MsgModelInfo msgModelInfo = new MsgModelInfo();
//
//        msgModelInfo.setSystemId(SystemIdEnum.values()[this.systemId]);
//        msgModelInfo.setCheckType(MsgCheckTypeEnum.values()[this.checkType]);
//        msgModelInfo.setBusiType(MsgBuisTypeEnum.values()[this.busiType]);
//        msgModelInfo.setTitle(this.title);
//        msgModelInfo.setContent(this.content);
//        msgModelInfo.setCrtDateTime(LocalDateTime.now());
//        msgModelInfo.setUpdDateTime(LocalDateTime.now());
//
//        return msgModelInfo;
//    }

}

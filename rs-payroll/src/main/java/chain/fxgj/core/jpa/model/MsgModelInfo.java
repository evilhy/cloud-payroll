package chain.fxgj.core.jpa.model;

import chain.fxgj.core.common.constant.DictEnums.ModelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.MsgBuisTypeEnum;
import chain.fxgj.core.common.constant.DictEnums.MsgCheckTypeEnum;
import chain.fxgj.core.common.constant.DictEnums.SystemIdEnum;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author lius* create by lius on 2018/09/04 下午13:44
 * <p>
 **/
@Entity
@Table(name = "msg_model_info")
public class MsgModelInfo {
    @Id
    @Column(name = "id", length = 32)
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    private String id; //主键，自动生成
    @Basic
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "system_id", columnDefinition = "char(1) COMMENT '使用系统(0  放薪管家,1厦门分行预警模板)' ")
    private SystemIdEnum systemId;
    @Basic
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "check_type", columnDefinition = "char(1) COMMENT '校验业务类型(0  邮箱  1 短信  2 微信)' ")
    private MsgCheckTypeEnum checkType;
    @Basic
    @Column(name = "busi_type", columnDefinition = "char(2) COMMENT '业务类型(0 忘记密码 1修改邮箱)' ")
    private MsgBuisTypeEnum busiType;   //业务类型(0 忘记密码 1修改邮箱)
    @Basic
    @Column(name = "title")
    private String title;   //标题
    @Basic
    @Column(name = "content", length = 2000)
    private String content;   //消息模板
    @Basic
    @Column(name = "template_id")
    private String templateId;    //微信消息模板
    @Basic
    @Column(name = "crt_date_time", updatable = false, columnDefinition = "datetime(6)")
    private LocalDateTime crtDateTime;  //创建时间
    @Basic
    @Column(name = "upd_date_time", columnDefinition = "datetime(6)")
    private LocalDateTime updDateTime; // 修改时间
    @Basic
    @Column(name = "model_status", columnDefinition = "char(1) COMMENT '模板状态(0停用、1启用)'default \'1\'")
    private ModelStatusEnum modelStatus;
    @Basic
    @Column(name = "msg_warn_id", columnDefinition = "varchar(32) COMMENT '预警模板id'")
    private String msgWarnId;

    public ModelStatusEnum getModelStatus() {
        return modelStatus;
    }

    public void setModelStatus(ModelStatusEnum modelStatus) {
        this.modelStatus = modelStatus;
    }

    public MsgModelInfo() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SystemIdEnum getSystemId() {
        return systemId;
    }

    public void setSystemId(SystemIdEnum systemId) {
        this.systemId = systemId;
    }

    public MsgCheckTypeEnum getCheckType() {
        return checkType;
    }

    public void setCheckType(MsgCheckTypeEnum checkType) {
        this.checkType = checkType;
    }

    public MsgBuisTypeEnum getBusiType() {
        return busiType;
    }

    public void setBusiType(MsgBuisTypeEnum busiType) {
        this.busiType = busiType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCrtDateTime() {
        return crtDateTime;
    }

    public void setCrtDateTime(LocalDateTime crtDateTime) {
        this.crtDateTime = crtDateTime;
    }

    public LocalDateTime getUpdDateTime() {
        return updDateTime;
    }

    public void setUpdDateTime(LocalDateTime updDateTime) {
        this.updDateTime = updDateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getMsgWarnId() {
        return msgWarnId;
    }

    public void setMsgWarnId(String msgWarnId) {
        this.msgWarnId = msgWarnId;
    }

    @Override
    public String toString() {
        return ReflectEntity.toStr(this);
    }
}

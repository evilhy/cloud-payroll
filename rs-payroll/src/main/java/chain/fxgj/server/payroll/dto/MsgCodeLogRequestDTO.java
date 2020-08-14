package chain.fxgj.server.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信,微信 获取接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsgCodeLogRequestDTO {

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
     * 发送消息媒介 （如：邮箱  手机号）
     */
    String msgMedium;

    /**
     * 有效时间（秒）,可以为空
     */
    Integer validTime;

    /**
     * 发送人
     */
    private String sendName;
    /**
     * 职位
     */
    private String position;
    /**
     * 企业
     */
    private String entName;
    /**
     * 机构
     */
    private String groupName;
    /**
     * 金额
     */
    private Double totalAmt;
    /**
     * 代发方案名称
     */
    private String planName;
    /**
     * 消息内容
     */
    private String content;

}

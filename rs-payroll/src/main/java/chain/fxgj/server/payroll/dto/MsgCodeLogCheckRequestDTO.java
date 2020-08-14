package chain.fxgj.server.payroll.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信,微信 验证接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MsgCodeLogCheckRequestDTO {

    /**
     * 指令序号
     */
    String codeId;

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
     * 消息码
     */
    String code;

    /**
     * 验证通过后的访问码
     */
    String accessCode;
    /**
     * 发送消息媒介 （如：邮箱  手机号）
     */
    private String msgMedium;

}
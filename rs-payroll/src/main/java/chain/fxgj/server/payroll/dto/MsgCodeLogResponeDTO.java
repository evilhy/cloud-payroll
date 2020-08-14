package chain.fxgj.server.payroll.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * 短信 ，微信 请求响应接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MsgCodeLogResponeDTO {

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
     * 发送消息媒介 （如：邮箱  手机号）
     */
    String msgMedium;

    /**
     * 消息码
     */
    String code;

    /**
     * 验证通过后的访问码
     */
    String access_code;

    /**
     * 状态(0-有效 1-已验证 2-已过期)
     */
    Integer msgStatus;

    /**
     * 有效时间（秒）
     */
    Integer validTime;

    /**
     * 验证时间
     */
    private Long invidDateTime;


    /**
     * 验证时间
     */
    private String errorCode;

    /**
     * 验证时间
     */
    private String errorMsg;

}

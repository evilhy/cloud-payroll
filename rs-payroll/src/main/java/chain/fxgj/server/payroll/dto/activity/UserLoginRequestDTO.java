package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 红包配置用户登录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDTO {
    /**
     * 手机号
     */
    private String phone;
    /**
     * 验证码id
     */
    private String smsCodeId;
    /**
     * 验证码
     */
    private String smsCode;

}

package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * 重置查询密码
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPwdDTO {
    /**
     * 信息ID
     */
    private String codeId;
    /**
     * 验证码
     */
    private String code;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 查询密码
     */
    private String pwd;
}

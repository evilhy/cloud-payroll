package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * Req100701
 * */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Req100701 {

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
     * 身份证号
     */
    private String idNumber;
    /**
     * 查询密码
     */
    private String pwd;
}

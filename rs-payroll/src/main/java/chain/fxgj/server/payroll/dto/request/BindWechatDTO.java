package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * 微信绑定
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindWechatDTO {
    /**
     * openId
     */
    private String openId;
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

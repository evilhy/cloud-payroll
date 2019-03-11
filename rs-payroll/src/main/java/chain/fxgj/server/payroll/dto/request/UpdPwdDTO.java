package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * 修改查询密码
 * */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdPwdDTO {
    /**
     * 原密码
     */
    private String oldPwd;
    /**
     * 密码
     */
    private String pwd;

}

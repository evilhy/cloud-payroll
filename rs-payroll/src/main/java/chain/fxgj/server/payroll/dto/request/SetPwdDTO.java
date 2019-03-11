package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * 设置查询密码
 * */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetPwdDTO {
    /**
     * wechatId
     */
    private String wechatId;
    /**
     * 密码
     */
    private String pwd;

}

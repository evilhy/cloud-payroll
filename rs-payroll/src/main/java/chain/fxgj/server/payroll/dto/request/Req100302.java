package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * Req100302
 * */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Req100302 {

    /**
     * 手机号
     */
    private String phone;
    /**
     * 业务类型 0身份验证
     */
    private String busiType;

}

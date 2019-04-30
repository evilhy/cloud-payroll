package chain.fxgj.server.payroll.dto.response;

import lombok.*;

/**
 * 企业超管
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntUserDTO {
    /**
     * 姓名
     */
    private String name;
    /**
     * 职位
     */
    private String position;
    /**
     * 手机号
     */
    private String phone;
}

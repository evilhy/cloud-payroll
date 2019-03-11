package chain.fxgj.server.payroll.dto.response;

import lombok.*;

/**
 * Res100709
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Res100709 {
    /**
     * openId
     */
    private String openId;
    /**
     * 是否绑定 1已绑定 0未绑定
     */
    private String bindStatus;

}

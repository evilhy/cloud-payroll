package chain.fxgj.server.payroll.dto.activity;

import lombok.*;

import java.math.BigDecimal;

/**
 * 红包雨活动返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ActivityRainRetDTO extends ActivityInfoDTO {
    /**
     * 活动id
     */
    private String id;

}

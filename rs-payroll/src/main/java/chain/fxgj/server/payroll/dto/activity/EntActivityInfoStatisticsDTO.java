package chain.fxgj.server.payroll.dto.activity;

import lombok.*;

import java.math.BigDecimal;

/**
 * 企业活动(统计)信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EntActivityInfoStatisticsDTO {
    /**
     * 企业id
     */
    private String entId;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 活动类型 0红包雨 1随机红包 2答题
     */
    private Integer activityTypeCode;
    /**
     * 活动类型 0红包雨 1随机红包 2答题
     */
    private String activityTypeDesc;
    /**
     * 实发金额
     */
    private BigDecimal realAmt;
}

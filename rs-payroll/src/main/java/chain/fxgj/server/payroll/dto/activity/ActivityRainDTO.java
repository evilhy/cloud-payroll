package chain.fxgj.server.payroll.dto.activity;

import lombok.*;

import java.math.BigDecimal;

/**
 * 红包雨活动
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ActivityRainDTO extends ActivityInfoDTO {
    /**
     * 活动数据
     */
    private ActivityRainDetail activityRainDetail;


    /**
     * 红包雨活动
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityRainDetail {
        /**
         * id
         */
        private String id;
        /**
         * 中奖率
         */
        private Double rate;
        /**
         * 最小金额
         */
        private BigDecimal minAmt;
        /**
         * 最大金额
         */
        private BigDecimal maxAmt;

    }

}

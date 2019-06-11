package chain.fxgj.server.payroll.dto.tfinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预约列表
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class IntentListDTO {
    /**
     * 系统当前时间
     */
    private Long nowDate;
    /**
     * 当前预约人数
     */
    @Builder.Default
    private Integer intentNum = 0;
    /**
     * 条数
     */
    @Builder.Default
    private Integer size = 0;
    /**
     * 列表
     */
    private List<IntentRealDTO> list;

    /**
     * 实时预约明细
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IntentRealDTO {
        /**
         * 姓名
         */
        private String clientName;
        /**
         * 预约额度
         */
        private BigDecimal intentAmount;
        /**
         * 到期收益
         */
        private BigDecimal profit;
        /**
         * 预约时间
         */
        private Long crtDateTime;
    }
}

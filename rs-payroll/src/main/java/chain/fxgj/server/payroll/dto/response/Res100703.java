package chain.fxgj.server.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Res100703
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Res100703 {
    /**
     * 应发总金额
     */
    private BigDecimal shouldTotalAmt;
    /**
     * 扣除总金额
     */
    private BigDecimal deductTotalAmt;
    /**
     * 实发总金额
     */
    private BigDecimal realTotalAmt;
    /**
     * 年份
     */
    private List<Integer> years;
    /**
     * planList
     */
    private List<PlanListBean> planList;
    /**
     * employeeSid
     */
    private String employeeSid;


    /**
     * planList
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlanListBean {
        /**
         * 方案ID
         */
        private String wageSheetId;
        /**
         * 方案名称
         */
        private String spName;
        /**
         * 资金类型
         */
        private String fundType;
        /**
         * 代发四大类型
         */
        private Integer spTypeIcon;
        /**
         * 实发金额
         */
        private BigDecimal totalAmt;
        /**
         * 方案时间
         */
        private Long createDateTime;
        /**
         * 员工id
         */
        private String employeeId;
        /**
         * 支付状态 0未 1已到账 2部分到账
         */
        private String payStatus;
        /**
         * 到账笔数
         */
        private Integer payCnt;

        public PlanListBean(String wageSheetId, BigDecimal totalAmt) {
            this.wageSheetId = wageSheetId;
            this.totalAmt = totalAmt;
        }

    }
}

package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Res100712
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class Res100712 {
    /**
     * 年份
     */
    private List<String> years;
    /**
     * 数据
     */
    private List<DataListBean> dataList;


    /**
     * 数据
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataListBean {
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
         * 推送日期
         */
        private Long pushDateTime;
        /**
         * 月份
         */
        private String month;

    }
}

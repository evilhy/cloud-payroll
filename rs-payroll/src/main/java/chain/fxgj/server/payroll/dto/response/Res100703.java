package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
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
     * 年份  Integer类型
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

}

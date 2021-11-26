package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanListBean {
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

    /**
     * 资金所属日期
     */
    private Integer fundDate;

    /**
     * 资金所属日期(描述)
     */
    private String fundDateDesc;

    public PlanListBean(String wageSheetId, BigDecimal totalAmt) {
        this.wageSheetId = wageSheetId;
        this.totalAmt = totalAmt;
    }

}
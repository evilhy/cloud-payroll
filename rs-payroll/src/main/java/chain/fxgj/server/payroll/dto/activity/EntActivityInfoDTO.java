package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 企业活动(基本)信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unchecked")
public class EntActivityInfoDTO {
    /**
     * 企业id
     */
    private String entId;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 活动ID, 不为空则有活动，为空则无活动
     */
    private String activityId;
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 活动类型 0红包雨 1随机红包 2答题
     */
    private Integer activityTypeCode;
    /**
     * 活动类型 0红包雨 1随机红包 2答题
     */
    private String activityTypeDesc;
    /**
     * 活动状态 1待审核 2审核拒绝 3待开启4 进行中5 已结束
     */
    private Integer activityStatusCode;
    /**
     * 活动状态 1待审核 2审核拒绝 3待开启4 进行中5 已结束
     */
    private String activityStatusDesc;
    /**
     * 预算金额
     */
    private BigDecimal budgetAmt;
    /**
     * 活动实发金额
     */
    private BigDecimal activityRealAmt;
    /**
     * 实发金额
     */
    private BigDecimal prizeRealAmt;
    /**
     * 资金状态
     */
    private String prizeDescribe;
    /**
     * 服务器时间
     */
    private long startDateTime;
    /**
     * 活动结束时间
     */
    private long endDateTime;
    /**
     * 开奖时间
     */
    private long lotteryDateTime;
}

package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 活动列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityListDTO {
    /**
     * id
     */
    private String id;
    /**
     * 活动类型 0红包雨 1随机红包 2答题
     */
    private Integer activityType;
    /**
     * 活动状态 1待审核 2审核拒绝 3待开启4 进行中5 已结束
     */
    private Integer activityStatus;
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 活动欢迎语
     */
    private String activitySpeech;
    /**
     * 预算金额
     */
    private BigDecimal budgetAmt;
    /**
     * 实发金额
     */
    private BigDecimal realAmt;
    /**
     * 参加人数
     */
    private Integer totalCnt;
    /**
     * 中奖人数
     */
    private Integer realCnt;
    /**
     * 红包个数/题目个数
     */
    private Integer num;
    /**
     * 活动时长
     */
    private Integer duration;
    /**
     * 是否定时开始 0不定时立即 1定时
     */
    private Integer isTimeOpen;
    /**
     * 开始时间
     */
    private Long startDateTime;
    /**
     * 结束时间
     */
    private Long endDateTime;
    /**
     * 发放成功人数
     */
    private Integer successCnt;
    /**
     * 发放失败人数
     */
    private Integer failCnt;
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
    /**
     * 活动操作流程
     */
    private List<ActivityFlow> activityFlowList;


    /**
     * 活动流程
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityFlow {
        /**
         * 时间
         */
        private Long crtDateTime;
        /**
         * 流程 0提交成功 1审核通过 2审核拒绝 3活动开启 4活动结束
         */
        private Integer activityFlow;
        /**
         * 流程
         */
        private String activityFlowStr;

    }
}

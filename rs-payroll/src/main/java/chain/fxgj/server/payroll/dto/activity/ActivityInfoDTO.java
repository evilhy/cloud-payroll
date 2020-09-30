package chain.fxgj.server.payroll.dto.activity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 红包详情
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityInfoDTO {
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
    private Integer realCnt = 0;
    /**
     * 红包个数/题目个数
     */
    private Integer num = 0;
    /**
     * 活动时长
     */
    private Integer duration = 0;
    /**
     * 是否定时开始 0不定时立即 1定时
     */
    private Integer isTimeOpen = 1;
    /**
     * 开始时间
     */
    private Long startDateTime;
    /**
     * 结束时间
     */
    private Long endDateTime;
    /**
     * 代发账户Id
     */
    private String accountId;
    /**
     * 代发账户
     */
    private String account;
    /**
     * 代发账户余额
     */
    private BigDecimal accountBalance;
    /**
     * 参与机构详情
     */
    private List<EntGroupDTO> groupsList;

}

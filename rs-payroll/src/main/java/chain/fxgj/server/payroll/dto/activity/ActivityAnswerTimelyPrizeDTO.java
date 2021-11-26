package chain.fxgj.server.payroll.dto.activity;

import lombok.*;

import java.math.BigDecimal;

/**
 * 参与（答题）活动
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ActivityAnswerTimelyPrizeDTO {
    /**
     * 活动ID
     */
    private String activityId;
    /**
     * 活动类型
     */
    private Integer activityType;
    /**
     * 所属企业
     */
    private String entId;
    /**
     * 所属机构
     */
    private String groupId;
    /**
     * 员工ID
     */
    private String employeeId;
    /**
     * 员工姓名
     */
    private String employeeName;
    /**
     * 证件号码
     */
    private String idNumber;
    /**
     * 微信openid
     */
    private String openId;
    /**
     * 微信昵称
     */
    private String nickname;
    /**
     * 微信头像
     */
    private String headimgurl;
    /**
     * 是否中奖（ false 否 true 是
     */
    private Boolean isAnswer;
    /**
     * 中奖金额
     */
    private BigDecimal prizeAmt;
    /**
     * 问题答案
     */
    private String answer;
    /**
     * 开奖时间
     */
    private long serverDateTime;
    /**
     * 倒计时(毫秒)
     */
    private long lotteryDateTime;
    /**
     * 所属机构（后台补，前端不上送）
     */
    private String[] groupIds;
    /**
     * 错误码
     */
    private String errorCode;
    /**
     * 错误信息
     */
    private String errorMsg;
}

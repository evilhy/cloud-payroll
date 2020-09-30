package chain.fxgj.server.payroll.dto.activity;

import lombok.*;

import java.math.BigDecimal;

/**
 * （手气红包实时（开奖
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unchecked")
public class ActivityRandomTimelyPrizeDTO {
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
     * 是否中奖（ false 否 true 是）
     */
    private Boolean isRandom;
    /**
     * 中奖金额
     */
    private BigDecimal prizeAmt;
    /**
     * 中奖名称
     */
    private String prizeName;
    /**
     * 中奖等级（-1 未中奖 数字越大 等级越高
     */
    private Integer grade;
    /**
     * 所属机构（后台补，前端不上送）
     */
    private String[] groupIds;
    /**
     * 错误码
     */
    private Integer errcode;
    /**
     * 错误信息
     */
    private String errmsg;
}

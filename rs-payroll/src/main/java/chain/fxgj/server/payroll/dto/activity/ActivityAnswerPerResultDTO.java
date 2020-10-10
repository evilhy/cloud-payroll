package chain.fxgj.server.payroll.dto.activity;

import lombok.*;

import java.math.BigDecimal;

/**
 * 个人（答题）活动(结果查询)
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unchecked")
public class ActivityAnswerPerResultDTO {
    /**
     * 员工姓名
     */
    private String employeeName;
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
    private Boolean isAnswe;
    /**
     * 银行卡号
     */
    private String cardNo;
    /**
     * 开卡银行
     */
    private String issuerName;
    /**
     * 中奖金额
     */
    private BigDecimal prizeAmt;
    /**
     * 中奖时间
     */
    private long prizeDateTime;
    /**
     * 总奖金
     */
    private BigDecimal totleAmt;
    /**
     * 奖金分配人数
     */
    private Integer prizeTotlePer;
    /**
     * 每人分配奖金
     */
    private BigDecimal prizeAmtPer;
    /**
     * 中奖耗时(毫秒
     */
    private long prizeTime;
}

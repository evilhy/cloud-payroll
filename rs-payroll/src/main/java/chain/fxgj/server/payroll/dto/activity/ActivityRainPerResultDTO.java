package chain.fxgj.server.payroll.dto.activity;

import lombok.*;

import java.math.BigDecimal;

/**
 * 个人（红包雨）活动(结果查询)
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unchecked")
public class ActivityRainPerResultDTO {
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
     * 是否中奖（ false 否 true 是
     */
    @Builder.Default
    private Boolean isRain = false;
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
     * 排名
     */
    private Integer rank;
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
     * 中奖耗时(毫秒)
     */
    private long prizeTime;
}

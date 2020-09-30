package chain.fxgj.server.payroll.dto.activity;

import lombok.*;

import java.math.BigDecimal;

/**
 * 个人（手气红包）活动(结果查询)
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AccedeRandomPerResultDTO {
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
    private Boolean isRandom;
    /**
     * 银行卡号
     */
    private String cardNo;
    /**
     * 开卡银行
     */
    private String issuerName;
    /**
     * 中奖时间
     */
    private long prizeDateTime;
    /**
     * 总数量
     */
    private Integer totle;
    /**
     * 已中奖总数量
     */
    private Integer prizeTotle;
    /**
     * 已中奖总人数
     */
    private Integer prizeTotlePer;
    /**
     * 中奖耗时(毫秒)
     */
    private long prizeTime;
    /**
     * 中奖金额
     */
    private BigDecimal prizeAmt;
    /**
     * 中奖名称
     */
    private String prizeName;
}

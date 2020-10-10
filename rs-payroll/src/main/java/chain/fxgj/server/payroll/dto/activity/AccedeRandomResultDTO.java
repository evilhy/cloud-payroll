package chain.fxgj.server.payroll.dto.activity;

import lombok.*;

import java.math.BigDecimal;

/**
 * 列表（手气红包）活动(结果查询)
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AccedeRandomResultDTO {
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
     * 中奖金额
     */
    private BigDecimal prizeAmt;
    /**
     * 中奖时间
     */
    private Long prizeDateTime;
    /**
     * 中奖名称
     */
    private String prizeName;
}

package chain.fxgj.server.payroll.dto.tfinance;

import chain.fxgj.core.jpa.model.BankProductIntention;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;

/**
 * 预约信息
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntentInfoDTO {
    /**
     * 客户经理id
     */
    private String custManagerId;
    /**
     * 客户经理姓名
     */
    private String managerName;
    /**
     * 客户经理手机号
     */
    private String managerPhone;
    /**
     * 预约金额
     */
    private BigDecimal intentAmount;
    /**
     * 认购金额
     */
    private BigDecimal subcribeAmount;
    /**
     * 认购利率
     */
    private Double subcribeRate;
    /**
     * 到期收益
     */
    private BigDecimal profit;
    /**
     * 预约时间
     */
    private Long crtDateTime;
    /**
     * 认购时间
     */
    private Long subcribeDateTime;
    /**
     * 对付时间
     */
    private Long dealDateTime;
    /**
     * 对付天数
     */
    private Long dealDay;
    /**
     * 状态 1=预约成功 2 认购成功
     */
    private Integer status;

    /**
     * 企业id
     */
    private String entId;
    /**
     * 产品信息
     */
    private ProductInfoDTO productInfoDTO;
    /**
     * 预约人数据
     */
    private List<WechatUser> list;


    public IntentInfoDTO(BankProductIntention bankProductIntention) {
        this.custManagerId = bankProductIntention.getCustManagerId();
        this.intentAmount = bankProductIntention.getIntentAmount();
        this.crtDateTime = bankProductIntention.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.subcribeDateTime = bankProductIntention.getSubcribeDateTime() == null ? null : bankProductIntention.getSubcribeDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.status = bankProductIntention.getIntentStatus().getCode();
        this.subcribeAmount = bankProductIntention.getSubcribeAmount();
        this.subcribeRate = bankProductIntention.getSubcribeRate();
        this.entId = bankProductIntention.getEntId();
    }

    /**
     * 微信用户
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WechatUser {
        /**
         * 用户昵称
         */
        private String nickname;
        /**
         * 用户头像
         */
        private String headimgurl;

    }
}

package chain.fxgj.server.payroll.dto.tfinance;

import chain.fxgj.server.payroll.constant.DictEnums.IsStatusEnum;
import lombok.*;

import java.math.BigDecimal;

/**
 * 用户预约登记
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntentRequestDTO {
    /**
     * 产品Id
     */
    private String productId;
    /**
     * 企业Id
     */
    private String entId;
    /**
     * 客户经理id
     */
    private String custManagerId;
    /**
     * 姓名
     */
    private String clientName;
    /**
     * 身份证
     */
    private String idNumber;
    /**
     * 手机号
     */
    private String clientPhone;
    /**
     * 预约金额
     */
    private BigDecimal intentAmount;
    /**
     * 渠道(0公众号 1分享)
     */
    @Builder.Default
    private String channel = "0";
    /**
     * 分享人id
     */
    private String fxId;
    /**
     * openId
     */
    private String openId;
    /**
     * 是否勾选团购协议
     */
    @Builder.Default
    private Integer protocol = IsStatusEnum.NO.getCode();

}

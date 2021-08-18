package chain.fxgj.server.payroll.dto.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Description:提现记录
 * @Author: du
 * @Date: 2021/7/14 11:21
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawalRecordLogDTO {

    /**
     * 提现记录ID
     */
    private String withdrawalRecordLogId;

    /**
     * 提现台帐ID
     */
    private String withdrawalLedgerId;
    /**
     * 提现金额
     */
    private String transAmount;
    /**
     * 收款人姓名
     */
    private String custName;
    /**
     * 收款卡号
     */
    private String employeeCardNo;
    /**
     * 收款卡号带
     */
    private String employeeCardStar;
    /**
     * 收款卡开卡银行
     */
    private String openBank;
    /**
     * 申请提现时间
     */
    private Long applyDateTime;
    /**
     * 预计到帐时间
     */
    private Long predictDateTime;
    /**
     * 完成时间
     */
    private Long payDateTime;
    /**
     * 交易流水号
     */
    private String transNo;
    /**
     * 交易状态 （0：成功、1：处理中 2：失败）
     */
    private Integer transStatus;
    /**
     * 交易状态 （0：成功、1：处理中 2：失败）
     */
    private String transStatusVal;
    /**
     * 失败原因
     */
    private String failDesc;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private Long crtDateTime;
    /**
     * 修改时间
     */
    private Long updDateTime;
    /**
     * 删除状态(0:正常、1:失败)
     */
    private Integer delStatus;
    /**
     * 删除状态     描述
     */
    private String delStatusVal;
    /**
     * salt
     */
    private String salt;

    /**
     * password
     */
    private String passwd;
}

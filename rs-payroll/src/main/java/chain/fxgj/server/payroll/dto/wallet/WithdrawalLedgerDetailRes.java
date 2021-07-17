package chain.fxgj.server.payroll.dto.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @Description:提现台账详情
 * @Author: du
 * @Date: 2021/7/14 18:09
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawalLedgerDetailRes {

    /**
     * 提现台帐ID
     */
    private String withdrawalLedgerId;
    /**
     * 企业ID
     */
    private String entId;
    /**
     *是否启用员工提现（0.停用 1.启用）
     */
    private Integer withdrawStatus;
    /**
     *是否启用员工提现  描述
     */
    private String withdrawStatusVal;
    /**
     * 方案名称
     */
    private String wageSheetName;
    /**
     * 资金类型
     */
    private Integer fundType;
    /**
     * 资金类型 描述
     */
    private String fundTypeVal;
    /**
     * 资金月份
     */
    private Integer fundDate;
    /**
     * 资金月份 描述
     */
    private String fundDateVal;
    /**
     * 付款账户ID(企业出帐的帐户)
     */
    private String accountId;
    /**
     * 付款账户号码(企业出帐的帐户)
     */
    private String account;
    /**
     * 付款账户号码带(企业出帐的帐户)
     */
    private String accountStar;
    /**
     * 付款账户名称(企业出帐的帐户)
     */
    private String accountName;
    /**
     * 付款账户开户行(企业出帐的帐户)
     */
    private String accountOpenBank;
    /**
     * 付款方企业名称
     */
    private String entName;
    /**
     * 付款方企业机构名称
     */
    private String groupName;
    /**
     * 收款人姓名
     */
    private String custName;
    /**
     * 收款人身份证号码
     */
    private String idNumber;
    /**
     * 收款金额
     */
    private String transAmount;
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
     * 发放时间
     */
    private Long issueTime;
    /**
     * 完成时间
     */
    private Long payDateTime;
    /**
     * 成功 提现记录ID
     */
    private String withdrawalRecordLogId;
    /**
     * 提现状态（0:待提现、1:提现成功、2:提现失败、3:处理中）
     */
    private Integer withdrawalStatus;
    /**
     * 提现状态  描述
     */
    private String withdrawalStatusVal;
    /**
     * 备注
     */
    private String remark;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;
    /**
     * 创建时间
     */
    private Long crtDateTime;
    /**
     * 修改时间
     */
    private Long updDateTime;
    /**
     * 交易流水号
     */
    private String transNo;
    /**
     * salt
     */
    private String salt;

    /**
     * password
     */
    private String passwd;
}

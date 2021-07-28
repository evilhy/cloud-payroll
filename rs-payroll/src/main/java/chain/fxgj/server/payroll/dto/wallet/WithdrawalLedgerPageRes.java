package chain.fxgj.server.payroll.dto.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Description:提现分页列表
 * @Author: du
 * @Date: 2021/7/14 17:56
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
public class WithdrawalLedgerPageRes {

    /**
     * 提现台帐ID
     */
    private String withdrawalLedgerId;
    /**
     * 企业ID
     */
    private String entId;
    /**
     * 薪资发放组全称
     */
    private String groupName;
    /**
     * 薪资发放组简称（机构简称）
     */
    private String shortGroupName;
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
     * 收款钱包帐号/二类户账号
     */
    private String walletNumber;
    /**
     * 收款卡开卡银行
     */
    private String openBank;
    /**
     * 方案名称
     */
    private String wageSheetName;
    /**
     * 发放时间
     */
    private Long issueTime;
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
     * 完成时间
     */
    private Long payDateTime;
    /**
     * 提现状态（0:待提现、1:提现成功、2:提现失败、3:处理中、4:超时未提现）
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
     * 付款账户状态(0:正常、1:异常)
     */
    private Integer accountStatus;
    /**
     * 付款账户状态 描述
     */
    private String accountStatusVal;
    /**
     * 是否启用员工提现（0.停用 1.启用）
     */
    private Integer withdrawStatus;

    /**
     * 是否启用员工提现  描述
     */
    private String withdrawStatusVal;
    /**
     * 是否收市
     */
    boolean bankClose;
    /**
     * salt
     */
    private String salt;
    /**
     * password
     */
    private String passwd;

    /**
     * 提现方式(0:手动提现、0:自动提现)
     */
    private Integer withdrawalMethod;
    /**
     * 提现方式 描述
     */
    private String withdrawalMethodVal;
    /**
     * 提现截止日期
     */
    private Long cutoffTime;
    /**
     * 系统时间
     */
    private Long systemTime;
}

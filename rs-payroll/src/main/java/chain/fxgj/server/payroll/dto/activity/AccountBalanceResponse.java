package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * 账户余额
 */
@XmlRootElement(name = "AccountBalanceResponse")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse implements Serializable {
    /**
     * 响应编码(如果返回码不是000000，则返回结果不正常)
     */
    private String ReplyCd;//如果返回码不是000000，则返回结果不正常
    /**
     * 响应信息
     */
    private String ReplyText;
    /**
     * 客户号
     */
    private String Cust_no;
    /**
     * 客户名称
     */
    private String Cust_name;
    /**
     * 帐号
     */
    private String account;
    /**
     * 账户名称
     */
    private String Acct_name;
    /**
     * 账户状态
     */
    private String Acct_status;
    /**
     * 账户余额
     */
    private String Acct_bal;
    /**
     * 账户可用余额
     */
    private String Acct_avi_bal;

}

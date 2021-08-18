package chain.fxgj.server.payroll.dto.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Description:确认提现信息
 * @Author: du
 * @Date: 2021/7/15 10:32
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
public class WithdrawalReq {

    /**
     * 提现台帐ID
     */
    private String withdrawalLedgerId;
    /**
     * 银行卡ID
     */
    private String employeeCardId;
    /**
     * 开卡行代码
     */
    private String issuerBankId;
    /**
     * 开卡银行
     */
    private String issuerName;
    /**
     * 银行卡号
     */
    private String cardNo;
}

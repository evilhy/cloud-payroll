package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 企业代发账户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntAccountDTO {
    /**
     * id
     */
    private String id;
    /**
     * 代发账户
     */
    private String account;
    /**
     * 代发账户余额
     */
    private BigDecimal accountBalance;


}

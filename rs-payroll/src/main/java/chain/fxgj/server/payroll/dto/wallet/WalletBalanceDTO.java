package chain.fxgj.server.payroll.dto.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @Description:钱包余额
 * @Author: du
 * @Date: 2021/7/14 17:17
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
public class WalletBalanceDTO {

    /**
     * 总余额
     */
    private String totalAmount;
    /**
     * 可用余额
     */
    private String availableAmount;
    /**
     * 冻结金额
     */
    private String frozenAmount;
    /**
     * salt
     */
    private String salt;
    /**
     * password
     */
    private String passwd;
}

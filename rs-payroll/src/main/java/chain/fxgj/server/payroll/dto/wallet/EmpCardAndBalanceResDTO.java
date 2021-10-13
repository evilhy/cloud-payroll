package chain.fxgj.server.payroll.dto.wallet;

import chain.utils.fxgj.constant.DictEnums.ModelStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * 员工银行卡
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
@XmlRootElement(name = "EmpCardAndBalanceResDTO")
public class EmpCardAndBalanceResDTO {
    /**
     * 银行卡张数
     */
    private Integer cardNum;

    /**
     * 钱包ID
     */
    private String employeeWalletId;

    /**
     * 钱包卡号
     */
    private String walletNumber;

    /**
     * 钱包总余额
     */
    private String balance;

    /**
     * 可用余额
     */
    private String availableAmount;

    /**
     * 冻结金额
     */
    private String frozenAmount;

    /**
     * 近期收入
     */
    private  String recentlyIssuedAmt;

    /**
     *是否启用员工提现（0.停用 1.启用）
     */
    private Integer withdrawStatus;

    /**
     *是否启用员工提现  描述
     */
    private String withdrawStatusVal;

    /**
     * salt
     */
    private String salt;

    /**
     * password
     */
    private String passwd;
    /**
     * 是否认证
     */
    private Boolean isAttest;
    /**
     * 未签约协议数
     */
    private Integer signNumber;
}

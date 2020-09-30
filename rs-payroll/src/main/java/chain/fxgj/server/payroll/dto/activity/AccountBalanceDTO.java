package chain.fxgj.server.payroll.dto.activity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * 账户余额查询
 */
@XmlRootElement(name = "AccountBalanceDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceDTO implements Serializable {
    private static final long serialVersionUID = 6354844858537191703L;
    /**
     * 代发银行账号（卡号）
     */
    private String account = "";

    @Override
    public String toString() {
        return "AccountBalanceDTO{" +
                "account='" + account + '\'' +
                '}';
    }
}

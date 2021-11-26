package chain.fxgj.server.payroll.dto.securities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

/**
 * 投资奖励
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecuritiesInvestmentRewardDTO {

    /**
     * 证券公司名称
     */
    private String companyName;
    /**
     * 月均资产
     */
    private BigDecimal monthlyAverageAssets;
    /**
     * 金豆奖励
     */
    private BigDecimal goldBean;

}

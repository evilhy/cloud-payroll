package chain.fxgj.server.payroll.dto.tfinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 理财产品
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {
    /**
     * 产品id
     */
    private String productId;
    /**
     * 企业id
     */
    private String entId;

}

package chain.fxgj.server.payroll.dto.securities.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 客户信息
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReqRewardDTO {

    /**
     * 客户id
     */
    private String custId;

    /**
     * 经理id
     */
    private String customerId;

}

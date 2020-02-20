package chain.fxgj.server.payroll.dto.securities;

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
public class SecuritiesCustInfoDTO {
    /**
     * 凭证
     */
    private String jsessionId;
    /**
     * 手机号
     */
    private String phone;

}

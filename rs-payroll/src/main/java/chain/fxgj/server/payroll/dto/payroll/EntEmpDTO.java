package chain.fxgj.server.payroll.dto.payroll;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 微信绑定
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class EntEmpDTO {

    /**
     * 身份证号
     */
    private String idNumber;

}

package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * Res100302
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class Res100302 {
    /**
     * codeId
     */
    private String codeId;
    /**
     * code
     */
    private String code;

}

package chain.fxgj.server.payroll.dto.payroll;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 密码校验
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class CheckPwdDTO {

    /**
     * 密码
     */
    private String pwd;

}

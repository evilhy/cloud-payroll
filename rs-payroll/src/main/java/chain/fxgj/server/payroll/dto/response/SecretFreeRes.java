package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 是否免密
 */
@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecretFreeRes {

    /**
     * 是否免密(true免密，false需要输入密码)
     */
    boolean secretFree;

    /**
     * 密码类型 (0无密码、1数字密码、2手势密码)
     */
    Integer pwdType;
}

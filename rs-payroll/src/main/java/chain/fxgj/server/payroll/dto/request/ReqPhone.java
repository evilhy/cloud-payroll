package chain.fxgj.server.payroll.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 手机验证码
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReqPhone {
    /**
     * 信息ID
     */
    private String codeId;
    /**
     * 验证码
     */
    private String code;
    /**
     * 手机号
     */
    private String phone;

}

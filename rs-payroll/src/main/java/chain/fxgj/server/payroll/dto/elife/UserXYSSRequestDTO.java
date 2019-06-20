package chain.fxgj.server.payroll.dto.elife;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * e生活微信用户
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class UserXYSSRequestDTO {
    /**
     * jsessionId
     */
    private String jsessionId;
    /**
     * 身份证号
     */
    private String idNumber;
    /**
     * 手机号
     */
    private String phone;
    /**
     * codeId
     */
    private String codeId;
    /**
     * 验证码
     */
    private String code;
    /**
     * openId
     */
    private String openId;

}

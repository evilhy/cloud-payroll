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
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequestDTO {
    /**
     * openId
     */
    private String openId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;

}

package chain.fxgj.server.payroll.dto.elife;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 微信用户
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class WechatDTO {
    /**
     * openId
     */
    /**
     * 手机号
     */
    private String openId;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 身份证
     */
    private String idNumber;
    /**
     * 姓名
     */
    private String name;

}

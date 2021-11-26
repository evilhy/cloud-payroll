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
@JsonIgnoreProperties(ignoreUnknown = true)
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
    /**
     * 业务类型
     * 0 微信绑定手机号验证(上手的手机号可以为空，根据jsessionId 找到绑定的手机号)
     * 1 明文传输手机号(此时手机号必填)
     * 2 通过企业绑定的手机
     */
    private String busiType;
    /**
     * 机构id
     */
    private String groupId;

}

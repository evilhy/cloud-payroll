package chain.fxgj.server.payroll.dto.securities.response;

import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
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

    /**
     * loginStatus (0未登录，1已登录)
     */
    private Integer loginStatus;

    /**
     * loginStatus (0未登录，1已登录)
     */
    private String loginStatusVal;

    /**
     * 用户id
     */
    private String custId;

    /**
     * 用户openId
     */
    private String openId;

}

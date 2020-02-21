package chain.fxgj.server.payroll.dto.securities.response;

import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 证券活动登录入参
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class SecuritiesLoginDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 客户经理id
     */
    private String customerId;

    /**
     * 推荐人id
     */
    private String invitationId;

    /**
     * 短信验证码
     */
    private String code;

}

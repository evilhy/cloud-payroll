package chain.fxgj.server.payroll.dto.securities.request;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqSecuritiesLoginDTO {

    /**
     * jsessionId
     */
    private String jsessionId;

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
    private String msgCode;

    /**
     * 短信验证码id
     */
    private String msgCodeId;

    /**
     * 证券平台
     */
    private int securitiesPlatform;

    /**
     * 客户id
     */
    private String custId;

}

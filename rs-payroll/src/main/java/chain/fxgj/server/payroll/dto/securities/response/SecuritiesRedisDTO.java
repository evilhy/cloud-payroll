package chain.fxgj.server.payroll.dto.securities.response;

import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 证券登录缓存数据
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class SecuritiesRedisDTO {

    /**
     * jsessionId
     */
    private String jsessionId;

    /**
     * openId
     */
    private String openId;

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
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String headimgurl;

    /**
     * 是否登录(0否，1是)
     */
    private IsStatusEnum loginStatus;

}

package chain.fxgj.server.payroll.dto.securities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotNull;

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
     * loginStatus (0未登录，1已登录)
     */
    private Integer loginStatus;

    /**
     * loginStatus (0未登录，1已登录)
     */
    private String loginStatusVal;

    /**
     * 客户ID YYYYMMDD+7位序列
     */
    private String custId;

    /**
     * 客户参与活动id
     */
    private String custActivityParticId;

    /**
     * 微信用户表id
     */
    private String wxUserId;
    /**
     * 性别
     */
    private String sex;
    /**
     * 区域
     */
    private String country;
    /**
     * 城市
     */
    private String city;

    /**
     * salt
     */
    private String salt;

    /**
     * password
     */
    private String passwd;

}

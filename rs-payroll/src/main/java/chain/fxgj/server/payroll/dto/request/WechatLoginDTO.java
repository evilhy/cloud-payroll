package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * 登录
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WechatLoginDTO {
    /**
     * openId
     */
    private String openId;
    /**
     * jsessionId
     */
    private String jsessionId;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像
     */
    private String headimgurl;
}

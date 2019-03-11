package chain.fxgj.server.payroll.dto.response;

import chain.fxgj.server.payroll.constant.DictEnums.IsStatusEnum;
import lombok.*;


/**
 * Res100705
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Res100705 {
    /**
     * 是否绑定 1已绑定 0未绑定
     */
    private String bindStatus;
    /**
     * 登录凭证
     */
    private String jsessionId;
    /**
     * 身份证号
     */
    private String idNumber;
    /**
     * 是否设置查询密码 1是 0否
     */
    @Builder.Default
    private Integer ifPwd = IsStatusEnum.NO.getCode();
    /**
     * 头像
     */
    private String headimgurl;
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;


    public Res100705(String jsessionId, String bindStatus) {
        this.jsessionId = jsessionId;
        this.bindStatus = bindStatus;
    }

}

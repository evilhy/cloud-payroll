package chain.fxgj.server.payroll.dto.response;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.server.payroll.constant.DictEnums.IsStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown=true)
public class Res100705 {
    /**
     * 是否绑定 1已绑定 0未绑定
     */
    @Builder.Default
    private String bindStatus = "0";
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
    /**
     * 合作方编号
     */
    private AppPartnerEnum apppartner;
    /**
     * 合作方描述
     */
    private String apppartnerDesc;

    /**
     * salt
     */
    private String salt;

    /**
     * password
     */
    private String passwd;



    public Res100705(String jsessionId, String bindStatus) {
        this.jsessionId = jsessionId;
        this.bindStatus = bindStatus;
    }

}

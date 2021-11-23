package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 员工信息
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmpInfoDTO {
    /**
     * 头像
     */
    private String headimgurl;
    /**
     * 姓名
     */
    private String name;
    /**
     * 身份证号
     */
    private String idNumber;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 身份证
     */
    private String idNumberStar;
    /**
     * 手机号
     */
    private String phoneStar;

    /**
     * 银行卡修改是否最新记录（0：已看 1：新，未看）
     */
    private Integer isNew;

    /**
     * 签约信息ID
     */
    String taxSignId;
    /**
     * 认证状态（0：未认证、1：认证中、2：认证失败、3：认证成功）
     */
    Integer attestStatus;
    /**
     * 认证状态描述
     */
    String attestStatusVal;
    /**
     * 是否完成签约 (0:未签约、1：已签约)
     */
    Integer signStatus;
    /**
     * 是否完成签约描述
     */
    String signStatusVal;
}

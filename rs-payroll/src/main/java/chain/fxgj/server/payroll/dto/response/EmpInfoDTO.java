package chain.fxgj.server.payroll.dto.response;

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

}

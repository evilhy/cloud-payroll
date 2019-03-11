package chain.fxgj.server.payroll.dto.elife;

import chain.fxgj.server.payroll.constant.DictEnums.IsStatusEnum;
import lombok.*;

/**
 * e生活微信用户
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserXYSSDTO {
    /**
     * jsessionId
     */
    private String jsessionId;
    /**
     * 姓名
     */
    @Builder.Default
    private String name = "匿名用户";
    /**
     * 身份证号
     */
    private String idNumber;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 是否已参与 0否 1是
     */
    @Builder.Default
    private Integer join = IsStatusEnum.NO.getCode();
    /**
     * 活动是否结束 0否 1是
     */
    @Builder.Default
    private Integer end = IsStatusEnum.NO.getCode();

}

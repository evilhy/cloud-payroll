package chain.fxgj.server.payroll.dto.tfinance;

import chain.fxgj.server.payroll.constant.DictEnums.IsStatusEnum;
import lombok.*;

/**
 * 预约人信息
 * */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    /**
     * 姓名
     */
    private String clientName;
    /**
     * 身份证
     */
    private String idNumber;
    /**
     * 身份证*
     */
    private String idNumberStar;
    /**
     * 手机号
     */
    private String clientPhone;
    /**
     * 手机号*
     */
    private String clientPhoneStar;
    /**
     * 客户经理Id
     */
    private String custManagerId;
    /**
     * 客户经理姓名
     */
    private String managerName;
    /**
     * 客户经理手机号
     */
    private String managerPhone;
    /**
     * 客户经理图像
     */
    private String managerImg;
    /**
     * 是否有华夏银行卡 0否 1是
     */
    @Builder.Default
    private Integer hxBank = IsStatusEnum.NO.getCode();
    /**
     * 当前时间
     */
    private Long nowDate;

}

package chain.fxgj.server.payroll.dto.elife;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown=true)
public class UserResponeDTO {
    /**
     * jsessionId
     */
    private String jsessionId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 客户经理姓名
     */
    private String managerName;
    /**
     * 客户经理手机号
     */
    private String mobile;
    /**
     * 分行机构号
     */
    private String branchOrgNo;
    /**
     * 分行名称
     */
    private String branchOrgName;
    /**
     * 吸存码
     */
    private String officer;

}

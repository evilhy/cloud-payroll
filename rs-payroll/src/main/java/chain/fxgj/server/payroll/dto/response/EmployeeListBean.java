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
@JsonIgnoreProperties(ignoreUnknown=true)
public class EmployeeListBean {
    /**
     * 手机号码（发验证码用）
     */
    private String phone;
    /**
     * 手机号码（星号遮蔽）
     */
    private String phoneStar;
    /**
     * 姓名
     */
    private String employeeName;
    /**
     * 女士、先生
     */
    private String sex;
    /**
     * 身份证
     */
    private String idNumber;
    /**
     * 企业名
     */
    private String entName;
    /**
     * 企业编号
     */
    private String entId;
    /**
     * 机构id
     */
    private String groupId;

    /**
     * salt
     */
    private String salt;

    /**
     * password
     */
    private String passwd;

}
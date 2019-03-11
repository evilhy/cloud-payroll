package chain.fxgj.server.payroll.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Res100701
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Res100701 {
    /**
     * 是否绑定 1已绑定 0未绑定
     */
    private String bindStatus;
    /**
     * 员工信息列表(身份证号为绑定时显示)
     */
    @Builder.Default
    private List<EmployeeListBean> employeeList = new ArrayList<>();

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
    public static class EmployeeListBean {
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

    }
}

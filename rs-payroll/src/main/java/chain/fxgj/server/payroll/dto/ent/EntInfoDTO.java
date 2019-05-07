package chain.fxgj.server.payroll.dto.ent;

import lombok.*;

import java.util.HashMap;
import java.util.LinkedList;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntInfoDTO {
    /**
     * 企业id
     */
    private String entId;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 企业简称
     */
    private String shortEntName;
    /**
     * 员工在职状态是否[在职]（ true[在职]   false ）
     */
    private Boolean empEntInservice;
    /**
     * 机构信息
     */
    @Builder.Default
    private HashMap<String, GroupInfo> groupInfoMap = new HashMap<String, GroupInfo>();
    /**
     * 机构信息
     */
    @Builder.Default
    private LinkedList<GroupInfo> groupInfoList = new LinkedList<GroupInfo>();

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfo {
        /**
         * 机构id
         */
        private String groupId;
        /**
         * 机构名称
         */
        private String groupName;
        /**
         * 机构机构
         */
        private String groupShortName;
        /**
         * 员工在职状态是否[在职]（ true[在职]   false ）
         */
        private Boolean empGroupInservice;
        /**
         * 员工信息
         */
        EmployeeInfo employeeInfo;


        @Getter
        @Setter
        @EqualsAndHashCode
        @ToString
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EmployeeInfo {
            /**
             * 员工id
             */
            private String employeeId;
            /**
             * 姓名
             */
            private String employeeName;
            /**
             * 在职状态
             */
            private Integer employeeStatus;
            /**
             * 在职描述
             */
            private String employeeStatusDesc;
            /**
             * 手机号
             */
            private String phone;
            /**
             * 入职时间
             */
            private Long entryDate;
            /**
             * 职位
             */
            private String position;
            /**
             * 员工工号
             */
            private String employeeNo;
        }
    }
}

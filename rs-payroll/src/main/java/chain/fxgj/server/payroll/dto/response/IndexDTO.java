package chain.fxgj.server.payroll.dto.response;

import chain.fxgj.server.payroll.dto.EmployeeDTO;
import lombok.*;

/**
 * index
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexDTO {
    /**
     * 最新企业
     */
    private DataListBean bean;

    /**
     * 银行卡修改是否最新记录（0：已看 1：新，未看）
     */
    private Integer isNew;

    /**
     * 最新企业
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataListBean {
        /**
         * 企业id
         */
        private String entId;
        /**
         * 企业
         */
        private String entName;
        /**
         * 机构
         */
        private String groupId;
        /**
         * 机构名称
         */
        private String groupName;
        /**
         * 机构简称
         */
        private String groupShortName;
        /**
         * 最新一笔日期
         */
        private Long createDate;
        /**
         * 状态
         */
        private String inServiceStatus;
        /**
         * 是否已读 0否 1是
         */
        private String isRead;


        public DataListBean(EmployeeDTO employeeDTO) {
            this.entId = employeeDTO.getEntId();
            this.groupId = employeeDTO.getGroupId();
            this.entName = employeeDTO.getEntName();
            this.groupName = employeeDTO.getGroupName();
            this.inServiceStatus = employeeDTO.getEmpStatus();
            this.groupShortName = employeeDTO.getGroupShortName();
        }

    }
}

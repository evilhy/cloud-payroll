package chain.fxgj.server.payroll.dto.response;

import chain.fxgj.server.payroll.dto.EmployeeDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 最新企业机构代发记录
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewestWageLogDTO {
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
     * 员工在职状态
     */
    private String inServiceStatus;
    /**
     * 是否已读 0否 1是
     */
    private String isRead;


    public NewestWageLogDTO(EmployeeDTO employeeDTO) {
        this.entId = employeeDTO.getEntId();
        this.groupId = employeeDTO.getGroupId();
        this.entName = employeeDTO.getEntName();
        this.groupName = employeeDTO.getGroupName();
        this.inServiceStatus = employeeDTO.getEmpStatus();
        this.groupShortName = employeeDTO.getGroupShortName();
    }

}
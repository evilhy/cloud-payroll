package chain.fxgj.server.payroll.dto;

import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.utils.fxgj.constant.DictEnums.FundLiquidationEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class EmployeeDTO {
    /**
     * 员工id
     */
    private String employeeId;
    /**
     * 姓名
     */
    private String employeeName;
    /**
     * 身份证
     */
    private String idNumberStar;
    /**
     * 手机号
     */
    private String phoneStar;
    /**
     * 职位
     */
    private String position;
    /**
     * 状态
     */
    private String empStatus;
    /**
     * 状态
     */
    private String inServiceStatus;
    /**
     * 员工工号
     */
    private String employeeNo;
    /**
     * 入职时间
     */
    private Long entryDate;
    /**
     * 机构id
     */
    private String groupId;
    /**
     * 机构
     */
    private String groupName;
    /**
     * 机构简称
     */
    private String groupShortName;
    /**
     * 企业id
     */
    private String entId;
    /**
     * 企业
     */
    private String entName;
    /**
     * 清算渠道（银行）
     */
    private FundLiquidationEnum liquidation;

    public EmployeeDTO(EntInfoDTO.GroupInfo.EmployeeInfo employeeInfo) {
        this.employeeId = employeeInfo.getEmployeeId();
        this.employeeName = employeeInfo.getEmployeeName();
        this.phoneStar = employeeInfo.getPhone();
        this.position = employeeInfo.getPosition();
        this.empStatus = employeeInfo.getEmployeeStatus() + "";
        this.inServiceStatus = employeeInfo.getEmployeeStatusDesc();
        this.employeeNo = employeeInfo.getEmployeeNo();
        this.entryDate = employeeInfo.getEntryDate();

    }

}

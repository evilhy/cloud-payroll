package chain.fxgj.server.payroll.dto.response;

import chain.fxgj.core.jpa.model.EmployeeCardInfo;
import chain.fxgj.server.payroll.dto.EmployeeDTO;
import lombok.*;

import java.util.List;

/**
 * Res100708
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Res100708 {
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
    private String inServiceStatus;
    /**
     * 状态
     */
    private String inServiceStatusVal;
    /**
     * 员工工号
     */
    private String employeeNo;
    /**
     * 机构
     */
    private String groupName;
    /**
     * 入职时间
     */
    private Long entryDate;
    /**
     * 银行卡
     */
    private List<BankCardListBean> bankCardList;

    public Res100708(EmployeeDTO employeeDTO) {
        this.employeeId = employeeDTO.getEmployeeId();
        this.employeeName = employeeDTO.getEmployeeName();
        this.idNumberStar = employeeDTO.getIdNumberStar();
        this.phoneStar = employeeDTO.getPhoneStar();
        this.position = employeeDTO.getPosition();
        this.inServiceStatus = employeeDTO.getEmpStatus();
        this.inServiceStatusVal = employeeDTO.getInServiceStatus();
        this.employeeNo = employeeDTO.getEmployeeNo();
        this.entryDate = employeeDTO.getEntryDate();
        this.groupName = employeeDTO.getGroupName();
    }

    /**
     * 银行卡
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BankCardListBean {
        /**
         * 开卡银行
         */
        private String bankName;
        /**
         * 卡号
         */
        private String bankCard;

        public BankCardListBean(EmployeeCardInfo employeeCardInfo) {
            this.bankName = employeeCardInfo.getIssuerName();
            this.bankCard = employeeCardInfo.getCardNo();
        }

    }

}

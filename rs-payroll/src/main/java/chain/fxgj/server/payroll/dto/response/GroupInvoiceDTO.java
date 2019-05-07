package chain.fxgj.server.payroll.dto.response;

import chain.fxgj.core.jpa.model.EntGroupInvoiceInfo;
import lombok.*;

/**
 * 企业机构发票信息
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupInvoiceDTO {
    /**
     * 机构发票唯一标识
     */
    String id;
    /**
     * 公司名称
     */
    String groupName;
    /**
     * 公司税号
     */
    String groupTaxNo;
    /**
     * 公司地址
     */
    String groupAddress;
    /**
     * 开户行账号
     */
    String openBankAccount;
    /**
     * 开户行名称
     */
    String openBankName;
    /**
     * 电话区号
     */
    String groupPhonePrefix;
    /**
     * 电话
     */
    String groupPhone;
    /**
     * 普票二维码resource_id
     */
    String entCommonQr;
    /**
     * 专票二维码resource_id
     */
    String entQrPath;
    /**
     * 0=未完善1=已完善
     */
    String flag;


    public GroupInvoiceDTO(EntGroupInvoiceInfo entGroupInvoiceInfo) {
        this.id = entGroupInvoiceInfo.getId();
        this.groupName = entGroupInvoiceInfo.getGroupName();
        this.groupTaxNo = entGroupInvoiceInfo.getGroupTaxNo();
        this.groupAddress = entGroupInvoiceInfo.getGroupAddress();
        this.openBankAccount = entGroupInvoiceInfo.getOpenBankAccount();
        this.openBankName = entGroupInvoiceInfo.getOpenBankName();
        this.groupPhone = entGroupInvoiceInfo.getGroupPhone();
        this.groupPhonePrefix = entGroupInvoiceInfo.getGroupPhonePrefix();
    }


}

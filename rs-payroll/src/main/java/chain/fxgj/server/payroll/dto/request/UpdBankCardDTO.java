package chain.fxgj.server.payroll.dto.request;

import chain.fxgj.server.payroll.dto.response.EmpEntDTO;
import lombok.*;

import java.util.List;

/**
 * 修改银行卡
 * */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdBankCardDTO {
    /**
     * 账号
     */
    private String cardNo;
    /**
     * 开户id
     */
    private String issuerBankId;
    /**
     * 开户行
     */
    private String issuerName;
    /**
     * 银行卡ID
     */
    private List<EmpEntDTO.BankCardGroup> bankCardGroups;


}

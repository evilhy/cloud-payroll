package chain.fxgj.server.payroll.dto.response;

import lombok.*;

import java.util.List;

/**
 * 员工企业
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmpEntDTO {
    /**
     * 企业简称
     */
    private String shortEntName;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 基本信息
     */
    private List<Res100708> items;
    /**
     * 银行卡
     */
    private List<BankCard> cards;

}

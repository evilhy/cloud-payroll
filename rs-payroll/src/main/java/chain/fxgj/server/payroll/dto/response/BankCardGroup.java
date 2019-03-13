package chain.fxgj.server.payroll.dto.response;

import lombok.*;

/**
 * 银行卡所属机构
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankCardGroup {
    /**
     * id
     */
    private String id;
    /**
     * 机构id
     */
    private String groupId;
    /**
     * 机构名称
     */
    private String shortGroupName;
}
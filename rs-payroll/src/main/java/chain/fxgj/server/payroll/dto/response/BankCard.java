package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankCard {
    /**
     * 账号
     */
    private String cardNo;
    /**
     * 修改之前账号
     */
    private String oldCardNo;
    /**
     * 开户行
     */
    private String issuerName;
    /**
     * 银行卡所属机构
     */
    private List<BankCardGroup> bankCardGroups;
    /**
     * 银行卡修改状态
     */
    private Integer cardUpdStatus;
    /**
     * 银行卡修改状态描述
     */
    private String cardUpdStatusVal;
    /**
     * 银行卡修改被拒原因
     */
    private String updDesc;
    /**
     * 银行卡修改是否最新记录（0：已看 1：新，未看）
     */
    private Integer isNew;
}
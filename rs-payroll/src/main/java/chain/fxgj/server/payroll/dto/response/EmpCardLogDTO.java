package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 员工企业银行卡修改记录
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class EmpCardLogDTO {
    /**
     * 时间
     */
    private Long crtDateTime;
    /**
     * 银行卡号
     */
    private String cardNo;
    /**
     * 开卡银行
     */
    private String issuerName;
    /**
     * 银行卡号，原
     */
    private String cardNoOld;
    /**
     * 开卡银行，原
     */
    private String issuerNameOld;
    /**
     * 机构名称
     */
    private String shortGroupName;
    /**
     * 修改状态
     */
    private Integer updStatus;
    /**
     * 修改状态描述
     */
    private String updStatusVal;
    /**
     * 修改备注（成功或失败原因）
     */
    private String updDesc;
    /**
     * 修改审核时间
     */
    private Long updDateTime;
    /**
     * 银行卡记录id
     */
    private String logId;

}

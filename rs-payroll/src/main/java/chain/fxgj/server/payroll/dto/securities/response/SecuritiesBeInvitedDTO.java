package chain.fxgj.server.payroll.dto.securities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 被邀请人信息
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class SecuritiesBeInvitedDTO {
    /**
     * 被邀请人手机
     */
    private String beInvitedPhone;
    /**
     * 达标状态(未达标、有效开户)
     */
    private String standardStatus;
    /**
     * 证券平台名称
     */
    private String securitiesName;
    /**
     * 奖励金豆
     */
    private String goldsBean;


}

package chain.fxgj.server.payroll.dto.securities.response;

import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 开户奖励
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class SecuritiesOpenRewardDTO {
    /**
     * 证券公司名称
     */
    private String companyName;
    /**
     * 是否获得奖励(0未获得 1已获得)
     */
    private IsStatusEnum isStatus;


}

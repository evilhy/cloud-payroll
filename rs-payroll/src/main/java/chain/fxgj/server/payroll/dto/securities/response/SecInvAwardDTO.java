package chain.fxgj.server.payroll.dto.securities.response;

import chain.wisales.core.constant.dictEnum.SecuritiesPlatformEnum;
import chain.wisales.core.constant.dictEnum.StandardEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

/**
 * 证券登录缓存数据
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class SecInvAwardDTO {

    /**
     * 客户id
     */
    private String custId;

    /**
     * 客户活动参与id
     */
    private String custActivityParticId;

    /**
     * 手机号
     */
    private String phoneNo;

    /**
     * 达标状态 (0未达标、1有效开户)
     */
    private StandardEnum standard;

    /**
     * 证券平台
     */
    private SecuritiesPlatformEnum securitiesPlatform;

    /**
     * 金豆奖励
     */
    private BigDecimal goldenBean;

}

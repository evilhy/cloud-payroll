package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * index
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexDTO {
    /**
     * 最新企业
     */
    private NewestWageLogDTO bean;
    /**
     * 银行卡修改是否最新记录（0：已看 1：新，未看）
     */
    private Integer isNew;

}

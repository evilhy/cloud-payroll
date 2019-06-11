package chain.fxgj.server.payroll.dto.tfinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 操作记录
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class BrowseRequestDTO {
    /**
     * 产品id
     */
    private String productId;
    /**
     * 企业id
     */
    private String entId;
    /**
     * openId
     */
    private String openId;
    /**
     * 渠道(0公众号 1分享)
     */
    @Builder.Default
    private String channel = "0";
    /**
     * 分享人id
     */
    private String fxId;


}

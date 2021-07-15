package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/7/15 13:52
 */
@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckCardBinRes {

    /**
     * 发卡机构代码
     */
    private String issuerCode;
    /**
     * 发卡行名称(简称)
     */
    private String issuerName;
    /**
     * 发卡行全称
     */
    private String issuerFullName;
    /**
     * 卡名称
     */
    private String cardName;
    /**
     * 主账号长度（一般13-19位)
     */
    private Integer cardNoLen;
    /**
     * 卡bin号
     */
    private String binNum;
}

package chain.fxgj.server.payroll.dto.securities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 证券活动登录入参
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResSecuritiesLoginDTO {

    /**
     * 客户id
     */
    private String custId;

    /**
     * loginStatus (0未登录，1已登录)
     */
    private Integer loginStatus;


}

package chain.fxgj.server.payroll.dto.nj;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * post 请求包装
 */
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NjReqDTO {

    /**
     * 登录凭证
     */
    private String accessToken;

}

package chain.fxgj.server.payroll.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * app获取用户信息请求参数
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqMiniInfo {

    /**
     * 签名
     */
    private String signature;
    /**
     * 时间戳
     */
    private Integer timestamp;
    /**
     * 加密数据
     */
    private String encryptedData;
    /**
     * 凭证
     */
    private String jsessionId;

}

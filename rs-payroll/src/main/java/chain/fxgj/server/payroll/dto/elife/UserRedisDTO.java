package chain.fxgj.server.payroll.dto.elife;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

/**
 * e生活微信用户
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRedisDTO {
    /**
     * openId
     */
    private String openId;
    /**
     * 时间
     */
    private LocalDateTime time;

}

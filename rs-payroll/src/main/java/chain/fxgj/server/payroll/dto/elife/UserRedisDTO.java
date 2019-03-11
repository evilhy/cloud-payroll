package chain.fxgj.server.payroll.dto.elife;

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

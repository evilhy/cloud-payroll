package chain.fxgj.server.payroll.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDTO {
    /**
     * 开发者微信号
     */
    private String toUserName;
    /**
     * 发送方帐号（一个OpenID）
     */
    private String openId;
    /**
     * 消息创建时间 （整型）
     */
    private String createTime;
    /**
     * 消息类型，event
     */
    private String msgType;
    /**
     * 事件类型，subscribe(订阅)、unsubscribe(取消订阅)
     */
    private String event;

}

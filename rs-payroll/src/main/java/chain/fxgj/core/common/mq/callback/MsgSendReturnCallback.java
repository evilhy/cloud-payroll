package chain.fxgj.core.common.mq.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 消息失败返回
 * <p>
 * 消息进入exchange但未进入queue时会被调用。
 *
 * @author lius
 */
@Slf4j
public class MsgSendReturnCallback implements RabbitTemplate.ReturnCallback {

    /**
     * 当消息从交换机到队列失败时，该方法被调用。（若成功，则不调用）
     *
     * @param message    消息主体
     * @param replyCode  返回码.
     * @param replyText  描述.
     * @param exchange   消息使用的交换器
     * @param routingKey 消息使用的路由键
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        String correlationId = message.getMessageProperties().getCorrelationId();
        log.error("ackMQSender 消息：{} 发送失败, 应答码：{} 原因：{} 交换机: {}  路由键: {}", correlationId, replyCode, replyText, exchange, routingKey);
    }
}

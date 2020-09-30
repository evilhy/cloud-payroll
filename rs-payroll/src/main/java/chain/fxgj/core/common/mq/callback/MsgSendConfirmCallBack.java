package chain.fxgj.core.common.mq.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 消息发送到交换机确认机制
 * <p>
 * 息从生产者到达exchange时返回ack，消息未到达exchange返回nack；
 *
 * @author lius
 */
@Slf4j
public class MsgSendConfirmCallBack implements RabbitTemplate.ConfirmCallback {

    /**
     * 当消息发送到交换机（exchange）时，该方法被调用.
     * <p>
     * 1.如果消息没有到exchange,则 ack=false
     * 2.如果消息到达exchange,则 ack=true
     *
     * @param correlationData 消息唯一标识
     * @param ack             确认结果
     * @param cause           失败原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("MsgSendConfirmCallBack_回调id:{}", correlationData);
        if (ack) {
            //消息发送成功
            log.info("将msg-db数据更新为处理成功");
        } else {
            //消息发送失败   重新发送
            log.info("记录异常日志...，后续会有补偿机制(定时器),{}", cause);
        }
    }

}

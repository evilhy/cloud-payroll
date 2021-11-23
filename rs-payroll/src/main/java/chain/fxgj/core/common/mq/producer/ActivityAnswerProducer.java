package chain.fxgj.core.common.mq.producer;

import chain.activity.dto.mq.AccedeAnswerReq;
import chain.activity.dto.mq.config.ActivityRabbitConfig;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 答题活动-生产者
 *
 * @Author zhuchangjian
 * @Date 2019/12/9 11:01
 **/
@Component
@Slf4j
public class ActivityAnswerProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Boolean sendMessage(AccedeAnswerReq accedeAnswerReq) {
        log.info("ActivityAnswerProducer.accedeAnswerReq:[{}]", JacksonUtil.objectToJson(accedeAnswerReq));
        Boolean bool = false;
        try {
            CorrelationData correlationId = new CorrelationData(accedeAnswerReq.getMessageId());
            rabbitTemplate.convertAndSend(ActivityRabbitConfig.PAYROLL_ANSWER_FANOUT_EXCHANGE, ActivityRabbitConfig.PAYROLL_ANSWER_FANOUT_ROUTING,
                    JacksonUtil.objectToJson(accedeAnswerReq), correlationId);
            bool = true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception[发送时间] - [{}]", e);
        }
        return bool;
    }

}

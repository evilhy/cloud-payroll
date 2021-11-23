package chain.fxgj.core.common.mq.producer;

import chain.activity.dto.mq.AccedeRainReq;
import chain.activity.dto.mq.config.ActivityRabbitConfig;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 红包雨活动-生产者
 *
 * @Author zhuchangjian
 * @Date 2019/12/10 09:13
 **/
@Component
@Slf4j
public class ActivityRainProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Boolean sendMessage(AccedeRainReq accedeRainReq) {
        log.info("ActivityAnswerProducer.rainAnswerReq:[{}]", JacksonUtil.objectToJson(accedeRainReq));
        Boolean bool = false;
        try {
            CorrelationData correlationId = new CorrelationData(accedeRainReq.getMessageId());
            rabbitTemplate.convertAndSend(ActivityRabbitConfig.PAYROLL_RAIN_FANOUT_EXCHANGE, ActivityRabbitConfig.PAYROLL_RAIN_FANOUT_ROUTING,
                    JacksonUtil.objectToJson(accedeRainReq), correlationId);
            bool = true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception[发送时间] - [{}]", e);
        }
        return bool;
    }

}

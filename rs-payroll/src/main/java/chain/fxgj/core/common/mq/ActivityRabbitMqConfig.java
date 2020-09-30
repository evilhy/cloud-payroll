package chain.fxgj.core.common.mq;

import chain.activity.dto.mq.config.ActivityRabbitConfig;
import chain.fxgj.core.common.mq.callback.MsgSendConfirmCallBack;
import chain.fxgj.core.common.mq.callback.MsgSendReturnCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @ClassName ActivityRabbitMqConfig
 * @Author zhuchangjian
 * @Date 2019/12/9 11:26
 **/
@Configuration
@Slf4j
public class ActivityRabbitMqConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;


    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RabbitTemplate rabbitTemplate() {

        CachingConnectionFactory connectionFactory = connectionFactory();

        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        /**若使用confirm-callback或return-callback，
         * 必须要配置publisherConfirms或publisherReturns为true
         * 每个rabbitTemplate只能有一个confirm-callback和return-callback
         */
        rabbitTemplate.setConfirmCallback(msgSendConfirmCallBack());
        rabbitTemplate.setReturnCallback(msgSendReturnCallback());
        /**
         * 消息发送失败返回到队列中
         * 使用return-callback时必须设置mandatory为true，或者在配置中设置mandatory-expression的值为true，
         * 可针对每次请求的消息去确定’mandatory’的boolean值，
         * 只能在提供’return-callback’时使用，与mandatory互斥
         */
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    /**
     * 消息确认机制
     * Confirms给客户端一种轻量级的方式，能够跟踪哪些消息被broker处理，
     * 哪些可能因为broker宕掉或者网络失败的情况而重新发布。
     * 确认并且保证消息被送达，提供了两种方式：发布确认和事务。(两者不可同时使用)
     * 在channel为事务时，不可引入确认模式；同样channel为确认模式下，不可使用事务。
     *
     * @return
     */
    @Bean
    public MsgSendConfirmCallBack msgSendConfirmCallBack() {
        return new MsgSendConfirmCallBack();
    }

    @Bean
    public MsgSendReturnCallback msgSendReturnCallback() {
        return new MsgSendReturnCallback();
    }

    /*********************答题队列设置开始*****************************/

    /**
     * 队列
     *
     * @return
     */
    @Bean
    public Queue answerPreconditionQueueOne() {
        return new Queue(ActivityRabbitConfig.PAYROLL_ANSWER_QUEUE_ONE, true, false, false);
    }

    /**
     * 交换机 (广播方式)
     *
     * @return
     */
    @Bean
    public FanoutExchange answerPreconditionFanoutExchange() {
        FanoutExchange directExchange = new FanoutExchange(ActivityRabbitConfig.PAYROLL_ANSWER_FANOUT_EXCHANGE, true, false);
        return directExchange;
    }

    /**
     * 队列和交换机绑定
     *
     * @return
     */
    @Bean
    public Binding answerPreconditionExchangeQueueBindingOne() {
        return BindingBuilder.bind(answerPreconditionQueueOne()).to(answerPreconditionFanoutExchange());
    }

    /*********************答题队列设置结束*****************************/

    /*********************红包雨队列设置开始*****************************/

    /**
     * 队列
     *
     * @return
     */
    @Bean
    public Queue rainPreconditionQueueOne() {
        return new Queue(ActivityRabbitConfig.PAYROLL_RAIN_QUEUE_ONE, true, false, false);
    }

    /**
     * 交换机 (广播方式)
     *
     * @return
     */
    @Bean
    public FanoutExchange rainPreconditionFanoutExchange() {
        FanoutExchange directExchange = new FanoutExchange(ActivityRabbitConfig.PAYROLL_RAIN_FANOUT_EXCHANGE, true, false);
        return directExchange;
    }

    /**
     * 队列和交换机绑定
     *
     * @return
     */
    @Bean
    public Binding rainPreconditionExchangeQueueBindingOne() {
        return BindingBuilder.bind(rainPreconditionQueueOne()).to(rainPreconditionFanoutExchange());
    }

    /*********************红包雨队列设置结束*****************************/

}

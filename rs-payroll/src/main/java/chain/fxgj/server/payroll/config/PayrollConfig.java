package chain.fxgj.server.payroll.config;

import chain.css.log.filter.TrackLogFilter;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.logging.Level;

/**
 * @author chain
 * create by chain on 2018/8/2 下午4:10
 **/
@Configuration
public class PayrollConfig {


    @Bean("wechatClient")
    public Client wechatClient() throws Exception {
        return ClientBuilder.newBuilder()
                .build()
                .register(new LoggingFeature(null, Level.INFO, LoggingFeature.Verbosity.PAYLOAD_TEXT, 1000));

    }

    @Bean("insideClient")
    public Client insideClient() throws Exception {
        return ClientBuilder.newBuilder()
                .build()
                .register(new LoggingFeature(null, Level.INFO, LoggingFeature.Verbosity.PAYLOAD_TEXT, 100));
    }

    /**
     * 设置最后处理，但是不要太往后，目前底层框架 最后执行的过滤器为 最大-10
     *
     * @return
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE - 5)
    public WebFilter webFilter() {
        return new TrackLogFilter();
    }

}

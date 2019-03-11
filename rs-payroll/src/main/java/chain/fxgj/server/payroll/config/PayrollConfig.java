package chain.fxgj.server.payroll.config;

import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .register(new LoggingFeature(null, Level.INFO, LoggingFeature.Verbosity.PAYLOAD_TEXT, 100));

    }

    @Bean("insideClient")
    public Client insideClient() throws Exception {
        return ClientBuilder.newBuilder()
                .build()
                .register(new LoggingFeature(null, Level.INFO, LoggingFeature.Verbosity.PAYLOAD_TEXT, 100));
    }
}

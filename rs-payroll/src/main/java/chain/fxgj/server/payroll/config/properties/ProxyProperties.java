package chain.fxgj.server.payroll.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description:正向代理
 * @Author: du
 * @Date: 2021/4/12 11:55
 */
@Component
@ConfigurationProperties(prefix = "proxy", ignoreInvalidFields = false)
@Data
public class ProxyProperties {

    private String ip;

    private int port;

    private String schemeName;
}

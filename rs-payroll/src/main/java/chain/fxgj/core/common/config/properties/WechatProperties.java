package chain.fxgj.core.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lius
 * create by
 **/
@Component
@ConfigurationProperties(
        prefix = "wechat", ignoreInvalidFields = true
)
public class WechatProperties {

    String host;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}

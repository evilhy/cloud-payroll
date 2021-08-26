package chain.fxgj.server.payroll.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description:报税服务配置
 * @Author: du
 * @Date: 2021/4/12 11:55
 */
@Component
@ConfigurationProperties(prefix = "tax", ignoreInvalidFields = false)
@Data
public class TaxProperties {

    /**
     * 渠道
     */
    private String channel;
    /**
     * signSalt混淆
     */
    private String signSalt;
    /**
     * 公钥
     */
    private String publicKey;
    /**
     * 测试地址
     */
    private String requestUrl;
    /**
     * 证书名称
     */
    private String certificateName;
    /**
     * 证书密码
     */
    private String password;

}

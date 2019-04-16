package chain.fxgj.server.payroll.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "merchants", ignoreInvalidFields = true)
@Data
public class MerchantsProperties {

    private List<Merchant> merchant = new ArrayList<Merchant>();

    @Data
    public static class Merchant {
        /**
         * 分组id
         */
        private String id;
        /**
         * 分组名称
         */
        private String name;
        /**
         * appid
         */
        private String appid;
        /**
         * appsecret
         */
        private String appsecret;
        /**
         * 接入平台编号
         */
        private String merchantCode;
        /**
         * 请求地扯
         */
        private String url;
        /**
         * 重定向工资条地址
         */
        private String accessUrl;
        /**
         * rsa 公钥
         */
        private String rsaPublicKey;
        /**
         * rsa 私钥
         */
        private String rsaPrivateKey;
        /**
         * 备注说明
         */
        private String remarks;

    }


}
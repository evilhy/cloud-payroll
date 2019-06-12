package chain.fxgj.server.payroll.config.properties;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "merchants", ignoreInvalidFields = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantsProperties {
    @Builder.Default
    private List<Merchant> merchant = new ArrayList<Merchant>();

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
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
        private AppPartnerEnum merchantCode;
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
         * rsa 公钥 -> 合作平台
         */
        private String paraRsaPublicKey;
        /**
         * 备注说明
         */
        private String remarks;
        /**
         * 数据权限
         */
        private List<FundLiquidationEnum>  dataAuths;

    }


}
package chain.fxgj.core.common.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author chain
 * create by chain on 2018/7/31 下午3:00
 **/
@Component
@ConfigurationProperties(prefix = "payroll", ignoreInvalidFields = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollProperties {

    private String imgUrl;

    private String insideUrl;

    /**
     * 放薪管家 sm4加密、解密key [线下分配]
     * sm4 对称加密
     */
    private String fxgjSm4Key;

    /**
     * 放薪管家 sm4  CBC模式下 vi [线下分配]
     * sm4 对称加密
     */
    private String fxgjSm4Iv;

    /**
     * 签名回执地址
     */
    private String signPdfPath;
}

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
     * 签名回执地址
     */
    private String signPdfPath;

    /**
     * 签名回执地址
     */
    private String signReplacePath;

    /**
     * 报税签约上传身份证图片地址
     */
    private String signUploadPath;
}

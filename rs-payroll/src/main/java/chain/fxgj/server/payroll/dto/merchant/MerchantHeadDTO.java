package chain.fxgj.server.payroll.dto.merchant;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.server.payroll.config.ErrorConstant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 工资条  对外输出接口
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantHeadDTO {

    /**
     * 版本号ID
     */
    @Builder.Default
    private String version = "1.0";
    /**
     * 签名
     * 填写对报文摘要的签名
     */
    private String signature;
    /**
     * 接入平台编号
     */
    //private String merchantCode;
    /**
     * appid
     */
    private String appid;
    /**
     * 毫秒
     */
    private String timestamp;
    /**
     * 渠道端流水号
     */
    private String clientSn;

    public static MerchantHeadDTO decrypt(MerchantHeadDTO merchantHeadDTO, String decrypt) {
        MerchantHeadDTO merchantHead = null;
        try {
            merchantHead = MerchantHeadDTO.builder()
                    //.version(RSAEncrypt.decrypt(merchantHeadDTO.getVersion(), decrypt))
                    .version(merchantHeadDTO.getVersion())
                    // .merchantCode(merchantHeadDTO.getMerchantCode())
                    .appid(merchantHeadDTO.getAppid())
                    .timestamp(merchantHeadDTO.getTimestamp())
                    .clientSn(merchantHeadDTO.getClientSn())
                    .signature(merchantHeadDTO.getSignature())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_03.getErrorMsg());
        }
        return merchantHead;
    }
}

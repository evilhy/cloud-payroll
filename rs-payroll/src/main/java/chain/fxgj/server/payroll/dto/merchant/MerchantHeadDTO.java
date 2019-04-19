package chain.fxgj.server.payroll.dto.merchant;


import chain.css.exception.ParamsIllegalException;
import chain.fxgj.server.payroll.config.ErrorConstant;
import chain.fxgj.server.payroll.util.RSAEncrypt;
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


    public static MerchantHeadDTO decrypt(MerchantHeadDTO merchantHeadDTO, String decrypt) {
        MerchantHeadDTO merchantHead = null;
        try {
            merchantHead = MerchantHeadDTO.builder()
                    .version(RSAEncrypt.decrypt(merchantHeadDTO.getVersion(), decrypt))
                   // .merchantCode(merchantHeadDTO.getMerchantCode())
                    .appid(merchantHeadDTO.getAppid())
                    .signature(merchantHeadDTO.getSignature())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_03.getErrorMsg());
        }
        return merchantHead;
    }
}

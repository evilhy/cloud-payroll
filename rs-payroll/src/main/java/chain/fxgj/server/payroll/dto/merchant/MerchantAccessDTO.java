package chain.fxgj.server.payroll.dto.merchant;

import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.fxgj.server.payroll.util.Sha1;
import lombok.*;

import java.security.DigestException;
import java.util.LinkedHashMap;

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
public class MerchantAccessDTO {
    /**
     * 接口调用凭证
     * 每次请求获取凭证，用完后立即失效
     */
    private String accessToken;
    /**
     * 凭证超时时间
     * accessToken接口调用凭证超时时间，单位（秒
     */
    private Integer expiresIn;
    /**
     * 访问地址
     */
    private String accessUrl;

    public static MerchantAccessDTO encryption(MerchantAccessDTO merchantAccessDTO, String encryption) {
        MerchantAccessDTO merchantAccess = null;
        try {
            merchantAccess = MerchantAccessDTO.builder()
                    .accessToken(RSAEncrypt.encrypt(merchantAccessDTO.getAccessToken(), encryption))
                    .expiresIn(merchantAccessDTO.getExpiresIn())
                    .accessUrl(merchantAccessDTO.getAccessUrl())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return merchantAccess;
    }

    public static String signature(MerchantAccessDTO merchantAccessDTO, MerchantHeadDTO merchantHeadDTO) throws DigestException {
        LinkedHashMap<String, Object> signatureMap = new LinkedHashMap<>();
        signatureMap.put("accessUrl", merchantAccessDTO.accessUrl);

        signatureMap.put("version", merchantHeadDTO.getVersion());
        signatureMap.put("merchantCode", merchantHeadDTO.getMerchantCode());

        String signature = Sha1.SHA1(signatureMap);
        return signature;
    }
}

package chain.fxgj.server.payroll.dto.merchant;

import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.fxgj.server.payroll.util.Sha1;
import lombok.*;

import java.security.DigestException;
import java.util.LinkedHashMap;
import java.util.LinkedList;


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
public class MerchantDTO {
    /**
     * 客户姓名
     */
    private String name;
    /**
     * 证件类型
     */
    private String idType;
    /**
     * 证件号码
     */
    private String idNumber;
    /**
     * 客户手机号
     */
    private String phone;
    /**
     * 标识用户唯一信息
     */
    private String uid;
    /**
     * 用户微信昵称
     */
    private String nickname;
    /**
     * 用户微信头像地址
     */
    private String headimgurl;

    public static MerchantDTO decrypt(MerchantDTO merchantDTO, String decrypt) {
        MerchantDTO merchant = null;
        try {
            merchant = MerchantDTO.builder()
                    .name(RSAEncrypt.decrypt(merchantDTO.getName(), decrypt) )
                    .idType(RSAEncrypt.decrypt(merchantDTO.getIdType(), decrypt) )
                    .idNumber(RSAEncrypt.decrypt(merchantDTO.getIdNumber(), decrypt) )
                    .phone(RSAEncrypt.decrypt(merchantDTO.getPhone(), decrypt) )
                    .uid(RSAEncrypt.decrypt(merchantDTO.getUid(), decrypt) )
                    .nickname(merchantDTO.getNickname())
                    .headimgurl(merchantDTO.getHeadimgurl())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return merchant;
    }


    public static String signature(MerchantDTO merchantDTO, MerchantHeadDTO merchantHeadDTO) throws DigestException {
        LinkedHashMap<String, Object> signatureMap = new LinkedHashMap<>();
        signatureMap.put("name", merchantDTO.getName());
        signatureMap.put("idType", merchantDTO.getIdType());
        signatureMap.put("idNumber", merchantDTO.getIdNumber());
        signatureMap.put("phone", merchantDTO.getPhone());
        signatureMap.put("uid", merchantDTO.getUid());

        signatureMap.put("version", merchantHeadDTO.getVersion());
        signatureMap.put("appid", merchantHeadDTO.getAppid());

        //signatureMap.put("signature", merchantHeadDTO.getSignature());
        //signatureMap.put("merchantCode", merchantHeadDTO.getMerchantCode());

        String signature = Sha1.SHA1(signatureMap);
        return signature;
    }


}

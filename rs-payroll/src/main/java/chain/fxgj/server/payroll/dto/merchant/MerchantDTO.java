package chain.fxgj.server.payroll.dto.merchant;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.server.payroll.config.ErrorConstant;
import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.fxgj.server.payroll.util.Sha1;
import chain.utils.commons.JacksonUtil;
import chain.utils.fxgj.constant.DictEnums.AppPartnerEnum;
import chain.utils.fxgj.constant.DictEnums.FundLiquidationEnum;
import chain.utils.fxgj.constant.DictEnums.RegisterTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.DigestException;
import java.util.LinkedHashMap;
import java.util.List;

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
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
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
     * openId
     */
    private String openId;
    /**
     * 用户微信昵称
     */
    private String nickname;
    /**
     * 用户微信头像地址
     */
    private String headimgurl;
    /**
     * 合作商平台标识
     */
    private AppPartnerEnum appPartner;
    /**
     * 数据权限
     */
    private List<FundLiquidationEnum> dataAuths;
    /**
     * 注册类型
     */
    private RegisterTypeEnum registerType;


    public static MerchantDTO decrypt(MerchantDTO merchantDTO, String decrypt) {
        MerchantDTO merchant = null;
        try {
            merchant = MerchantDTO.builder()
                    .name(RSAEncrypt.decrypt(StringUtils.trimToEmpty(merchantDTO.getName()), decrypt))
                    .idType(RSAEncrypt.decrypt(StringUtils.trimToEmpty(merchantDTO.getIdType()), decrypt))
                    .idNumber(RSAEncrypt.decrypt(StringUtils.trimToEmpty(merchantDTO.getIdNumber()), decrypt))
                    .phone(RSAEncrypt.decrypt(StringUtils.trimToEmpty(merchantDTO.getPhone()), decrypt))
                    .uid(RSAEncrypt.decrypt(StringUtils.trimToEmpty(merchantDTO.getUid()), decrypt))
                    .openId(RSAEncrypt.decrypt(StringUtils.trimToEmpty(merchantDTO.getOpenId()), decrypt))
                    .headimgurl(StringUtils.trimToEmpty(merchantDTO.getHeadimgurl()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_03.getErrorMsg());
        }

        String nickName = "";
        try {
            nickName = URLEncoder.encode(merchantDTO.getNickname(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("获取昵称出现异常！");
        }
        merchant.setNickname(nickName);
        return merchant;
    }


    public static String signature(MerchantDTO merchantDTO, MerchantHeadDTO merchantHeadDTO) {
        LinkedHashMap<String, Object> signatureMap = new LinkedHashMap<>();
        signatureMap.put("name", merchantDTO.getName());
        signatureMap.put("idType", merchantDTO.getIdType());
        signatureMap.put("idNumber", merchantDTO.getIdNumber());
        signatureMap.put("phone", merchantDTO.getPhone());
        signatureMap.put("uid", merchantDTO.getUid());

        signatureMap.put("version", merchantHeadDTO.getVersion());
        signatureMap.put("appid", merchantHeadDTO.getAppid());
        signatureMap.put("timestamp", merchantHeadDTO.getAppid());
        signatureMap.put("clientSn", merchantHeadDTO.getAppid());
        log.info("signatureMap:[{}]", JacksonUtil.objectToJson(signatureMap));
        String signature = null;
        try {
            signature = Sha1.SHA1(signatureMap);
        } catch (DigestException e) {
            e.printStackTrace();
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_05.getErrorMsg());
        }
        return signature;
    }


// 切库注释
//    public EmployeeWechatInfo conver() {
//        EmployeeWechatInfo employeeWechatInfo = EmployeeWechatInfo.builder()
//                .name(name)
//                .idType(CertTypeEnum.values()[Integer.valueOf(this.idType)])
//                .idNumber(this.idNumber)
//                .phone(phone)
//                .uid(uid)
//                .openId(openId)
//                .nickname(this.nickname)
//                .headimgurl(this.headimgurl)
//                .appPartner(this.appPartner)
//                .build();
//
//        return employeeWechatInfo;
//
//    }

    /**
     * @param merchantDTO
     * @param merchantHeadDTO
     * @return
     */
    public static String signatureClinet(MerchantDTO merchantDTO, MerchantHeadDTO merchantHeadDTO) {
        LinkedHashMap<String, Object> signatureMap = new LinkedHashMap<>();
        signatureMap.put("name", merchantDTO.getName());
        signatureMap.put("idType", merchantDTO.getIdType());
        signatureMap.put("idNumber", merchantDTO.getIdNumber());
        signatureMap.put("phone", merchantDTO.getPhone());
        signatureMap.put("uid", merchantDTO.getUid());

        signatureMap.put("version", merchantHeadDTO.getVersion());
        signatureMap.put("appid", merchantHeadDTO.getAppid());
        log.info("signatureMap:[{}]", JacksonUtil.objectToJson(signatureMap));
        String signature = null;
        try {
            signature = Sha1.SHA1(signatureMap);
        } catch (DigestException e) {
            e.printStackTrace();
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_05.getErrorMsg());
        }
        return signature;
    }


}
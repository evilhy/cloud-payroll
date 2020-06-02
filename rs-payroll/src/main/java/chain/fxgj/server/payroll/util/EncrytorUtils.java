package chain.fxgj.server.payroll.util;

import chain.css.exception.ServiceHandleException;
import chain.ids.core.commons.constant.ErrorConstant;
import chain.utils.commons.StringUtils;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.salt.StringFixedSaltGenerator;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @program: cloud-gateway
 * @description: 加密工具类
 * @author: lius
 * @create: 2020/06/01 11:42
 */
public class EncrytorUtils {

    private PooledPBEStringEncryptor encryptorNo = null;

    private static PooledPBEStringEncryptor initEncrypt(String salt, String passwd) {
        PooledPBEStringEncryptor encryptorNo = new PooledPBEStringEncryptor();
        encryptorNo.setPassword(passwd);
        encryptorNo.setPoolSize(1);
        encryptorNo.setSaltGenerator(new StringFixedSaltGenerator(salt));
        return encryptorNo;
    }

    /**
     * 随机加密
     *
     * @param field
     * @param salt
     * @param passwd
     * @return
     */
    public static String encryptField(String field, String salt, String passwd) {
        try {

            PooledPBEStringEncryptor encryptorNo = initEncrypt(base64Decoder(salt), base64Decoder(passwd));

            if (StringUtils.isEmpty(field)) {
                return field;
            }
            return encryptorNo.encrypt(field);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据加密失败！"));
        }
    }

    /**
     * 随机解密
     *
     * @param field
     * @param salt
     * @param passwd
     * @return
     */
    public static String decryptField(String field, String salt, String passwd) {
        try {
            PooledPBEStringEncryptor encryptorNo = initEncrypt(base64Decoder(salt), base64Decoder(passwd));

            if (StringUtils.isEmpty(field)) {
                return field;
            }
            return encryptorNo.decrypt(field);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据解密失败！"));
        }
    }


    /**
     * Base64 编码
     *
     * @param str 原字符串
     * @return
     */
    public static String base64Encoder(String str) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] textByte = new byte[0];
        try {
            textByte = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedText = encoder.encodeToString(textByte);
        return encodedText;
    }


    /**
     * Base64 解码
     *
     * @param encodedText
     * @return
     */
    public static String base64Decoder(String encodedText) {
        final Base64.Decoder decoder = Base64.getDecoder();
        String str = null;
        try {
            str = new String(decoder.decode(encodedText), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

}

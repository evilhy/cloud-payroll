package chain.fxgj.server.payroll.service.sm4.impl;

import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.server.payroll.service.sm4.TokenSignService;
import chain.fxgj.server.payroll.util.nj.SM4Util;
import chain.fxgj.server.payroll.util.nj.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * @Description:SM4 加解密
 * @Author: du
 * @Date: 2021/3/1 10:09
 */
@Slf4j
@Service
public class TokenSignServiceImpl implements TokenSignService {

    @Autowired
    PayrollProperties payrollProperties;

    /**
     * 使用 sm4 对 报文体解密 【放薪管家】【解密】
     *
     * @param algorithmName sm4的算法名称
     * @param cipherText    消息体
     * @return
     */
    @Override
    public String Sm4FxgjDecrypt(String algorithmName, String cipherText) throws BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] sm4dec = null;
        byte[] encrypted = Base64.decodeBase64(cipherText);
        String sm4Key = payrollProperties.getFxgjSm4Key();
        byte[] key = sm4Key.length() == 16 ? sm4Key.getBytes() : Util.hexStringToBytes(sm4Key);
        log.debug("====>从配置文件中取 ，sm4 key 【{}】 ， 长度：【{}】,", payrollProperties.getFxgjSm4Key(), payrollProperties.getFxgjSm4Key().length());

        if (SM4Util.ALGORITHM_NAME_ECB_NOPADDING.equals(algorithmName)) {
            // SM4/ECB/NoPadding  key
            log.debug("====>sm4 解密 模式: [{}]", SM4Util.ALGORITHM_NAME_ECB_NOPADDING);
            String fxgjSm4Key = payrollProperties.getFxgjSm4Key();
            //byte[] encrypted = Util.hexToByte(cipherText);
            sm4dec = SM4Util.decrypt_Ecb_NoPadding(key, encrypted);
        } else if (SM4Util.ALGORITHM_NAME_ECB_PADDING.equals(algorithmName)) {
            // SM4/ECB/PKCS5Padding key
            log.debug("====>sm4 解密 模式: [{}]", SM4Util.ALGORITHM_NAME_ECB_PADDING);
            sm4dec = SM4Util.decrypt_Ecb_Padding(key, encrypted);
        } else if (SM4Util.ALGORITHM_NAME_CBC_PADDING.equals(algorithmName)) {
            // SM4/CBC/PKCS5Padding  key
            String iv = payrollProperties.getFxgjSm4Iv();
            log.debug("====>sm4 解密 模式: [{}]", SM4Util.ALGORITHM_NAME_CBC_PADDING);
            log.debug("====>从配置文件中取 ，sm4 iv 【{}】 ， 长度：【{}】,", iv, iv.length());
            // SM4/CBC/PKCS5Padding  key
            sm4dec = SM4Util.decrypt_Cbc_Padding(key, iv.getBytes(), encrypted);
        } else if (SM4Util.ALGORITHM_NAME_CBC_NOPADDING.equals(algorithmName)) {
            // SM4/CBC/NoPadding  key
            //String iv = bankSecretsProperties.getFxgjSm4Iv();
            String iv = "0000000000000000";
            log.debug("====>sm4 解密 模式: [{}]", SM4Util.ALGORITHM_NAME_CBC_NOPADDING);
            log.debug("====>从配置文件中取 ，sm4 iv 【{}】 ， 长度：【{}】,", iv, iv.length());
            sm4dec = SM4Util.encrypt_Cbc_NoPadding(key, iv.getBytes(), encrypted);
        }
        return new String(sm4dec).trim();
    }

    /**
     * 使用 sm4 对 报文体加密 【放薪管家】【加密】
     *
     * @param algorithmName sm4的算法名称
     * @param plainText     消息体
     * @return
     */
    @Override
    public String Sm4FxgjEncrypt(String algorithmName, String plainText) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException {
        //        String fxgjSm4Key = bankSecretsProperties.getFxgjSm4Key();
        String encrypt = null;
        byte[] encrypted = null;
        String sm4Key = payrollProperties.getFxgjSm4Key();
        byte[] key = sm4Key.length() == 16 ? sm4Key.getBytes() : Util.hexStringToBytes(sm4Key);

        log.debug("====>从配置文件中取 ，sm4 key 【{}】 ， 长度：【{}】,", sm4Key, sm4Key.length());

        if (SM4Util.ALGORITHM_NAME_ECB_NOPADDING.equals(algorithmName)) {
            // SM4/ECB/NoPadding  key
            log.debug("====>sm4 加密 模式: [{}]", SM4Util.ALGORITHM_NAME_ECB_NOPADDING);
            byte[] ret = noPadding(plainText);
            encrypted = SM4Util.encrypt_Ecb_NoPadding(key, ret);
        } else if (SM4Util.ALGORITHM_NAME_ECB_PADDING.equals(algorithmName)) {
            // SM4/ECB/PKCS5Padding  key
            log.debug("====>sm4 加密 模式: [{}]", SM4Util.ALGORITHM_NAME_ECB_PADDING);
            encrypted = SM4Util.encrypt_Ecb_Padding(key, plainText.getBytes());
        } else if (SM4Util.ALGORITHM_NAME_CBC_PADDING.equals(algorithmName)) {
            // SM4/CBC/PKCS5Padding  key
            String iv = payrollProperties.getFxgjSm4Iv();
            log.debug("====>sm4 加密 模式: [{}]", SM4Util.ALGORITHM_NAME_CBC_PADDING);
            log.debug("====>从配置文件中取 ，sm4 iv 【{}】 ， 长度：【{}】,", iv, iv.length());
            encrypted = SM4Util.encrypt_Cbc_Padding(key, iv.getBytes(), plainText.getBytes());
        } else if (SM4Util.ALGORITHM_NAME_CBC_NOPADDING.equals(algorithmName)) {
            // SM4/CBC/NoPadding  key
            //String iv = njSecretsProperties.getFxgjSm4Iv();
            String iv = "0000000000000000";
            log.debug("====>sm4 加密 模式: [{}]", SM4Util.ALGORITHM_NAME_CBC_NOPADDING);
            log.debug("====>从配置文件中取 ，sm4 iv 【{}】 ， 长度：【{}】,", iv, iv.length());
            byte[] ret = noPadding(plainText);
            //byte[] ret =plainText.getBytes();
            encrypted = SM4Util.encrypt_Cbc_NoPadding(key, iv.getBytes(), ret);
        }
        //加密结果 --> 转成base64
        encrypt = Base64.encodeBase64String(encrypted);
        return encrypt;
    }


    private byte[] noPadding(String plainText) {
        byte[] ret = (byte[]) null;
        byte[] data = plainText.getBytes();
        int p = 16 - data.length % 16;

        ret = new byte[data.length + p];
        System.arraycopy(data, 0, ret, 0, data.length);
        return ret;
    }

}

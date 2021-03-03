package chain.fxgj.server.payroll.service.sm4;

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
 * @Date: 2021/3/1 10:08
 */
public interface TokenSignService {

    /**
     * 使用 sm4 对 报文体解密 【放薪管家】【解密】
     *
     * @param algorithmName sm4的算法名称
     * @param cipherText    消息体
     * @return
     */
    public String Sm4FxgjDecrypt(String algorithmName, String cipherText) throws BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException;

    /**
     * 使用 sm4 对 报文体加密 【放薪管家】【加密】
     *
     * @param algorithmName sm4的算法名称
     * @param plainText     消息体
     * @return
     */
    public String Sm4FxgjEncrypt(String algorithmName, String plainText) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException;
}

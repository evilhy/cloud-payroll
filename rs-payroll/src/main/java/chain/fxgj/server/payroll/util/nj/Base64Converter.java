package chain.fxgj.server.payroll.util.nj;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/3/3 10:57
 */
public class Base64Converter {

    final static Base64.Encoder encoder = Base64.getEncoder();
    final static Base64.Decoder decoder = Base64.getDecoder();

    /**
     * 给字符串加密
     * @param text
     * @return
     */
    public static String encode(String text) {
        byte[] textByte = new byte[0];
        try {
            textByte = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedText = encoder.encodeToString(textByte);
        return encodedText;
    }

    /**
     * 将加密后的字符串进行解密
     * @param encodedText
     * @return
     */
    public static String decode(String encodedText) {
        String text = null;
        try {
            text = new String(decoder.decode(encodedText), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {

        String username = "Miracle Luna";
        String password = "p@sSW0rd";

        // 加密
        System.out.println("====  [加密后] 用户名/密码  =====");
        System.out.println(Base64Converter.encode(username));
        System.out.println(Base64Converter.encode(password));

        // 解密
        System.out.println("\n====  [解密后] 用户名/密码  =====");
        System.out.println(Base64Converter.decode(Base64Converter.encode(username)));
        System.out.println(Base64Converter.decode(Base64Converter.encode(password)));
    }
}

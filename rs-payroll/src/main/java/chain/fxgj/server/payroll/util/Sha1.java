package chain.fxgj.server.payroll.util;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class Sha1 {

    /**
     * SHA1 安全加密算法
     *
     * @param maps 参数key-value map集合
     * @return
     * @throws DigestException
     */
    public static String SHA1(Map<String, Object> maps) throws DigestException {
        //获取信息摘要 - 参数字典排序后字符串
        String decrypt = getOrderByLexicographic(maps);

//       String ss =  DigestUtils.sha1Hex(decrypt);
//        Log.info("==>另一种算法={}",ss);

        try {
            //指定sha1算法
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(decrypt.getBytes());
            //获取字节数组
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
//                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
//
//                if (shaHex.length() < 2) {
//                    hexString.append(0);
//                }
//                hexString.append(shaHex);

                hexString.append(String.format("%02X", 0xFF & messageDigest[i]));

            }
            return hexString.toString().toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new DigestException("签名错误！");
        }
    }

    /**
     * 获取参数的字典排序
     *
     * @param maps 参数key-value map集合
     * @return String 排序后的字符串
     */
    private static String getOrderByLexicographic(Map<String, Object> maps) {
        return splitParams(lexicographicOrder(getParamsName(maps)), maps);
    }

    /**
     * 获取参数名称 key
     *
     * @param maps 参数key-value map集合
     * @return
     */
    private static List<String> getParamsName(Map<String, Object> maps) {
        List<String> paramNames = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : maps.entrySet()) {
            paramNames.add(entry.getKey());
        }
        return paramNames;
    }

    /**
     * 参数名称按字典排序
     *
     * @param paramNames 参数名称List集合
     * @return 排序后的参数名称List集合
     */
    private static List<String> lexicographicOrder(List<String> paramNames) {
        Collections.sort(paramNames);
        return paramNames;
    }

    /**
     * 拼接排序好的参数名称和参数值
     *
     * @param paramNames 排序后的参数名称集合
     * @param maps       参数key-value map集合
     * @return String 拼接后的字符串
     */
    private static String splitParams(List<String> paramNames, Map<String, Object> maps) {
        StringBuilder paramStr = new StringBuilder();
        int i = 0;
        for (String paramName : paramNames) {

//            if (i < 1) {
//                paramStr.append(paramName);
//            } else {
//                paramStr.append(paramName).append("=");
//            }
            paramStr.append(paramName).append("=");

            //解决乱码问题
//            if(i<paramNames.size()-1){
//                paramStr.append(String.valueOf(maps.get(paramName))).append("&");
//            }else {
//                paramStr.append(String.valueOf(maps.get(paramName)));
//            }

            try {
                if (i < paramNames.size() - 1) {
                    paramStr.append(new String(String.valueOf(maps.get(paramName)).getBytes("UTF-8"))).append("&");
                } else {
                    paramStr.append(new String(String.valueOf(maps.get(paramName)).getBytes("UTF-8")));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

//            maps.get(paramName);
//            for (Map.Entry<String, Object> entry : maps.entrySet()) {
//                if (paramName.equals(entry.getKey())) {
//                    paramStr.append(String.valueOf(entry.getValue()));
//                }
//            }

            i = i + 1;
        }
        log.info("==>拼接后的字符串={}", paramStr.toString());
        return paramStr.toString();
    }
}
package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.config.properties.MerchantsProperties;
import chain.fxgj.server.payroll.dto.merchant.MerchantDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantHeadDTO;
import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.JsonUtil;
import chain.utils.commons.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @program: cloud-general
 * @description: 测试
 * @author: lius
 * @create: 2020/02/25 14:42
 */
@Slf4j
public class SslHttpTest {

    public static void main(String[] args) {
        String sourceUrl = "https://sitwxp.cardpu.com/fx-payroll/#/token?accessToken=kc383AWf";
        String pubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGYHGGPlZvE4DE7ExTBMDNwJlDKXBiQYaprvxGZ+rf7YqJhxO08UnecTHKpPdA0KGe6vMwgT58AN3Cj1WsytIQ6Y2ybiqSwlpjlFQaNb3jiiE4gnSMkMvxxzRaHQ+Y10Qtfil47wqVq2TCKMMWrgSfMNINoTbSEp10FFbhbVrxpQIDAQAB";
        String priKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIZgcYY+Vm8TgMTsTFMEwM3AmUMpcGJBhqmu/EZn6t/tiomHE7TxSd5xMcqk90DQoZ7q8zCBPnwA3cKPVazK0hDpjbJuKpLCWmOUVBo1veOKITiCdIyQy/HHNFodD5jXRC1+KXjvCpWrZMIowxauBJ8w0g2hNtISnXQUVuFtWvGlAgMBAAECgYAjwCzz5kngq3Oq8KMtwyn4k7Ey6Sd5PK2zH1cG9EbM5Mni5QkdLsTUZZE1tMYDfH5DZYbl9LzHCQP262OD4UIZz2mhotBzT6UaZPhdMNsHzNojIwQa+syHTBgFMe39AuDyyes+0pG4rAlolPDMuywgb5yIK+1eMvPiW8nXZvg9AQJBAPOInM/ybSt7iqzL0Am9GOsOsvBXAlKxzUvEAdPi7YcFn5pmHTVQeymeDj+qlMYm56lB14UcTakEdwL/5SYLRXUCQQCNQWJAwDDdKcFw6vtH5WoDh4KeDGupLCQ89g1RpLoZtwq0oe46VexC59EfhG1Kz9zTi2YVrfRnc+lH5WpTLgVxAkEAqENOnXrRpQaB5SwY/HGT4uzQA7EKYNqKjvvJi32yQeVHxiUhrzGBN1sGW0Tf8Bz3WQGuCEFrAwmbtQ3bZLLK9QJAXU6yc2lBHebGNCvUfyKJC/nIi1RTDbXt3iL+m06/69qghL9umTRG089DsZkNhNyX11l+vpVhG7FSiL5/pKCC0QJAMHErhJK0pHG1e64bVbMQNhTPBPlt7WwMMkNU7iewNglPAwUzzeTHbf0wAHfHSo2vpz4BKrKRU6F2HxMOyzaTnQ==df723820";

        String zxKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI/4kSgDuQjP74lyAuoVFtZbnAgQjFN1YmSXLAjgXAsnh8g3Lx5N6i7j7uuLO4IjdEpTBjMob9IlwiEF9KE3Rx06RKamuOXlTJft2E9XxoXCNbpviMWui8z983Hd3K9lyLq+NrdOnw510A4+/Bp6Vo3Y8TLW/AzcCizWOOFtD/xxAgMBAAECgYAe4DkJMtsw958wg+H6H7G5IQZyeFIP6AHE/uKzuKIkxkKJfsQ9JIqPqdRocYC+pSngcrPGrupkUDK08FkJZhh66koLp5vAsDe1EtS3Q4G2njSa5Mjq4iqqhGr5VkBp2cEOrXcjqvFgPsgQtd5VjTXE8Q7crKbP5srFldr1EAL4wQJBANd7Z9aH4/bUaLeDkrHGlj6vCVKXvm4S8ff4KA9IWMxQJkTrFaQjxQggkgSpWbwh+9iwcd7OvSrtskPbyMNuiqkCQQCrCtqRnk4oh2yjh1oyZRhCpKuo8GDv0/fyJ9g4JzvNrKojno7iueKt9SU8+1E52v2Fti9yeejz9pF5RdtIDoiJAkAztnoFEjezVOaMpBbgczg7cuZZ2/DnmcRYLkXu5P3qipGkmIZpDeyLNsXUOJBAhidaqX2qaxMa6lCN2IEl5bGBAkEAjsEOTz8a6ISuzYVoaGsr5mNdyjBmF4SIiplqwuMr7KtRjU0G4IBYiuvx2oW+81StB+5Yh2jsXsHKBLejHu7SUQJBAKJetmdzgEWnTsaPj/R1RZQMvFLI9YiNoZESxCLSfga0hmIexeJJE6mwIPCnj/osGgmdH2Lr7CbmEZ8T8V2J/EE=";

        try {
            String en = RSAEncrypt.encrypt(sourceUrl, pubKey);
            log.info("en:[{}]", en);

            String de = RSAEncrypt.decrypt(en, priKey);
            log.info("de:[{}]", de);

//            String retUrl = "fctPEfZot+tgdveDpiN+KE+WhqWV/yUOL3adJzbqDd/fNEAa7P5dLJ4hJW3C+N9tC1nRVIr1p1CvntKfLZHA9k4PIY1Vtfy8epjJr1hdQH9EatII2E9KQKltmzMhum5HoCJs858c4NtCdD09f0E7A5xy60qiLg8cvf9gMhDXrSFAw3UBhN1bDOqpDh0dLD4UoAqWBSQo/PzHVXQDEUYySsbNil4WCmoLsfUi81mfc1nVHmzJq0Bzl61GIUkK7jDQAquI4e/yX9JWGBY1Fo8WCdHz2z3YRcmaWSdcvjnuDwwKxOYRA3o8jPsIMW1zBk1vAOFaTK4ZHeC3mD3JcFHepQ=";
            String retUrl = "iKgthFVnqG1+Uv0942ilUeTvXJH9zVvjdosXBQbwvBIwrhuOZIBZt3VVZwQhq/Q3xmw58YojoRFSP3tc1BAW9aHrtCxB+zIbmFOLSGpRHMql/osMoO9ElfIhzd9mgoPvIBTzq+YHxKQJ+IZ4A/4VdJoaxgsIglRVx5ythCw1ZeA=";
            String decrypt = RSAEncrypt.decrypt(retUrl, zxKey);
            log.info("decrypt:[{}]", decrypt);



        } catch (Exception e) {
            e.printStackTrace();
        }

        // 开始
        String rsaPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGYHGGPlZvE4DE7ExTBMDNwJlDKXBiQYaprvxGZ+rf7YqJhxO08UnecTHKpPdA0KGe6vMwgT58AN3Cj1WsytIQ6Y2ybiqSwlpjlFQaNb3jiiE4gnSMkMvxxzRaHQ+Y10Qtfil47wqVq2TCKMMWrgSfMNINoTbSEp10FFbhbVrxpQIDAQAB";
        String version = "1.0";
        String appid = "wx0345ad9614fe9567";

        MerchantHeadDTO merchantHeadDTO = MerchantHeadDTO.builder()
                .appid(appid)
                .version(version)
                .build();
        String name = "五次名";
        String idType = "01";
        String idNumber = "500101199109080018";
        String phone = "13420090801";
        String uid = "9871234";

//        String name = "彭银";
//        String idType = "01";
//        String idNumber = "500101199109080018";
//        String phone = "13420090801";
//        String uid = "9871234";

        String openId = "oFnSLvyxBArqJtYqd3-xU6H7Xr08";
        String nickname = "用户微信昵称";
        String headimgurl = "http://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTLGZicQDuRPCCcFEFEN72qnAgVGJ99JMmegLMTknEpaSGbVzo2aweUSCkC0reicqhpZOWABEoTqahmA/132";

        MerchantDTO merchantDTO = MerchantDTO.builder()
                .name(name)
                .idType(idType)
                .idNumber(idNumber)
                .phone(phone)
                .uid(uid)
                .nickname(nickname)
                .headimgurl(headimgurl)
                .build();

        String merHead = JacksonUtil.objectToJson(merchantHeadDTO);
        System.out.println("merchantHeadDTO:" + merHead);

        String merDto = JacksonUtil.objectToJson(merchantDTO);
        System.out.println("merchantDTO:[{}]" + merDto);

        String signature = MerchantDTO.signature(merchantDTO, merchantHeadDTO);
        System.out.println("signature sha-1:" + signature);

        //用公钥加密
        MerchantDTO merchantDTO_Encrypt = null;
        try {
            merchantDTO_Encrypt = MerchantDTO.builder()
                    .name(RSAEncrypt.encrypt(name, rsaPublicKey))
                    .idType(RSAEncrypt.encrypt(idType, rsaPublicKey))
                    .idNumber(RSAEncrypt.encrypt(idNumber, rsaPublicKey))
                    .phone(RSAEncrypt.encrypt(phone, rsaPublicKey))
                    .uid(RSAEncrypt.encrypt(uid, rsaPublicKey))
                    .openId(RSAEncrypt.encrypt(openId, rsaPublicKey))
                    .nickname(nickname)
                    .headimgurl(headimgurl)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String reqParam = JsonUtil.objectToJson(merchantDTO_Encrypt);
        System.out.println("reqParam:" + reqParam);

        Base64 base64 = new Base64();

        //使用公钥加密
        try {
            signature = RSAEncrypt.encrypt(signature, rsaPublicKey);
            System.out.println("signature使用公钥加密:" + signature);
            String s = RSAEncrypt.decrypt(signature, priKey);
            System.out.println("signature使用私钥解密-->:" + s);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            signature = base64.encodeToString(signature.getBytes("UTF-8"));
            System.out.println("signature.base64:" + signature);
            String ss = new String(base64.decode(signature), "UTF-8");
            System.out.println("signature.base64解密:-->:" + ss);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        //测试地址
            String requestUrl = "https://sitgateway.cardpu.com/payroll/merchant/getAccess";
//        String requestUrl = "http://172.16.248.62:8080/merchant/getAccess";
        System.out.println("调用地址:" + requestUrl);
        System.out.println("入参:" + reqParam);

        String certFile = "E://Dowdloads//p12//general-client.p12";
        //证书密码
        String password = "kayak20200225";

        try {
//              测试暂不使用证书
//            FileInputStream file = null;
//            file = new FileInputStream(new File(certFile));
//
//            // 私钥证书密码
//            KeyStore keyStore = KeyStore.getInstance("PKCS12");
//            keyStore.load(file, password.toCharArray());
//
//            SSLContext sslContext = SSLContexts.custom()
//                    .loadTrustMaterial(null, new TrustStrategy() {
//                        @Override
//                        public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
//                            return true;
//                        }
//                    })
//                    .loadKeyMaterial(keyStore, password.toCharArray())
//                    .build();

            StringEntity postingString = new StringEntity(reqParam);
            postingString.setContentType("application/json");//发送json数据需要设置contentType

            LocalDateTime now = LocalDateTime.now();
            String clientDate = "" + now.getYear() + now.getMonthValue() + now.getDayOfMonth();
            log.info("clientDate:[{}]", clientDate);
            log.info("yyyyMMdd1:[{}]", new SimpleDateFormat("yyyyMMdd").format(new Date()));
            long clientTime = now.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            log.info("clientTime:[{}]", clientTime);

            String encryptVersion = RSAEncrypt.encrypt(version, rsaPublicKey);
            log.info("version:[{}], encryptVersion:[{}]", version, encryptVersion);
            //方式一：post请求
            HttpPost httpPost = new HttpPost(requestUrl);
            httpPost.setEntity(postingString);
            httpPost.setHeader("signature",signature);
            httpPost.setHeader("appid",appid);
            httpPost.setHeader("version", RSAEncrypt.encrypt(version, rsaPublicKey));
            httpPost.setHeader("clientSn", "123456789987654321");
            httpPost.setHeader("clientDate", clientDate);
            httpPost.setHeader("clientTime", String.valueOf(clientTime));
            //方式二：get请求
            HttpGet httpget = new HttpGet(requestUrl);

            CloseableHttpClient httpClient = HttpClients.custom()
//                    .setSSLContext(sslContext)
                    .build();

            HttpResponse response = httpClient.execute(httpPost);

            System.out.print(response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            log.info("result:[{}]");
            System.out.print(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

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

        String rsaPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGYHGGPlZvE4DE7ExTBMDNwJlDKXBiQYaprvxGZ+rf7YqJhxO08UnecTHKpPdA0KGe6vMwgT58AN3Cj1WsytIQ6Y2ybiqSwlpjlFQaNb3jiiE4gnSMkMvxxzRaHQ+Y10Qtfil47wqVq2TCKMMWrgSfMNINoTbSEp10FFbhbVrxpQIDAQAB";
        String version = "1.0";
        String appid = "wx0345ad9614fe9567";

        MerchantHeadDTO merchantHeadDTO = MerchantHeadDTO.builder()
                .appid(appid)
                .version(version)
                .build();

        String name = "王方";
        String idType = "01";
        String idNumber = "14212219891008530X";
        String phone = "16666666666";
        String uid = "9871234";
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
        System.out.println("merchantDTO:[]" + merDto);

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
        System.out.println("reqParam:" + JacksonUtil.objectToJson(reqParam));

        Base64 base64 = new Base64();

        //使用公钥加密
        try {
            signature = RSAEncrypt.encrypt(signature, rsaPublicKey);
            System.out.println("signature使用公钥加密:" + signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            signature = base64.encodeToString(signature.getBytes("UTF-8"));
            System.out.println("signature.base64:" + signature);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        //测试地址
        String requestUrl = "https://sitwx.cardpu.com/merchant/getAccess";
        System.out.println("调用地址:" + requestUrl);
        System.out.println("入参:" + reqParam);

        String certFile = "E://Dowdloads//p12//general-client.p12";
        //证书密码
        String password = "kayak20200225";

        try {

            FileInputStream file = null;
            file = new FileInputStream(new File(certFile));

            // 私钥证书密码
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(file, password.toCharArray());

            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                            return true;
                        }
                    })
                    .loadKeyMaterial(keyStore, password.toCharArray())
                    .build();

            StringEntity postingString = new StringEntity(reqParam);
            postingString.setContentType("application/json");//发送json数据需要设置contentType

            LocalDateTime now = LocalDateTime.now();
            String clientDate = "" + now.getYear() + now.getMonthValue() + now.getDayOfMonth();
            log.info("clientDate:[{}]", clientDate);
            log.info("yyyyMMdd1:[{}]", new SimpleDateFormat("yyyyMMdd").format(new Date()));
            long clientTime = now.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            log.info("clientTime:[{}]", clientTime);
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
                    .setSSLContext(sslContext).build();

            HttpResponse response = httpClient.execute(httpPost);

            System.out.print(response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            System.out.print(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

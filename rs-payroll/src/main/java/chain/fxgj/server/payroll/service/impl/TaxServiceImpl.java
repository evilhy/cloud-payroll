package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.server.payroll.dto.tax.SealUserReq;
import chain.fxgj.server.payroll.dto.tax.SealUserRes;
import chain.fxgj.server.payroll.dto.tax.WalletH5Req;
import chain.fxgj.server.payroll.dto.tax.WalletH5Res;
import chain.fxgj.server.payroll.service.TaxService;
import chain.fxgj.server.payroll.util.RSAUtils;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenm
 * @date 2020/11/5
 */
@Slf4j
@Service
public class TaxServiceImpl implements TaxService {
    //渠道
//    private final static String channel="xingxuan";
    private final static String channel = "XAYK";
    //signSalt混淆
    private final static String signSalt = "zxyh7572";
    // Rsa 公钥
    private final static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCNotSn72NFXy92LCc09MjZOAlhngjbUGrDcj8y8pTUMy1tM9NvTjWTMc9OU+bN9pnBzS7sFPJ+aSDaC81p6LeetHwovSkZGdxXMogFow3PPvnc+oc/19oeqsrMrB/bDxjF4sWVNgn+RhXjuOmBLn43WS10ZZ7zEV9DwT8WiyZVqQIDAQAB";

    private final static String requestUrl = "https://sitapi.lgabc.cn/exgateway/lgb";
//    private final static String requestUrl="https://ysbapi.lgabc.cn/exgateway/lgb";
    //https://ysbapi.lgabc.cn/exgateway/    f2xKYRotyEy5W31r

    @Override
    public void file() {
        String url = "/sys/unAuth/file";
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("date", "20210714");
        try {
            //h5
            send(paramMap, url);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("=====> 发送交易请求失败，url:{}，paramMap:{}", url, JacksonUtil.objectToJson(paramMap));
        }
    }

    @Override
    public WalletH5Res walletH5(WalletH5Req req) {
        String url = "/sys/unAuth/file";
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("transUserId", req.getTransUserId());
        paramMap.put("userName", req.getUserName());
        paramMap.put("phoneNo", req.getPhoneNo());
        paramMap.put("idType", req.getIdType());
        paramMap.put("idCardNo", req.getIdCardNo());
        paramMap.put("fwOrg", req.getFwOrg());
        paramMap.put("ygOrg", req.getYgOrg());
        try {
            //h5
            String result = send(paramMap, url);
            if (StringUtils.isNotBlank(result)) {
                return JacksonUtil.jsonToBean(result, WalletH5Res.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("=====> 发送交易请求失败，url:{}，paramMap:{}", url, JacksonUtil.objectToJson(paramMap));
        }
        return null;
    }

    @Override
    public String sealH5(String transUserId) {
        String url = "/sys/unAuth/file";
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("transUserId", transUserId);
        try {
            //h5
            String result = send(paramMap, url);
            if (StringUtils.isNotBlank(result)) {
                Map<String, String> map = (Map<String, String>) JacksonUtil.jsonToMap(result);
                return map.get("url");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("=====> 发送交易请求失败，url:{}，paramMap:{}", url, JacksonUtil.objectToJson(paramMap));
        }
        return null;
    }

    @Override
    public SealUserRes user(SealUserReq req) {
        String url = "/sys/unAuth/file";
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("transUserId", req.getTransUserId());
        paramMap.put("userName", req.getUserName());
        paramMap.put("phoneNo", req.getPhoneNo());
        paramMap.put("idType", req.getIdType());
        paramMap.put("idCardNo", req.getIdCardNo());
        paramMap.put("idCardImg1", req.getIdCardImg1());
        paramMap.put("idCardImg2", req.getIdCardImg2());
        paramMap.put("fwOrg", req.getFwOrg());
        paramMap.put("ygOrg", req.getYgOrg());
        try {
            //h5
            String result = send(paramMap, url);
            if (StringUtils.isNotBlank(result)) {
                return JacksonUtil.jsonToBean(result, SealUserRes.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("=====> 发送交易请求失败，url:{}，paramMap:{}", url, JacksonUtil.objectToJson(paramMap));
        }
        return null;
    }

    public String send(Map<String, Object> paramMap, String url) throws Exception {
        String timestamp = System.currentTimeMillis() + "";
        //随机字符串
        String random = DigestUtils.md5Hex(timestamp).substring(0, 16);
        String cipherKey = toEncodedString(
                Base64.encodeBase64(RSAUtils.encryptByPublicKey(random.getBytes(), publicKey)), StandardCharsets.ISO_8859_1);
        log.debug("cipherKey:{}", cipherKey);

        StringBuilder md5Src = new StringBuilder();
        md5Src.append(signSalt)
                .append(random)
                .append(channel)
                .append(timestamp)
                .append(url)
                .append(JacksonUtil.objectToJson(paramMap));
        log.debug("md5Src:[{}]", md5Src.toString());
        String cipherMac = DigestUtils.md5Hex(md5Src.toString());
        log.debug("cipherMac:[{}]", cipherMac);

        HttpResponse request = request(requestUrl + url, JacksonUtil.objectToJson(paramMap), channel, timestamp, cipherKey, cipherMac);
        if (null == request) {
            log.info("=====> 发送交易请求失败");
        }
        if (null != request && null != request.getEntity()) {
            return EntityUtils.toString(request.getEntity());
        }
        return null;
    }

    private static HttpResponse request(String requestUrl, String reqParam, String channel, String timestamp, String cipherKey, String cipherMac) {
        log.info("request:{},body:{}", requestUrl, reqParam);

        //证书密码
        String password = "f2xKYRosKEy5W31r";
        try {
            File file1 = new ClassPathResource("api-client.p12").getFile();
            FileInputStream file = new FileInputStream(file1);

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

            StringEntity postingString = new StringEntity(reqParam, "UTF-8");
            //发送json数据需要设置contentType
            postingString.setContentEncoding("UTF-8");
            postingString.setContentType("application/json");

            //post请求
            HttpPost httpPost = new HttpPost(requestUrl);
            httpPost.addHeader("channel", channel);
            httpPost.addHeader("timestamp", timestamp);
            httpPost.addHeader("cipher-key", cipherKey);
            httpPost.addHeader("cipher-mac", cipherMac);
            log.debug(JacksonUtil.objectToJson(httpPost.getAllHeaders()));
            httpPost.setEntity(postingString);

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .build();
            HttpResponse response = httpClient.execute(httpPost);
            log.debug("response:{},{}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
//            String result = EntityUtils.toString(response.getEntity());
//            log.debug(result);

            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), new File("D:/tmp/payroll/sign/11.txt"));

            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toEncodedString(final byte[] bytes, final Charset charset) {
        return new String(bytes, charset != null ? charset : Charset.defaultCharset());
    }

    public static void main(String[] args) {
        try {
            //h5
            String url = "/sys/walletH5";
            Map<String, Object> paramMap = new HashMap();

//            paramMap.put("transUserId", "60b4fb889cf2115094db5920");
//            paramMap.put("userName", "杜颖");
//            paramMap.put("phone", "15807151024");
//            paramMap.put("isRealAuth", true);
//            paramMap.put("idType", IdTypeEnum.SFZ);
//            paramMap.put("idCardNo","421181199003105320");

            //钱包余额
            url = "/sys/userAssets";

            //交易记录
//            url="/wallet/transLog";
//            paramMap.put("subTransType", "OUT");
//            paramMap.put("start", "");
//            paramMap.put("end", "");

            //分佣任务
//            url="/sys/task";
//            paramMap.put("amt", new BigDecimal("100"));
//            paramMap.put("taskNo", "8");
//            paramMap.put("taskDesc", "分佣任务1");
//            paramMap.put("commissionType", "个人佣金");
//            paramMap.put("commissionRatio", BigDecimal.ONE);

            //实名认证
//            url="/sys/user";
//            paramMap.put("idCardImg1", Base64Util.GetImageStr("/Users/admin/Downloads/111.jpg"));
//            paramMap.put("idCardImg2", Base64Util.GetImageStr("/Users/admin/Downloads/sealImg.png"));

            //签约
//            url="/sys/sealH5";

            url = "/sys/unAuth/file";
            paramMap.put("date", "20210714");

            String timestamp = System.currentTimeMillis() + "";
            //随机字符串
            String random = DigestUtils.md5Hex(timestamp).substring(0, 16);
            String cipherKey = toEncodedString(
                    Base64.encodeBase64(RSAUtils.encryptByPublicKey(random.getBytes(), publicKey)), StandardCharsets.ISO_8859_1);
            log.debug("cipherKey:{}", cipherKey);

            StringBuilder md5Src = new StringBuilder();
            md5Src.append(signSalt)
                    .append(random)
                    .append(channel)
                    .append(timestamp)
                    .append(url)
                    .append(JacksonUtil.objectToJson(paramMap));
            log.debug("md5Src:[{}]", md5Src.toString());
            String cipherMac = DigestUtils.md5Hex(md5Src.toString());
            log.debug("cipherMac:[{}]", cipherMac);

            request(requestUrl + url, JacksonUtil.objectToJson(paramMap), channel, timestamp, cipherKey, cipherMac);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

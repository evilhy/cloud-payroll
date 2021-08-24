package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.server.payroll.config.properties.ProxyProperties;
import chain.fxgj.server.payroll.config.properties.TaxProperties;
import chain.fxgj.server.payroll.service.TaxHttpClientService;
import chain.fxgj.server.payroll.util.RSAUtils;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Map;

/**
 * 报税平台请求接口
 *
 * @author
 */
@Service("taxHttpClientService")
@Slf4j
public class TaxHttpClientServiceImpl implements TaxHttpClientService {

    @Autowired
    ProxyProperties proxyProperties;

    @Autowired
    TaxProperties taxProperties;

    /**
     * 发送请求
     *
     * @param paramMap 请求参数
     * @param url      请求接口路径
     * @return
     * @throws Exception
     */
    @Override
    public String send(Map<String, Object> paramMap, String url) throws Exception {
        String timestamp = System.currentTimeMillis() + "";
        //随机字符串
        String random = DigestUtils.md5Hex(timestamp).substring(0, 16);
        String cipherKey = toEncodedString(
                Base64.encodeBase64(RSAUtils.encryptByPublicKey(random.getBytes(), taxProperties.getPublicKey())), StandardCharsets.ISO_8859_1);
        log.debug("cipherKey:{}", cipherKey);

        StringBuilder md5Src = new StringBuilder();
        md5Src.append(taxProperties.getSignSalt())
                .append(random)
                .append(taxProperties.getChannel())
                .append(timestamp)
                .append(url)
                .append(JacksonUtil.objectToJson(paramMap));
        log.debug("md5Src:[{}]", md5Src.toString());
        String cipherMac = DigestUtils.md5Hex(md5Src.toString());
        log.debug("cipherMac:[{}]", cipherMac);

        log.info("=====> 报税平台接口 请求地址 ：{}", taxProperties.getRequestUrl() + url);
        log.info("=====> 报税平台接口 请求信息 ：{}", JacksonUtil.objectToJson(paramMap));
        HttpResponse request = request(taxProperties.getRequestUrl() + url, JacksonUtil.objectToJson(paramMap), taxProperties.getChannel(), timestamp, cipherKey, cipherMac);
        log.info("=====> 报税平台接口 返回信息 ：{}", JacksonUtil.objectToJson(request));
        if (null == request) {
            log.info("=====> 发送交易请求失败");
        }
        if (null != request && null != request.getEntity()) {
            return EntityUtils.toString(request.getEntity());
        }
        return null;
    }

    private HttpResponse request(String requestUrl, String reqParam, String channel, String timestamp, String cipherKey, String cipherMac) {
        log.info("request:{},body:{}", requestUrl, reqParam);

        //证书密码
        String password = taxProperties.getPassword();
        try {
            File file1 = new ClassPathResource(taxProperties.getCertificateName()).getFile();
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

            //正向代理
            HttpHost proxy = new HttpHost(proxyProperties.getIp(), proxyProperties.getPort(), proxyProperties.getSchemeName());

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setProxy(proxy)
                    .build();
            HttpResponse response = httpClient.execute(httpPost);
            log.debug("response:{},{}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
//            String result = EntityUtils.toString(response.getEntity());
//            log.debug(result);

//            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), new File("D:/tmp/payroll/sign/11.txt"));

            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toEncodedString(final byte[] bytes, final Charset charset) {
        return new String(bytes, charset != null ? charset : Charset.defaultCharset());
    }
}

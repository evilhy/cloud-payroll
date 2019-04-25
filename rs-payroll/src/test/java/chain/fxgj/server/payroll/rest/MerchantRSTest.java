package chain.fxgj.server.payroll.rest;


import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.config.properties.MerchantsProperties;
import chain.fxgj.server.payroll.dto.merchant.MerchantAccessDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantHeadDTO;
import chain.fxgj.server.payroll.dto.request.DistributeDTO;
import chain.fxgj.server.payroll.dto.response.PlanListBean;
import chain.fxgj.server.payroll.dto.response.Res100703;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.dto.tfinance.IntentRequestDTO;
import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.Optional;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class MerchantRSTest extends BaseRSTest {


    @Autowired
    MerchantsProperties merchantProperties;


    private MerchantsProperties.Merchant getMerchant(String id) {
        Optional<MerchantsProperties.Merchant> qWechat = merchantProperties.getMerchant().stream()
                .filter(item -> item.getAppid().equalsIgnoreCase(id)).findFirst();
        MerchantsProperties.Merchant merchant = qWechat.orElse(null);
        return merchant;
    }

    /**
     * 访问凭证
     */
    @Test
    public void getAccessUrl() throws Exception {

        //String signature = "123456789";
        //String merchantCode = "1";
        String version = "1.0";
        String appid = "wx0345ad9614fe9567";

        MerchantHeadDTO merchantHeadDTO = MerchantHeadDTO.builder()
                .appid(appid)
                //.merchantCode(merchantCode)
                .version(version)
                .build();


        MerchantsProperties.Merchant merchant = this.getMerchant(StringUtils.trimToEmpty(appid));

        String name = "测试";
        String idType = "01";
        String idNumber = "420625";
        String phone = "13899997777";
        String uid = "9871234";
        String nickname = "用户微信昵称";
        String headimgurl = "http://wwww.baidu.com";


        MerchantDTO merchantDTO = MerchantDTO.builder()
                .name(name)
                .idType(idType)
                .idNumber(idNumber)
                .phone(phone)
                .uid(uid)
                .nickname(nickname)
                .headimgurl(headimgurl)
                .build();

        String signature = MerchantDTO.signature(merchantDTO, merchantHeadDTO);
        //用公钥加密
        MerchantDTO merchantDTO_Encrypt = MerchantDTO.builder()
                .name(RSAEncrypt.encrypt(name, merchant.getRsaPublicKey()))
                .idType(RSAEncrypt.encrypt(idType, merchant.getRsaPublicKey()))
                .idNumber(RSAEncrypt.encrypt(idNumber, merchant.getRsaPublicKey()))
                .phone(RSAEncrypt.encrypt(phone, merchant.getRsaPublicKey()))
                .uid(RSAEncrypt.encrypt(uid, merchant.getRsaPublicKey()))
                .nickname(nickname)
                .headimgurl(headimgurl)
                .build();

        log.info("==>merchantDTO={}", JacksonUtil.objectToJson(merchantDTO));

        log.info("==>merchantDTO_Encrypt={}", JacksonUtil.objectToJson(merchantDTO_Encrypt));

        log.info("==>signature sha-1 ={}", signature);
        //使用公钥加密
        signature =  RSAEncrypt.encrypt(signature, merchant.getRsaPublicKey());
        log.info("==>signature 使用公钥加密 ={}", signature);

        Base64 base64 = new Base64();
        signature= base64.encodeToString(signature.getBytes("UTF-8"));
        log.info("==>signature base64 ={}", signature);

        log.info("==>appid={}", appid);
        log.info("==>version={}", RSAEncrypt.encrypt(version, merchant.getRsaPublicKey()));

        //appid = "wx0345ad9614fe9567999";

        webTestClient.post()
                .uri("/merchant/getAccess")
                .header("signature", signature)
                .header("appid", appid)
                .header("version", RSAEncrypt.encrypt(version, merchant.getRsaPublicKey()))
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(merchantDTO_Encrypt)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader().exists("signature")
                .expectBody()//返回是什么类型的对象
                .consumeWith(WebTestClientRestDocumentation.document("merchant_getAccessUrl",
                        relaxedResponseFields(JavaDocReader.javaDoc(MerchantAccessDTO.class))
                ));
    }


    /**
     * 访问凭证
     */
    @Test
    public void callback() {
        String accessToken = "HivcNXzz";

        webTestClient.get()
                .uri("/merchant/callback?accessToken={accessToken}", accessToken)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(WebTestClientRestDocumentation.document("merchant_callback",
                        requestParameters(
                                parameterWithName("accessToken").description("访问凭证")
                        ),
                        relaxedResponseFields(JavaDocReader.javaDoc(Res100705.class))
                ));
    }


}

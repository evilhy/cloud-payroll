package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.merchant.MerchantAccessDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantHeadDTO;
import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.utils.commons.UUIDUtil;
import org.apache.commons.codec.binary.Base64;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;


@FixMethodOrder(MethodSorters.JVM)
public class MerchantControllerTest extends BaseTestCase {

    @Test
    public void getAccess() throws Exception {

        String appid = "wx0345ad9614fe9567";
        String version = "1.0";
        String clientSn = UUIDUtil.createUUID32();
        String clientDate = "20210913";
        String clientTime = "101010";
        String timestamp  =""+ System.currentTimeMillis();


        MerchantHeadDTO merchantHeadDTO = new MerchantHeadDTO();
        merchantHeadDTO.setAppid("wx0345ad9614fe9567");
        merchantHeadDTO.setVersion(version);
        merchantHeadDTO.setClientSn(clientSn);
        merchantHeadDTO.setTimestamp(timestamp);


        String rsaPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGYHGGPlZvE4DE7ExTBMDNwJlDKXBiQYaprvxGZ+rf7YqJhxO08UnecTHKpPdA0KGe6vMwgT58AN3Cj1WsytIQ6Y2ybiqSwlpjlFQaNb3jiiE4gnSMkMvxxzRaHQ+Y10Qtfil47wqVq2TCKMMWrgSfMNINoTbSEp10FFbhbVrxpQIDAQAB";

        //1. 元数据
        String name = "彭银";
        String idType = "01";
        String idNumber = "14212219891008530X";
        String phone = "16666666666";
        String uid = "9871234";
        String openId = "oFnSLvyxBArqJtYqd3-xU6H7Xr08";

        MerchantDTO merchant = new MerchantDTO();
        merchant.setName(name);
        merchant.setIdType(idType);
        merchant.setIdNumber(idNumber);
        merchant.setPhone(phone);
        merchant.setUid(uid);
        merchant.setOpenId(openId);
        merchant.setHeadimgurl("http://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTLGZicQDuRPCCcFEFEN72qnAgVGJ99JMmegLMTknEpaSGbVzo2aweUSCkC0reicqhpZOWABEoTqahmA/132");
        merchant.setNickname("用户微信昵称");

        //signature
        String signatureClient = MerchantDTO.signature(merchant, merchantHeadDTO);
        //signature  -->公钥  --> 加密
        String encryptSignatureClient = RSAEncrypt.encrypt(signatureClient, rsaPublicKey);
        //signature base64 解密
        Base64 base64 = new Base64();
        String signature = new String(base64.encode(encryptSignatureClient.getBytes()), "UTF-8");


        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setName(RSAEncrypt.encrypt(name, rsaPublicKey));
        merchantDTO.setIdType(RSAEncrypt.encrypt(idType, rsaPublicKey));
        merchantDTO.setIdNumber(RSAEncrypt.encrypt(idNumber, rsaPublicKey));
        merchantDTO.setPhone(RSAEncrypt.encrypt(phone, rsaPublicKey));
        merchantDTO.setUid(RSAEncrypt.encrypt(uid, rsaPublicKey));
        merchantDTO.setOpenId(RSAEncrypt.encrypt(openId, rsaPublicKey));
        merchantDTO.setHeadimgurl("http://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTLGZicQDuRPCCcFEFEN72qnAgVGJ99JMmegLMTknEpaSGbVzo2aweUSCkC0reicqhpZOWABEoTqahmA/132");
        merchantDTO.setNickname("用户微信昵称");


        webTestClient.post().uri("/merchant/getAccess")
                //.header("signature", "QklIQXlkSGVycStZcHpYenBmRGs0Uk9ReUV5NTB0ektvTGthRW4vQ2hZWnUraXZBdHh2OGZvRjkzdHFvSXRDMHJVUldFOFFZQ2gxS2N6MllqVk81cHNadzFkbk1LcUtqN2hYYndYUThEUGUyYy9ocTB2d0tnVkwrZ3lpOHpNNzBaMHNKcVo3NEFKaklDdElqK1d3NFNOK0xIMERJaG8vQlNUR3JoWVJrOWxBPQ==")
                .header("signature", signature)
                .header("appid", appid)
                .header("version", version)
                .header("clientSn", clientSn)
                .header("clientDate", clientDate)
                .header("clientTime", clientTime)
                .header("timestamp", timestamp)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(merchantDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("merchant_getAccess",
                        relaxedRequestFields(JavaDocReader.javaDoc(MerchantDTO.class)),
                        relaxedResponseFields(JavaDocReader.javaDoc(MerchantAccessDTO.class))
                ));

    }
}
package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.merchant.MerchantAccessDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantDTO;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;


@FixMethodOrder(MethodSorters.JVM)
public class MerchantControllerTest extends BaseTestCase{

    @Test
    public void getAccess() throws Exception {

        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setName("彭银");
        merchantDTO.setIdType("01");
        merchantDTO.setIdNumber("14212219891008530X");
        merchantDTO.setPhone("16666666666");
        merchantDTO.setUid("9871234");
        merchantDTO.setOpenId("oFnSLvyxBArqJtYqd3-xU6H7Xr08");
        merchantDTO.setName("用户微信昵称");
        merchantDTO.setHeadimgurl("http://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTLGZicQDuRPCCcFEFEN72qnAgVGJ99JMmegLMTknEpaSGbVzo2aweUSCkC0reicqhpZOWABEoTqahmA/132");

        webTestClient.post().uri("/merchant/getAccess")
                .header("signature", "xxxxxxxxxxx")
                .header("appid", "wx0345ad9614fe9567")
                .header("version", "01")
                .header("clientSn", "")
                .header("clientDate", "")
                .header("clientTime", "")
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
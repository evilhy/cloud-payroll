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
                .header("signature", "QklIQXlkSGVycStZcHpYenBmRGs0Uk9ReUV5NTB0ektvTGthRW4vQ2hZWnUraXZBdHh2OGZvRjkzdHFvSXRDMHJVUldFOFFZQ2gxS2N6MllqVk81cHNadzFkbk1LcUtqN2hYYndYUThEUGUyYy9ocTB2d0tnVkwrZ3lpOHpNNzBaMHNKcVo3NEFKaklDdElqK1d3NFNOK0xIMERJaG8vQlNUR3JoWVJrOWxBPQ==")
                .header("appid", "wx0345ad9614fe9567")
                .header("version", "dgl412VRwgpKpeTzx7sUq+w5rbNHFKMNhjo/STCAqpF4Z/uiL/pAxsfy7q4MprnDtUv2AvPvoSHaO6a82EJ2DWu4DWYQ6SI1BBr9AmB6bujl+K0B/jpAJiTJWpuIr1cqIlruyO6+IDQzl47XX7WkMaonNvvlaoQNvZYnCn1oCs0=")
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
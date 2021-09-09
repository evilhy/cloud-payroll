package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.tax.SigningDetailsReq;
import chain.utils.commons.UUIDUtil;
import core.dto.response.region.RegionDictionaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/9/9 15:56
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class RegionControllerTest  extends BaseTestCase{

    /**
     * 地区数据字典
     *
     * @throws Exception
     */
    @Test
    public void dictionary() throws Exception {
        String salt = UUIDUtil.createUUID32();
        String passwd = UUIDUtil.createUUID32();
        String entId = UUIDUtil.createUUID32();
        webTestClient.get().uri("/region/dictionary?regionParentCode={regionParentCode}","420100000000")
                .header("encry-salt", salt)
                .header("encry-passwd", passwd)
                .header("ent-id", entId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("region_dictionary",
                        relaxedRequestParameters( parameterWithName("regionParentCode").description("地区父ID（不传默认查询所有:省、自治区、直辖市）")),
                        relaxedResponseFields(JavaDocReader.javaDoc(chain.fxgj.server.payroll.dto.region.RegionDictionaryDTO.class))
                ));
    }
}

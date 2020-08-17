package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.testfile.EmpCardResDTOP;
import core.dto.request.BaseReqDTO;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;


@FixMethodOrder(MethodSorters.JVM)
public class WalletControllerTest extends BaseTestCase{

    @Test
    public void balance() throws Exception {
        BaseReqDTO baseReqDTO = new BaseReqDTO();
//        baseReqDTO.setEntId("ff80808171b40c010171c489c2210009");
        baseReqDTO.setIdNumber("330123198604198875");
        webTestClient.post().uri("/wallet/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(baseReqDTO)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(EmpCardResDTOP.class)//返回是什么类型的对象
                .consumeWith(document("wallet_balance"
                        //相应文档
//                        relaxedRequestFields(JavaDocReader.javaDoc(BaseReqDTO.class)),
//                        relaxedResponseFields(JavaDocReader.javaDoc(EmpCardResDTOP.class))
                ));
    }

    @Test
    public void empCardList() throws Exception {
        BaseReqDTO baseReqDTO = new BaseReqDTO();
        baseReqDTO.setEntId("");
        webTestClient.post().uri("/wallet/empCardList")
                .contentType(MediaType.APPLICATION_JSON)
                .header("id", "fxgj")
                .syncBody(baseReqDTO)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(EmpCardResDTOP.class)//返回是什么类型的对象
                .consumeWith(document("wallet_empCardList",
                        //相应文档
//                        relaxedRequestFields(JavaDocReader.javaDoc(BaseReqDTO.class)),
                        relaxedResponseFields(JavaDocReader.javaDoc(EmpCardResDTOP.class))));
    }
}
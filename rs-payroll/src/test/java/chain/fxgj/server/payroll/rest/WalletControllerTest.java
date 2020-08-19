package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.request.BindWechatDTO;
import chain.fxgj.server.payroll.dto.testfile.EmpCardResDTOP;
import chain.fxgj.server.payroll.dto.tfinance.IntentRequestDTO;
import core.dto.request.BaseReqDTO;
import core.dto.response.wallet.EmpCardAndBalanceResDTO;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
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
    public void empCardAdnBalance() throws Exception {
        BindWechatDTO baseReqDTO = new BindWechatDTO();

        webTestClient.post().uri("/wallet/empCardAdnBalance")
                .contentType(MediaType.APPLICATION_JSON)
                .header("encry-salt", "123456")
                .header("encry-passwd", "234567")
                .syncBody(baseReqDTO)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(EmpCardAndBalanceResDTO.class)//返回是什么类型的对象
                .consumeWith(document("wallet_empCardAdnBalance",
                        //相应文档
                        relaxedRequestFields(JavaDocReader.javaDoc(BindWechatDTO.class)),
                        relaxedResponseFields(JavaDocReader.javaDoc(EmpCardAndBalanceResDTO.class))));
    }


    /**
     * 福利卡券数量
     *
     * @throws Exception
     */
    @Test
    public void countWelfareEmpTicket() throws Exception {
        IntentRequestDTO intentRequestDTO = new IntentRequestDTO();
        intentRequestDTO.setProtocol(1);
        webTestClient.post()
                .uri("/wisales/countWelfareEmpTicket")
                .header("jsession_id", "123123123")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(intentRequestDTO)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IntentRequestDTO.class)//返回是什么类型的对象
                .consumeWith(document("wisales_countWelfareEmpTicket",
                        relaxedRequestFields(JavaDocReader.javaDoc(IntentRequestDTO.class))));
    }
}
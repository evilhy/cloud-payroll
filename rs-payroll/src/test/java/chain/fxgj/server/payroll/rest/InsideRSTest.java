package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.request.*;
import chain.fxgj.server.payroll.dto.response.BankCardGroup;
import chain.fxgj.server.payroll.dto.response.Res100302;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.springframework.http.MediaType;

import java.util.Arrays;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

/**
 * @Author: hdl
 * @Date: 2019/3/12 15:37
 */
@FixMethodOrder(MethodSorters.JVM)
public class InsideRSTest extends BaseRSTest {


    @Test
    public void sendCode() {
        Req100302 req100302 = new Req100302();
        req100302.setBusiType("0");
        req100302.setPhone("15527689216");
        webTestClient.post().uri("/inside/sendCode")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(req100302)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("inside_sendCode",
                        relaxedRequestFields(JavaDocReader
                                .javaDoc(Res100302.class))));
    }

    @Test
    public void receipt() {
        login();
        ResReceiptDTO build = ResReceiptDTO.builder()
                .wageDetailId("ff808081694d24ff016950dcfc89000f")
                .receiptsStatus(1)
                .msg("我有意见").build();
        webTestClient.post().uri("/inside/receipt")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(build)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("inside_receipt",
                        relaxedRequestFields(JavaDocReader.javaDoc(ResReceiptDTO.class))));
    }

    @Test
    public void readWage() {
        login();
        ReadWageDTO build = ReadWageDTO.builder()
                .wageSheetId("3861bb29c33b424b966d7f1cb587e7d3")
                .build();
        webTestClient.post().uri("/inside/read")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(build)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("inside_read",
                        relaxedRequestFields(JavaDocReader.javaDoc(ReadWageDTO.class))));
    }

    @Test
    public void bandWX() {
        login();
        Req100702 build = Req100702.builder().codeId("11111")
                .code("666666")
                .idNumber("2kerXcaeyc6D6pTxJawRhHa/VFbmTXjl")
                .phone("18627879315")
                .build();
        webTestClient.post().uri("/inside/bindWX")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(build)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("inside_bindWX",
                        relaxedRequestFields(JavaDocReader.javaDoc(Req100702.class))));
    }

    @Test
    public void rz() {
        login();
        Req100701 build = Req100701.builder().codeId("11111")
                .code("666666")
                .idNumber("2kerXcaeyc6D6pTxJawRhHa/VFbmTXjl")
                .phone("18627879315")
                .pwd("123456")
                .build();
        webTestClient.post().uri("/inside/rz")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(build)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("inside_rz",
                        relaxedRequestFields(JavaDocReader.javaDoc(Req100701.class))));
    }

    @Test
    public void setPwd() {
        login();
        String pwd = "654321";
        webTestClient.post().uri("/inside/setPwd?pwd={pwd}", pwd)
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("inside_setPwd",
                        relaxedRequestParameters(parameterWithName("pwd").description("查询密码"))));
    }

    @Test
    public void updPwd() {
        login();
        UpdPwdDTO build = UpdPwdDTO.builder().oldPwd("654321").pwd("123456")
                .build();
        webTestClient.post().uri("/inside/updPwd")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(build)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("inside_updPwd",
                        relaxedRequestFields(JavaDocReader.javaDoc(UpdPwdDTO.class))));
    }

    @Test
    public void checkPhoneCode() {

    }

    @Test
    public void updPhone() {
        login();
        ReqPhone build = ReqPhone.builder().codeId("111111").code("666666").phone("15527682917")
                .build();
        webTestClient.post().uri("/inside/updPhone")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(build)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("inside_updPhone",
                        relaxedRequestFields(JavaDocReader.javaDoc(ReqPhone.class))));
    }

    @Test
    public void updBankCard() {
        login();
        BankCardGroup build1 = BankCardGroup.builder()
                .groupId("ff808081690a2b1c016913374f4a0006")
                .shortGroupName("安卓")
                .id("ff808081692304f001692901d81600f8")
                .build();
        UpdBankCardDTO build = UpdBankCardDTO.builder()
                .issuerBankId("03040000").cardNo("6230200165153434").issuerName("华夏银行").bankCardGroups(Arrays.asList(build1))
                .build();
        webTestClient.post().uri("/inside/updBankCard")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(build)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("inside_updBankCard",
                        relaxedRequestFields(JavaDocReader.javaDoc(UpdBankCardDTO.class))
                                .andWithPrefix("[]", JavaDocReader.javaDoc(BankCardGroup.class))));
    }

    @Test
    public void bankCardIsNew() {
    }
}
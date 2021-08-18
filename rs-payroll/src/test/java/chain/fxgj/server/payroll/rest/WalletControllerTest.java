package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.PageDTO;
import chain.fxgj.server.payroll.dto.SelectListDTO;
import chain.fxgj.server.payroll.dto.request.BindWechatDTO;
import chain.fxgj.server.payroll.dto.tfinance.IntentRequestDTO;
import chain.fxgj.server.payroll.dto.wallet.*;
import chain.utils.commons.UUIDUtil;
import core.dto.response.wallet.EmpCardAndBalanceResDTO;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.MediaType;

import java.util.Arrays;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;


@FixMethodOrder(MethodSorters.JVM)
public class WalletControllerTest extends BaseTestCase {

    /**
     * 钱包余额
     *
     * @throws Exception
     */
    @Test
    public void balance() throws Exception {
//        BaseReqDTO baseReqDTO = new BaseReqDTO();
//        baseReqDTO.setEntId("ff80808171b40c010171c489c2210009");
//        baseReqDTO.setIdNumber("330123198604198875");
        String entId = UUIDUtil.createUUID32();
        String sessionId = UUIDUtil.createUUID32();
        webTestClient.get().uri("/wallet/balance")
                .header("jsessionId", sessionId)
                .header("ent-id", entId)
                .exchange().expectStatus().isOk()
                .expectBody()
                .consumeWith(document("wallet_balance",
                        relaxedResponseFields(JavaDocReader.javaDoc(WalletBalanceDTO.class))
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

    /**
     * 查询登陆人当前企业资产信息
     *
     * @throws Exception
     */
    @Test
    public void empCardAndBalance() throws Exception {
        String salt = UUIDUtil.createUUID32();
        String passwd = UUIDUtil.createUUID32();
        String entId = UUIDUtil.createUUID32();
        webTestClient.get().uri("/wallet/empCardAndBalance")
                .header("encry-salt", salt)
                .header("encry-passwd", passwd)
                .header("ent-id", entId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("wallet_empCardAndBalance",
//                        relaxedRequestParameters(parameterWithName("url").description("获取url")),
                        relaxedResponseFields(JavaDocReader.javaDoc(chain.fxgj.server.payroll.dto.wallet.EmpCardAndBalanceResDTO.class))
                ));
    }

    /**
     * 提现台账分页列表
     *
     * @throws Exception
     */
    @Test
    public void withdrawalLedgerPage() throws Exception {
        WithdrawalLedgerPageReq build = WithdrawalLedgerPageReq.builder()
                .withdrawalStatus(Arrays.asList(0, 1, 2, 3))
                .build();
        webTestClient.post()
                .uri("/wallet/withdrawalLedgerPage")
                .header("jsession_id", UUIDUtil.createUUID32())
                .header("ent-id", UUIDUtil.createUUID32())
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(build)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()//返回是什么类型的对象
                .consumeWith(document("wallet_withdrawalLedgerPage",
                        relaxedRequestFields(JavaDocReader.javaDoc(WithdrawalLedgerPageReq.class))
                        , relaxedResponseFields(JavaDocReader.javaDoc(PageDTO.class))
                                .andWithPrefix("content.[].",
                                        JavaDocReader.javaDoc(WithdrawalLedgerPageRes.class))
                ));
    }

    /**
     * 提现台账详情
     *
     * @throws Exception
     */
    @Test
    public void withdrawalLedgerDetail() throws Exception {
        String withdrawalLedgerId = UUIDUtil.createUUID32();
        webTestClient.get().uri("/wallet/withdrawalLedgerDetail/{withdrawalLedgerId}", withdrawalLedgerId)
                .header("ent-id", UUIDUtil.createUUID32())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("wallet_withdrawalLedgerDetail",
                        pathParameters(parameterWithName("withdrawalLedgerId").description("提现台账ID"))
                        , relaxedResponseFields(JavaDocReader.javaDoc(WithdrawalLedgerDetailRes.class))
                ));
    }

    /**
     * 提现进度详情
     *
     * @throws Exception
     */
    @Test
    public void withdrawalRecordDetail() throws Exception {
        String withdrawalLedgerId = UUIDUtil.createUUID32();
        webTestClient.get().uri("/wallet/withdrawalRecordDetail/{withdrawalLedgerId}", withdrawalLedgerId)
                .header("ent-id", UUIDUtil.createUUID32())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("wallet_withdrawalRecordDetail",
                        pathParameters(parameterWithName("withdrawalLedgerId").description("提现台账ID"))
                        , relaxedResponseFields(JavaDocReader.javaDoc(WithdrawalRecordDetailRes.class))
                                .andWithPrefix("withdrawalRecordLog.",
                                        JavaDocReader.javaDoc(WithdrawalRecordLogDTO.class))
                                .andWithPrefix("withdrawalLedgerDetail.",
                                        JavaDocReader.javaDoc(WithdrawalLedgerDetailRes.class))
                ));
    }

    /**
     * 收款账户列表
     *
     * @throws Exception
     */
    @Test
    public void employeeCardList() throws Exception {
        webTestClient.get().uri("/wallet/employeeCardList")
                .header("ent-id", UUIDUtil.createUUID32())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("wallet_employeeCardList"
                        , relaxedResponseFields(JavaDocReader.javaDoc(EmployeeCardDTO.class))
                ));
    }

    /**
     * 确认提现
     *
     * @throws Exception
     */
    @Test
    public void withdraw() throws Exception {
        WithdrawalReq build = WithdrawalReq.builder()
                .employeeCardId(UUIDUtil.createUUID32())
                .cardNo("21342423213")
                .issuerBankId("023320")
                .issuerName("张三")
                .withdrawalLedgerId(UUIDUtil.createUUID32())
                .build();
        webTestClient.post()
                .uri("/wallet/withdraw")
                .header("jsession_id", UUIDUtil.createUUID32())
                .header("ent-id", UUIDUtil.createUUID32())
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(build)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()//返回是什么类型的对象
                .consumeWith(document("wallet_withdraw",
                        relaxedRequestFields(JavaDocReader.javaDoc(WithdrawalReq.class))
                ));
    }

    /**
     * 数据字典
     */
    @Test
    public void dict() {
        String id = "WithdrawalStatusEnum";
        webTestClient.get().uri("/base/{id}/dictItem", id)
                .header("jsessionId", UUIDUtil.createUUID32())
                .exchange().expectStatus().isOk()
                .expectBody()
                .consumeWith(document("base_dict",
                        pathParameters(parameterWithName("id").description("字典kEY")),
                        relaxedResponseFields(JavaDocReader.javaDoc(SelectListDTO.class))
                ));
    }
}
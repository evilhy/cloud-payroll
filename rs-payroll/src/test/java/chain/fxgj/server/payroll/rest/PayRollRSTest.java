package chain.fxgj.server.payroll.rest;

import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.response.*;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.SpringBootWebTestClientBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

/**
 * @Author: hdl
 * @Date: 2019/3/12 16:03
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class PayRollRSTest {

    public static String sessionId="5b4b10fb6a70467fa289f16896ae9113";

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    public ApplicationContext context;

    public WebTestClient webTestClient;

    @Autowired
    @Qualifier("jacksonCodecCustomizer")
    public CodecCustomizer codecCustomizer;

    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    EmployeeEncrytorService employeeEncrytorService;

    @Before
    public void before() throws Exception {
        final WebTestClient.Builder builder = WebTestClient.bindToApplicationContext(context)
                .configureClient();
        final SpringBootWebTestClientBuilderCustomizer builderCustomizer =
                new SpringBootWebTestClientBuilderCustomizer(Lists.newArrayList(codecCustomizer));
        //将自定义的序列化配置应用到webTestCliend
        builderCustomizer.customize(builder);
        this.webTestClient = builder
                .responseTimeout(Duration.ofMinutes(10))
                .baseUrl("http://localhost:8080/")
                .filter(documentationConfiguration(restDocumentation))
                .build();
    }

    @After
    public void after() throws Exception {

    }

    @Test
    public void serverDateTime() {
        webTestClient.get().uri("/roll/sdt")
                .exchange().expectStatus().isOk().expectBody(Long.class)
                .consumeWith(document("roll_sdt", relaxedResponseFields())).
                consumeWith(body ->
                    log.info("++++++++++{}", body.getResponseBody())
                );
    }

    @Test
    public void index() throws Exception{
        login();
        webTestClient.get().uri("roll/index")
                .header("jsession_id", sessionId)
                .exchange().expectStatus().isOk().expectBody(IndexDTO.class)
                .consumeWith(document("roll_index", relaxedResponseFields(JavaDocReader.javaDoc(IndexDTO.class))
                        .andWithPrefix("bean.", JavaDocReader.javaDoc(NewestWageLogDTO.class))))
                .consumeWith(body ->
                        log.info("++++++++++{}", body.getResponseBody())
                );
    }

    @Test
    public void groupList() {
        login();
        webTestClient.get().uri("roll/groupList")
                .header("jsession_id", sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_groupList",
                        relaxedResponseFields(fieldWithPath("[]").description("机构列表"))
                        .andWithPrefix("[]", JavaDocReader.javaDoc(NewestWageLogDTO.class))));

    }

    @Test
    public void entEmp() {
        String idNumber = "120105197801050320";
        webTestClient.get().uri("roll/entEmp?idNumber={idNumber}",idNumber)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_entEmp", relaxedRequestParameters(parameterWithName("idNumber").description("员工身份证号")),
                        relaxedResponseFields(JavaDocReader.javaDoc(NewestWageLogDTO.class))));
    }

    @Test
    public void wageList() {
        login();
        //groupId=ff808081692304f0016928d74681008e&year=&type=1
        String groupId = "ff808081690a2b1c016913374f4a0006";
        String year = "2019";
        String type = "1";
        webTestClient.get().uri("roll/wageList?groupId={groupId}&year={year}&type={type}",groupId,year,type)
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_wageList",
                        relaxedRequestParameters(parameterWithName("groupId").description("机构id"))
                                .and(parameterWithName("year").description("年份"))
                                .and(parameterWithName("type").description("类型 0资金到账 1合计")),
                        relaxedResponseFields(JavaDocReader.javaDoc(Res100703.class))
                                .andWithPrefix("planList.[]", JavaDocReader.javaDoc(PlanListBean.class))
                                ));
    }

    @Test
    public void wageDetail() {
        login();
        String wageSheetId = "3861bb29c33b424b966d7f1cb587e7d3";
        String groupId = "ff808081690a2b1c016913374f4a0006";
        webTestClient.get().uri("roll/wageDetail?wageSheetId={wageSheetId}&groupId={groupId}",wageSheetId,groupId)
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_wageDetail",
                        relaxedRequestParameters(parameterWithName("groupId").description("机构id"))
                                .and(parameterWithName("wageSheetId").description("方案id")),
                        relaxedResponseFields(fieldWithPath("[]").description("查看工资条详情"))
                                .andWithPrefix("[].", JavaDocReader
                                        .javaDoc(WageDetailDTO.class))));

    }

    @Test
    public void empInfo() {
        login();
        webTestClient.get().uri("roll/empInfo")
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_empInfo",
                        relaxedResponseFields(fieldWithPath("[]").description("员工个人信息"))
                                .andWithPrefix("[].", JavaDocReader
                                        .javaDoc(Res100708.class))));
    }

    @Test
    public void invoice() {
        login();
        webTestClient.get().uri("roll/invoice")
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_invoice",
                        relaxedResponseFields(fieldWithPath("[]").description("查询发票信息列表"))
                                .andWithPrefix("[].", JavaDocReader
                                        .javaDoc(GroupInvoiceDTO.class))));
    }

    @Test
    public void checkPwd() {
        login();
        String pwd = "123456";
        webTestClient.get().uri("roll/checkPwd?pwd={pwd}",pwd)
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_checkPwd",
                        relaxedRequestParameters(parameterWithName("pwd").description("查询密码"))));
    }

    @Test
    public void checkCard() {
        login();
        String cardNo = "153433";
        String idNumber= "SZqiUsfAC7mztlvQ6kSxy5zddhXWf3UK";
        webTestClient.get().uri("roll/checkCard?cardNo={cardNo}&idNumber={idNumber}",cardNo,idNumber)
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_checkCard",
                        relaxedRequestParameters(parameterWithName("cardNo").description("银行卡后6位"))
                                .and(parameterWithName("idNumber").description("身份证号"))));
    }

    @Test
    public void emp() {
        login();
        webTestClient.get().uri("roll/emp")
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_emp",
                        relaxedResponseFields(JavaDocReader.javaDoc(EmpInfoDTO.class))));
    }

    @Test
    public void empEnt() {
        login();
        webTestClient.get().uri("roll/empEnt")
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_empEnt",
                        relaxedResponseFields(fieldWithPath("[]").description("员工企业"))
                                .andWithPrefix("[].", JavaDocReader
                                        .javaDoc(EmpEntDTO.class))
                                .andWithPrefix("items[].",JavaDocReader.javaDoc(Res100708.class))
                                .andWithPrefix("cards[].",JavaDocReader.javaDoc(BankCard.class))
                                .andWithPrefix("cards.bankCardGroups[].",JavaDocReader.javaDoc(BankCardGroup.class))));
    }

    @Test
    public void empCard() {
        login();
        webTestClient.get().uri("roll/empCard")
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_empCard",
                        relaxedResponseFields(fieldWithPath("[]").description("员工银行卡"))
                                .andWithPrefix("[].", JavaDocReader
                                        .javaDoc(EmpEntDTO.class))
                                .andWithPrefix("items[].",JavaDocReader.javaDoc(Res100708.class))
                                .andWithPrefix("cards[].",JavaDocReader.javaDoc(BankCard.class))
                                .andWithPrefix("cards.bankCardGroups[].",JavaDocReader.javaDoc(BankCardGroup.class))));
    }

    @Test
    public void empCardLog() {
        login();
        String ids ="ff808081692304f001692901d81600f8";
        webTestClient.get().uri("roll/empCardLog?ids={ids}",ids)
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_empCardLog",
                        relaxedRequestParameters(parameterWithName("ids").description("银行卡id|集合")),
                        relaxedResponseFields(fieldWithPath("[]").description("员工银行卡修改记录"))
                                .andWithPrefix("[].", JavaDocReader
                                        .javaDoc(EmpCardLogDTO.class))));
    }

    @Test
    public void entPhone() {
        login();
        webTestClient.get().uri("roll/entPhone")
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_entPhone",
                        relaxedResponseFields(fieldWithPath("[]").description("手机号和公司列表"))
                                .andWithPrefix("[].", JavaDocReader
                                        .javaDoc(EmployeeListBean.class))));
    }

    @Test
    public void entUser() {
        login();
        String entId="ff808081690f0e3201691330d0950024";
        webTestClient.get().uri("roll/entUser?entId={entId}",entId)
                .header("jsession_id",sessionId)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_entUser",
                        relaxedRequestParameters(parameterWithName("entId").description("企业id")),
                        relaxedResponseFields(fieldWithPath("[]").description("企业超管"))
                                .andWithPrefix("[].", JavaDocReader
                                        .javaDoc(EntUserDTO.class))));
    }

    public void login() {
        sessionId = UUIDUtil.createUUID32();
        String openId= "oFnSLv0w4nipfwH0hFBwBimXjleY";
        String nickName = "%E9%9F%A9%E5%BE%B7%E8%89%AF";
        String headImg = "http://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTK2TdCQXNqUrzY9u3SFgRLdI5kOs0yh3jHrwEzic8n5tB9RDHHMqNsOX8l06rVAibVHHsrA273wwwjw/132";
        String idNumber = "420704199304164673";
        try {
            UserPrincipal userPrincipal = empWechatService.setWechatInfo(sessionId, openId, nickName, headImg,idNumber);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
package chain.fxgj.server.payroll.controller;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.handpassword.HandPasswordDTO;
import chain.fxgj.server.payroll.dto.request.PasswordSaveReq;
import chain.fxgj.server.payroll.dto.response.CrateNumericKeypadRes;
import chain.fxgj.server.payroll.dto.response.SecretFreeRes;
import chain.fxgj.server.payroll.service.EmployeeEncrytorService;
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

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

/**
 * @Description:
 * @Author: du
 * @Date: 2020/8/13 15:24
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class PasswordControllerTest {


    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    public ApplicationContext context;

    public WebTestClient webTestClient;

    @Autowired
    @Qualifier("jacksonCodecCustomizer")
    public CodecCustomizer codecCustomizer;

    @Autowired
    EmployeeEncrytorService employeeEncrytorService;

    @Before
    public void setUp() throws Exception {
        final WebTestClient.Builder builder = WebTestClient.bindToApplicationContext(context)
                .configureClient();
        final SpringBootWebTestClientBuilderCustomizer builderCustomizer =
                new SpringBootWebTestClientBuilderCustomizer(Lists.newArrayList(codecCustomizer));
        builderCustomizer.customize(builder);
        this.webTestClient = builder.baseUrl("http://localhost:8080/")
                .filter(documentationConfiguration(restDocumentation))
                .build();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * ????????????????????????????????????
     */
    @Test
    public void getBusinessFlowById() {
        webTestClient.get()
                .uri("/password/queryHandPassword")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//??????????????????????????????
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("password_queryHandPassword",
                        relaxedResponseFields(JavaDocReader.javaDoc(HandPasswordDTO.class))
                ));
    }

    /**
     * ??????????????????
     */
    @Test
    public void closeHandPassword() {
        webTestClient.get()
                .uri("/password/closeHandPassword")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//??????????????????????????????
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("password_closeHandPassword"
                ));
    }

    /**
     * ????????????????????????
     */
    @Test
    public void crateNumericKeypad() {
        webTestClient.get()
                .uri("/password/crateNumericKeypad")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//??????????????????????????????
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("password_crateNumericKeypad",
                        relaxedResponseFields(JavaDocReader.javaDoc(CrateNumericKeypadRes.class))
                ));
    }

    /**
     * ????????????
     */
    @Test
    public void checkPassword() {
        PasswordSaveReq req = PasswordSaveReq.builder()
                .password("N0,N1,N3,N4,N7,N9")
                .type("0")
                .build();

        webTestClient.post().uri("/password/checkPassword")
//                .header("jsessionId", sessionId)
                .syncBody(req).exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("password_checkPassword",
                        relaxedRequestFields(JavaDocReader.javaDoc(PasswordSaveReq.class))
                ));
//        webTestClient.get()
//                .uri("/password/checkPassword?password={password}&type={type}", password, type)
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody(String.class)//??????????????????????????????
//                .consumeWith(body -> log.info(body.getResponseBody()))
//                .consumeWith(document("password_checkPassword",
//                        requestParameters(parameterWithName("password").description("??????(??????????????????,?????????)"),
//                                parameterWithName("type").description("??????????????? 0???????????????  1???????????????"))
//                ));
    }

    /**
     * ?????????????????????
     */
    @Test
    public void savePassword() {
        PasswordSaveReq req = PasswordSaveReq.builder()
                .password("N0,N1,N3,N4,N7,N9")
                .type("0")
                .build();

        webTestClient.post().uri("/password/savePassword")
//                .header("jsessionId", sessionId)
                .syncBody(req).exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("password_savePassword",
                        relaxedRequestFields(JavaDocReader.javaDoc(PasswordSaveReq.class))
                ));
    }

    /**
     * ??????????????????????????????????????????
     */
    @Test
    public void login() {

        PasswordSaveReq req = PasswordSaveReq.builder()
                .password("N0,N1,N3,N4,N7,N9")
                .type("0")
                .build();

        webTestClient.post().uri("/password/login")
//                .header("jsessionId", sessionId)
                .syncBody(req).exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("password_login",
                        relaxedRequestFields(JavaDocReader.javaDoc(PasswordSaveReq.class))
                ));


//        String password = "123456";
//        String type = "1";
//
//        webTestClient.get()
//                .uri("/password/login?password={password}&type={type}", password, type)
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody(String.class)//??????????????????????????????
//                .consumeWith(body -> log.info(body.getResponseBody()))
//                .consumeWith(document("password_login",
//                        requestParameters(parameterWithName("password").description("??????(??????????????????,?????????)"),
//                        parameterWithName("type").description("??????????????? 0???????????????  1???????????????"))
//                ));
    }

    @Test
    public void secretFree() {
        PasswordSaveReq passwordSaveReq = new PasswordSaveReq();
        passwordSaveReq.setPassword("N0,N1,N3,N4,N7,N9");
        passwordSaveReq.setType("0");
        webTestClient.post().uri("/password/secretFree")
//                .header("jsessionId", sessionId)
                .syncBody(passwordSaveReq)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("password_secretFree",
                        relaxedRequestFields(JavaDocReader.javaDoc(PasswordSaveReq.class)),
                        relaxedResponseFields(JavaDocReader.javaDoc(SecretFreeRes.class))
                ));
    }

    @Test
    public void test(){
        log.info(employeeEncrytorService.decryptPwd("eDcp0Y6NEyw="));
    }

}

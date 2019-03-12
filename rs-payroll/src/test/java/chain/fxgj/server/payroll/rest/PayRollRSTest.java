package chain.fxgj.server.payroll.rest;

import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.response.IndexDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.StringUtils;
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

import static org.junit.Assert.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
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

    @Before
    public void before() throws Exception {
        final WebTestClient.Builder builder = WebTestClient.bindToApplicationContext(context)
                .configureClient();
        final SpringBootWebTestClientBuilderCustomizer builderCustomizer =
                new SpringBootWebTestClientBuilderCustomizer(Lists.newArrayList(codecCustomizer));
        //将自定义的序列化配置应用到webTestCliend
        builderCustomizer.customize(builder);
        this.webTestClient = builder.baseUrl("http://localhost:8080/")
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
    public void index() {
        login();
        webTestClient.get().uri("/roll/index")
                .header("jsession_id", sessionId)
                .exchange().expectStatus().isOk().expectBody(IndexDTO.class)
                .consumeWith(document("roll_index", relaxedResponseFields(JavaDocReader.javaDoc(IndexDTO.class)))).
                consumeWith(body ->
                        log.info("++++++++++{}", body.getResponseBody())
                );
    }

    @Test
    public void groupList() {
    }

    @Test
    public void entEmp() {
    }

    @Test
    public void wageList() {
    }

    @Test
    public void wageDetail() {
    }

    @Test
    public void empInfo() {
    }

    @Test
    public void invoice() {
    }

    @Test
    public void checkPwd() {
    }

    @Test
    public void checkCard() {
    }

    @Test
    public void emp() {
    }

    @Test
    public void empEnt() {
    }

    @Test
    public void empCard() {
    }

    @Test
    public void empCardLog() {
    }

    @Test
    public void entPhone() {
    }

    @Test
    public void entUser() {
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
        }
    }
}
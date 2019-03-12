package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.dto.request.Req100302;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

/**
 * @Author: hdl
 * @Date: 2019/3/12 15:37
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class InsideRSTest extends BaseRSTest {

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    public ApplicationContext context;

    public WebTestClient webTestClient;

    @Autowired
    @Qualifier("jacksonCodecCustomizer")
    public CodecCustomizer codecCustomizer;

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
    public void sendCode() {
//        Req100302 req100302 = new Req100302();
//        req100302.setBusiType("0");
//        req100302.setPhone("15527689216");
//        webTestClient.post().uri("/inside/sendCode")
//                .contentType(MediaType.APPLICATION_JSON)
//                .syncBody(req100302)
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody()
    }

    @Test
    public void receipt() {
    }

    @Test
    public void readWage() {
    }

    @Test
    public void bandWX() {
    }

    @Test
    public void rz() {
    }

    @Test
    public void setPwd() {
    }

    @Test
    public void updPwd() {
    }

    @Test
    public void checkPhoneCode() {
    }

    @Test
    public void updPhone() {
    }

    @Test
    public void updBankCard() {
    }

    @Test
    public void bankCardIsNew() {
    }
}
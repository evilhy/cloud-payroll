package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.request.ReqMiniInfo;
import chain.fxgj.server.payroll.dto.response.Res100705;

import chain.fxgj.server.payroll.dto.response.ResMiniUserInfo;
import core.dto.wechat.WeixinJsapiDTO;
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

import java.time.Duration;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

/**
 * BaseRS Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Jan 30, 2019</pre>
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class MiniRsTest {
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
                .responseTimeout(Duration.ofSeconds(60))
                .filter(documentationConfiguration(restDocumentation))
                .build();
    }

    @After
    public void after() throws Exception {

    }

    /**
     * 根据code 获取用户信息
     *
     */
    @Test
    public void miniUserInfo() throws Exception {
        webTestClient.get()
                .uri("/mini/miniUserInfo?code={code}&appPartner={appPartner}",
                        "b1610e49e63a692c5543f9dc000058ec75b4aeb7", "FXGJ")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("mini_miniUserInfo",
                        relaxedRequestParameters(parameterWithName("code").description("code")
                                , parameterWithName("appPartner").description("渠道")),
                        relaxedResponseFields(JavaDocReader.javaDoc(ResMiniUserInfo.class))));//出参对象描述
    }

    /**
     * 获取手机号
     */
    @Test
    public void getPhoneNumber() throws Exception {
        ReqMiniInfo reqMiniInfo = new ReqMiniInfo();
        reqMiniInfo.setJsessionId("123123");
        webTestClient.post().uri("/mini/getPhoneNumber")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(reqMiniInfo)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("mini_getPhoneNumber",
                        relaxedRequestFields(JavaDocReader
                                .javaDoc(ResMiniUserInfo.class))));
    }

    /**
     * Junit 获取code再待测试
     * 微信回调接口
     *
     * @throws Exception
     */
    @Test
    public void wxCallback() throws Exception {

        webTestClient.get()
                .uri("/weixin/wxCallback?code={code}&wageSheetId={wageSheetId}&routeName={routeName}&=appPartner={appPartner}",
                        "001xIfAi00Bffp1Ph7yi0Y9mAi0xIfAF", "2", "3","FXGJ")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(document("wxCallback_wechat",
                        relaxedRequestParameters(parameterWithName("code").description("code"),
                                parameterWithName("wageSheetId").description("wageSheetId"),
                                parameterWithName("routeName").description("routeName")),
                        relaxedResponseFields(JavaDocReader.javaDoc(Res100705.class))));//出参对象描述
    }

    /**
     * Junit ok
     * JS分享产生分享签名
     *
     * @throws Exception
     */
    @Test
    public void getJsapiSignature() throws Exception {

        webTestClient.get().uri("/weixin/getJsapiSignature?url={url}", "http://www.baidu.com")
                .header("id", "zo")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(WeixinJsapiDTO.class)
                .consumeWith(document("getJsapiSignature_wechat",
                        relaxedRequestParameters(parameterWithName("url").description("获取url")),
                        relaxedResponseFields(JavaDocReader.javaDoc(WeixinJsapiDTO.class))));
    }

}

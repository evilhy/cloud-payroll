package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.dto.securities.request.ReqSecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecInvAwardDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesCustInfoDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesIntegralRewardDTO;
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
public class SecuritiesControllerTest {
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
    public void loginCheck() throws Exception {
        webTestClient.get()
                .uri("/securityes/loginCheck?code={code}&appPartner={appPartner}",
                        "b1610e49e63a692c5543f9dc000058ec75b4aeb7", "FXGJ")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("securityes_loginCheck",
                        relaxedRequestParameters(parameterWithName("code").description("code")
                                , parameterWithName("appPartner").description("渠道")),
                        relaxedResponseFields(JavaDocReader.javaDoc(SecuritiesCustInfoDTO.class))));//出参对象描述
    }

    /**
     * 登录
     */
    @Test
    public void securitiesLogin() throws Exception {
        String sessionId = "321321321";
        ReqSecuritiesLoginDTO reqSecuritiesLoginDTO = new ReqSecuritiesLoginDTO();
        reqSecuritiesLoginDTO.setMsgCode("123");
        reqSecuritiesLoginDTO.setCustomerId("");
        reqSecuritiesLoginDTO.setInvitationId("123123123");
        reqSecuritiesLoginDTO.setPhone("13400000000");
        webTestClient.post()
                .uri("/securityes/securitiesLogin")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(reqSecuritiesLoginDTO)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()//返回是什么类型的对象
                .consumeWith(document("securityes_securitiesLogin"));
//                        relaxedRequestFields(JavaDocReader.javaDoc(IntentRequestDTO.class))));
    }


    /**
     * Junit ok
     * 邀请奖励列表查询
     *
     * @throws Exception
     */
    @Test
    public void qryInvitationAward() throws Exception {
        webTestClient.get()
                .uri("/securityes/qryInvitationAward?custIdOrManagerId={custIdOrManagerId}",
                        "202002230020757")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("securityes_qryInvitationAward",
                                relaxedRequestParameters(parameterWithName("custIdOrManagerId").description("客户或经理id")),
                        relaxedResponseFields(JavaDocReader.javaDoc(SecInvAwardDTO.class))));//出参对象描述
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


    /**
     * 证券开户活动我的邀请查询
     *
     * @throws Exception
     */
    @Test
    public void querySecuritiesInvitation() {
        String managerId = "ff80808166c9dcbc0166c9dcd0490056";
        webTestClient.get()
                .uri("/securityes/unAuth/querySecuritiesInvitation?managerId={managerId}", managerId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()//返回是什么类型的对象
                .consumeWith(document("securityes_querySecuritiesInvitation",
                        relaxedRequestParameters(parameterWithName("managerId").description("客户经理ID")),
                        relaxedResponseFields(JavaDocReader.javaDoc(SecuritiesIntegralRewardDTO.class))
                    )
                );//出参对象描述
    }

    /**
     * 证券工分邀请奖励列表查询
     *
     * @throws Exception
     */
    @Test
    public void queryInvitationIntegral() {
        String managerId = "edb79b9de9e44202b9dd69592c3c4b9b";
        webTestClient.get()
                .uri("/securityes/unAuth/queryInvitationIntegral?managerId={managerId}", managerId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()//返回是什么类型的对象
                .consumeWith(document("securityes_queryInvitationIntegral",
                        relaxedRequestParameters(parameterWithName("managerId").description("客户经理ID")),
                        relaxedResponseFields(JavaDocReader.javaDoc(SecuritiesIntegralRewardDTO.class))
                        )
                );//出参对象描述
    }

//    @Test
//    public void qryGoldenBean() throws Exception {
//        webTestClient.get().uri("/securityes/qryGoldenBean?custId={custId}", "123123123123")
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody()
//                .consumeWith(document("securityes_qryGoldenBean",
//                        relaxedRequestParameters(parameterWithName("custId").description("客户id"))));
//    }
//
//    @Test
//    public void qryDataSynTime() throws Exception {
//        webTestClient.get().uri("/securityes/qryDataSynTime")
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody()
//                .consumeWith(document("securityes_qryDataSynTime"));
//    }
//
//    @Test
//    public void qryOpenRewardList() throws Exception {
//
//        webTestClient.get().uri("/securityes/qryOpenRewardList?custId={custId}", "1231231231234")
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody()
//                .consumeWith(document("securityes_qryOpenRewardList",
//                        relaxedRequestParameters(parameterWithName("custId").description("客户id")),
//                        relaxedResponseFields().andWithPrefix("List.[]",JavaDocReader.javaDoc(SecuritiesOpenRewardDTO1.class))));
//    }
//
//    @Test
//    public void qryInvestmentRewardList() throws Exception {
//        SecuritiesRewardReqDTO1 securitiesRewardReqDTO1 = SecuritiesRewardReqDTO1.builder()
//                .custId("123123123")
//                .managerId("")
//                .build();
//        webTestClient.post().uri("/securityes/qryInvestmentRewardList")
//                .contentType(MediaType.APPLICATION_JSON)
//                .syncBody(securitiesRewardReqDTO1)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .consumeWith(document("securityes_qryInvestmentRewardList",
//                        relaxedRequestParameters(parameterWithName("custId").description("客户id")),
//                        relaxedResponseFields().andWithPrefix("List.[]", JavaDocReader.javaDoc(SecuritiesRewardResDTO1.class))));
//    }
}

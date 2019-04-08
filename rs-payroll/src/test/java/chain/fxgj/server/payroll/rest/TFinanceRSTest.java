package chain.fxgj.server.payroll.rest;

import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.PageResponseDTO;
import chain.fxgj.server.payroll.dto.response.NewestWageLogDTO;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.dto.tfinance.*;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
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
public class TFinanceRSTest{
    public static String sessionId="5b4b10fb6a70467fa289f16896ae9113";
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    public ApplicationContext context;

    public WebTestClient webTestClient;
    @Autowired
    EmpWechatService empWechatService;

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
    /**
     * Junit ok
     * 活动产品列表
     */
    @Test
    public void list() throws Exception {
        login();
        webTestClient.get()
                .uri("/tfinance/list")
                .header("jsession_id", sessionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("list_tfinance",
                        relaxedResponseFields(fieldWithPath("[]").description("理财产品"))
                                .andWithPrefix("[]", JavaDocReader.javaDoc(ProductDTO.class))));//出参对象描述

    }

    /** Junit ok
     * 同事团理财产品
     * @throws Exception
     */
    @Test
    public void productInfo() throws Exception {
        login();
        webTestClient.get()
                .uri("/tfinance/product?productId={productId}&entId={entId}&channel={channel}&fxId={fxId}",
                        "ff808081671710a301671bc90aaf0005","123","0","234")
                .header("jsession_id", sessionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("product_tfinance",
                        relaxedRequestParameters(
                                parameterWithName("productId").description("产品Id"),
                                parameterWithName("entId").description("企业Id"),
                                parameterWithName("channel").description("渠道(0公众号菜单 1banner 2分享)"),
                                parameterWithName("fxId").description("分享人id")),
                        relaxedResponseFields(JavaDocReader.javaDoc(ProductInfoDTO.class))
                                .andWithPrefix("markList.[].",JavaDocReader.javaDoc(ProductInfoDTO.ProductMarkDTO.class))));//出参对象描述

    }

    /**
     * 平台产品预约列表
     * @throws Exception
     */
    @Test
    public void intentionList() throws Exception {
        login();
        webTestClient.get()
                .uri("/tfinance/intentionList?productId={productId}",
                        "ff808081671710a301671bc90aaf0005")
                .header("jsession_id", sessionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("intentionList_tfinance",
                        relaxedRequestParameters(
                                parameterWithName("productId").description("产品Id")),
                        relaxedResponseFields(JavaDocReader.javaDoc(IntentListDTO.class))
                                .andWithPrefix("list.[].",JavaDocReader.javaDoc(IntentListDTO.IntentRealDTO.class))));//出参对象描述

    }

    /**
     * 操作列表
     * @throws Exception
     */
    @Test
    public void operateList() throws Exception {
        login();
        webTestClient.get()
                .uri("/tfinance/operateList?productId={productId}&entId={entId}&operate={operate}",
                        "ff8080816900035b016904d3b6e2003d","ff80808168a8503e0168da9830c9000a","0")
                .header("jsession_id", sessionId)
                .header("page", "1")
                .header("size", "10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("operateList_tfinance",
                        relaxedRequestParameters(
                                parameterWithName("productId").description("产品Id"),
                                parameterWithName("entId").description("企业Id"),
                                parameterWithName("operate").description("操作类型 0 浏览 1预约")),
                        relaxedResponseFields(JavaDocReader.javaDoc(PageResponseDTO.class))));//出参对象描述
    }

    /**
     * 预约明细
     * @throws Exception
     */
    @Test
    public void intentInfo() throws Exception {
        login();
        webTestClient.get()
                .uri("/tfinance/intentInfo?productId={productId}",
                        "ff8080816707679e01670b07a4a30000")
                .header("jsession_id", sessionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("intentInfo_tfinance",
                        relaxedRequestParameters(
                                parameterWithName("productId").description("产品Id")),
                        relaxedResponseFields(JavaDocReader.javaDoc(IntentInfoDTO.class))
                                .andWithPrefix("list.[].",JavaDocReader.javaDoc(IntentInfoDTO.WechatUser.class))));//出参对象描述
    }

    /**
     * 预约人信息
     * @throws Exception
     */
    @Test
    public void userInfo() throws Exception {
        login();
        webTestClient.get()
                .uri("/tfinance/userInfo?entId={entId}",
                        "ff80808168a8503e0168da9830c9000a")
                .header("jsession_id", sessionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("userInfo_tfinance",
                        relaxedRequestParameters(
                                parameterWithName("entId").description("企业Id")),
                        relaxedResponseFields(JavaDocReader.javaDoc(UserInfoDTO.class))));//出参对象描述
    }

    /**
     * 预约产品
     * @throws Exception
     */
    @Test
    public void intent() throws Exception {
        login();
        IntentRequestDTO intentRequestDTO = new IntentRequestDTO();
        intentRequestDTO.setProtocol(1);
        intentRequestDTO.setProductId("ff808081671710a301671bc90aaf0005");
        intentRequestDTO.setClientName("张张");
        intentRequestDTO.setIdNumber("420704199304164673");
        intentRequestDTO.setClientPhone("13333333333");
        webTestClient.post()
                .uri("/tfinance/intent")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(intentRequestDTO)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IntentRequestDTO.class)//返回是什么类型的对象
                .consumeWith(document("intent_tfinance",
                        relaxedRequestFields(JavaDocReader.javaDoc(IntentRequestDTO.class))));
    }

    /**
     * 获取Code的url
     * @throws Exception
     */
    @Test
    public void codeUrl() throws Exception {
        login();
        webTestClient.get()
                .uri("/tfinance/codeUrl?redirectUrl={redirectUrl}",
                        "www.baidu.com")
                .header("jsession_id", sessionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("codeUrl_tfinance",
                        relaxedRequestParameters(parameterWithName("redirectUrl").description("跳转url"))));//出参对象描述
    }
} 

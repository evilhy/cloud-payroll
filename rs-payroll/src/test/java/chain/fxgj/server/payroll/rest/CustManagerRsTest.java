package chain.fxgj.server.payroll.rest;

import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.PageResponseDTO;
import chain.fxgj.server.payroll.dto.request.DistributeDTO;
import chain.fxgj.server.payroll.dto.response.ManagerInfoDTO;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.dto.tfinance.*;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.outside.common.dto.wechat.WeixinJsapiDTO;
import chain.utils.commons.UUIDUtil;
import chain.wechat.client.feign.IwechatFeignService;
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
public class CustManagerRsTest {
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
     * 查询客户经理信息
     */
    @Test
    public void manager() throws Exception {
        login();
        webTestClient.get()
                .uri("/manager/managerInfo")
                .header("jsession_id", sessionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("managerInfo_custmanager",
                        relaxedResponseFields(JavaDocReader.javaDoc(ManagerInfoDTO.class))));//出参对象描述

    }

    /**
     * 通知企业分配客户经理 入库
     * @throws Exception
     */
    @Test
    public void intent() throws Exception {
        login();
        DistributeDTO distributeDTO = new DistributeDTO();
        distributeDTO.setGroupId("ff8080816899924301689e0297240001");
        webTestClient.post()
                .uri("/manager/distribute")
                .header("jsession_id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(distributeDTO)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IntentRequestDTO.class)//返回是什么类型的对象
                .consumeWith(document("distribute_custmanager",
                        relaxedRequestFields()));
    }

} 

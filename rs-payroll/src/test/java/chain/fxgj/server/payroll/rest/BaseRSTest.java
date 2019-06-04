package chain.fxgj.server.payroll.rest;

import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.SpringBootWebTestClientBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

/**
 * BaseRS Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Jan 30, 2019</pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@Transactional
@Rollback(false)
@Slf4j
public class BaseRSTest {

    public static String sessionId = "5b4b10fb6a70467fa289f16896ae9113";

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
                .filter(documentationConfiguration(restDocumentation))
                .build();
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: upload(@NotNull @RequestParam("file") MultipartFile uploadfile)
     */
    @Test
    public void testUpload() throws Exception {
//        webTestClient.post().uri("").exchange().expectStatus().
//                isOk().expectBody(UploadResponse.class).
//                consumeWith(document("baseRS_upload",requestParameters()));
    }

    /**
     * Method: dictItem(@PathVariable("id") String auth)
     */

    public void login() {
        sessionId = UUIDUtil.createUUID32();
        String openId = "oFnSLv0w4nipfwH0hFBwBimXjleY";
        String nickName = "%E9%9F%A9%E5%BE%B7%E8%89%AF";
        String headImg = "http://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTK2TdCQXNqUrzY9u3SFgRLdI5kOs0yh3jHrwEzic8n5tB9RDHHMqNsOX8l06rVAibVHHsrA273wwwjw/132";
        String idNumber = "420704199304164673";
        try {
            UserPrincipal userPrincipal = empWechatService.setWechatInfo(sessionId, openId, nickName, headImg, idNumber);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

} 

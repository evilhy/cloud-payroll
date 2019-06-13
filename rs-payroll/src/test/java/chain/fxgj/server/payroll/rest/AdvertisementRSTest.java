package chain.fxgj.server.payroll.rest;

import chain.fxgj.core.common.service.WechatBindService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.SpringBootWebTestClientBuilderCustomizer;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

/**
 * XmWorkerProjectRS Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Mar 14, 2019</pre>
 */
@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class AdvertisementRSTest extends BaseRSTest {
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    public ApplicationContext context;

    public WebTestClient webTestClient;

    @Autowired
    public WechatBindService wechatBindService;

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

    @Test
    public void rotation() throws Exception {
//        webTestClient.get().uri("/advertising/rotation?channelId={channelId}", "0")
//                .exchange().expectStatus().isOk()
//                .expectBody()
//                .consumeWith(document("advertising_rotation",
//                        relaxedRequestParameters(parameterWithName("channelId").description("渠道id(0 放薪管家web,1 放薪经理 2 微信工资条 3 放薪虎符)")),
//                        relaxedResponseFields(JavaDocReader.javaDoc(AdvertisingRotationDTO.class))));


       // wechatBindService.getEntInfos("420625198410100050");
    }


}

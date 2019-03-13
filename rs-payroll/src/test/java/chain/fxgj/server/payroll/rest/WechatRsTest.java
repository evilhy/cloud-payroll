package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.outside.common.dto.wechat.WeixinAuthorizeUrlDTO;
import chain.outside.common.dto.wechat.WeixinExtResponeDTO;
import chain.outside.common.dto.wechat.WeixinJsapiDTO;
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
import java.util.List;

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
public class WechatRsTest {
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    public ApplicationContext context;

    public WebTestClient webTestClient;

    @Autowired
    IwechatFeignService iwechatFeignService;

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
     * Junit ok
     * 验证消息的确来自微信服务器
     */
    @Test
    public void signatureGet() throws Exception {
        webTestClient.get()
                .uri("/weixin/signature?signature={signature}&timestamp={timestamp}&nonce={nonce}&echostr={echostr}&id={id}",
                        "b1610e49e63a692c5543f9dc000058ec75b4aeb7", "1551701634", "631307959", "2142728365402838963","zo")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("signatureGet_wechat",
                        relaxedRequestParameters(parameterWithName("id").description("微信公众号分组id")
                                ,parameterWithName("signature").description("微信加密签名")
                                ,parameterWithName("timestamp").description("时间戳")
                                ,parameterWithName("nonce").description("随机数")
                                ,parameterWithName("echostr").description("随机字符串")),
                        relaxedResponseFields()));//出参对象描述

    }

    /**
     *Junit ok
     * 用户发送信息->微信接收信息后再调用->后台服务(后台验签，通过后返回对应消息)->微信->用户
     * </p>
     * 验证消息的确来自微信服务器 然后发送消息
     */
    @Test
    public void signaturePost() throws Exception {
        String xml = "<xml><ToUserName><![CDATA[gh_2daa360513df]]></ToUserName>\n" +
                "<FromUserName><![CDATA[oikrq5giuCd4Rw4qG3fYY3sxn2sI]]></FromUserName>\n" +
                "<CreateTime>1551779035</CreateTime>\n" +
                "<MsgType><![CDATA[text]]></MsgType>\n" +
                "<Content><![CDATA[0]]></Content>\n" +
                "<MsgId>22216132666205741</MsgId>\n" +
                "<Event>subscribe</Event>\n" +
                "</xml>";

        webTestClient.post()
                .uri("/weixin/signature?signature={signature}&timestamp={timestamp}&nonce={nonce}&echostr={echostr}&id={id}",
                        "b1610e49e63a692c5543f9dc000058ec75b4aeb7", "1551701634", "631307959","2142728365402838963","zo")
                .contentType(MediaType.TEXT_XML)
                .accept(MediaType.TEXT_XML)
                .syncBody(xml)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(document("signaturePost_wechat",
                        relaxedRequestParameters(parameterWithName("signature").description("微信加密签名")),
                        relaxedRequestParameters(parameterWithName("timestamp").description("时间戳")),
                        relaxedRequestParameters(parameterWithName("nonce").description("随机数")),
                        relaxedRequestParameters(parameterWithName("echostr").description("随机字符串")),
                        relaxedResponseFields()));
    }

    /**
     * Junit 获取code再待测试
     * 微信回调接口
     * @throws Exception
     */
    @Test
    public void wxCallback() throws Exception {
//        WeixinAuthorizeUrlDTO weixinAuthorizeUrlDTO = new WeixinAuthorizeUrlDTO();
//        weixinAuthorizeUrlDTO.setUrl("www.baidu.com");
//        WeixinAuthorizeUrlDTO zo = iwechatFeignService.getOAuthUrl("zo", weixinAuthorizeUrlDTO);
//        String url = zo.getUrl();

        webTestClient.get()
                .uri("/weixin/wxCallback?code={code}&wageSheetId={wageSheetId}&routeName={routeName}",
                        "011pSDcu1rWfyf0Toacu1fKJcu1pSDcS","2","3")
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

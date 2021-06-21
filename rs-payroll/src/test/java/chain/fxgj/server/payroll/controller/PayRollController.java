package chain.fxgj.server.payroll.controller;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.request.SignedSaveReq;
import chain.fxgj.server.payroll.dto.response.WageDetailDTO;
import chain.fxgj.server.payroll.dto.response.WageHeadDTO;
import chain.fxgj.server.payroll.service.EmployeeEncrytorService;
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

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/6/18 14:37
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class PayRollController {

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
     * 校验并保存密码
     */
    @Test
    public void saveSigned() {
        SignedSaveReq req = SignedSaveReq.builder()
                .sign("data:image/jpg;base64,/9j/4AAQSkZJRgABAQEBLAEsAAD/4QA6RXhpZgAATU0AKgAAAAgAA1EQAAEAAAABAQAAAFERAAQAAAABAAAdh1ESAAQAAAABAAAdhwAAAAD+X/M5v7VitFR203XQ//9k=")
                .wageDetailId(UUIDUtil.createUUID32())
                .wageSheetId(UUIDUtil.createUUID32())
                .build();

        webTestClient.post().uri("/roll/saveSigned")
                .syncBody(req).exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("roll_saveSigned",
                        relaxedRequestFields(JavaDocReader.javaDoc(SignedSaveReq.class))
                ));
    }

    @Test
    public void wageDetail() {
        String wageSheetId = UUIDUtil.createUUID32();
        String groupId = UUIDUtil.createUUID32();
        webTestClient.get()
                .uri("/roll/wageDetail?wageSheetId={wageSheetId}&groupId={groupId}", wageSheetId, groupId)
                .header("jsession-id", UUIDUtil.createUUID32())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("roll_wageDetail",
                        relaxedResponseFields(JavaDocReader.javaDoc(WageDetailDTO.class))
                                .andWithPrefix("wageShowDTO.", JavaDocReader.javaDoc(WageDetailDTO.WageShowDTO.class))
                                .andWithPrefix("content.[].", JavaDocReader.javaDoc(WageDetailDTO.Content.class))
                                .andWithPrefix("wageHeadDTO.", JavaDocReader.javaDoc(WageHeadDTO.class))
                                .andWithPrefix("wageHeadDTO.heads.[]", JavaDocReader.javaDoc(WageHeadDTO.Cell.class))
                ));
    }
}

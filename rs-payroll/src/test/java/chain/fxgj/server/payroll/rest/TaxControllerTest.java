package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.tax.*;
import chain.utils.commons.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/23 15:13
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class TaxControllerTest extends BaseTestCase {

    /**
     * 签约详情
     *
     * @throws Exception
     */
    @Test
    public void signingDetails() throws Exception {
        String salt = UUIDUtil.createUUID32();
        String passwd = UUIDUtil.createUUID32();
        String entId = UUIDUtil.createUUID32();
        webTestClient.get().uri("/tax/signingDetails?withdrawalLedgerId={withdrawalLedgerId}", UUIDUtil.createUUID8())
                .header("encry-salt", salt)
                .header("encry-passwd", passwd)
                .header("ent-id", entId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("tax_signingDetails",
                        relaxedRequestParameters(parameterWithName("withdrawalLedgerId").description("提现台账ID")),
                        relaxedResponseFields(JavaDocReader.javaDoc(SigningDetailsReq.class))
                ));
    }

    /**
     * 身份证上传
     */
    /**
     * 身份证上传
     */
    @Test
    public void upload() {
        String salt = UUIDUtil.createUUID32();
        String passwd = UUIDUtil.createUUID32();
        String entId = UUIDUtil.createUUID32();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        HttpEntity<ClassPathResource> entity =
                new HttpEntity<>(new ClassPathResource("1.jpg"), headers);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", entity);
        webTestClient.post().uri("/tax/upload").contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .header("encry-salt", salt)
                .header("encry-passwd", passwd)
                .header("ent-id", entId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("tax_upload",
                        requestParts(partWithName("file").description("文件内容")),
                        relaxedResponseFields(JavaDocReader.javaDoc(UploadDto.class))
                ));
    }

    /**
     * 签约
     *
     * @throws Exception
     */
    @Test
    public void signing() throws Exception {
        SigningDetailsReq build = SigningDetailsReq.builder()
                .idCardFront("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fnewpic.jxnews.com.cn%2F0%2F11%2F41%2F88%2F11418823_708254.jpg&refer=http%3A%2F%2Fnewpic.jxnews.com.cn&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1632293398&t=cecf694f548c5a955b1a523ef9f62bf0")
                .idCardNegative("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.legaldaily.com.cn%2Flocality%2Fimages%2F2012-05%2F03%2F002511f36021110c6ade26.jpg&refer=http%3A%2F%2Fwww.legaldaily.com.cn&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1632293440&t=bf974772ad97bbdd3d4f905f1a2b9f89")
                .idNumber("420110199809095678")
                .idType("1")
                .idTypeVal("身份证")
                .phone("18600020000")
                .userName("李慕白")
                .taxSignId(UUIDUtil.createUUID32())
                .provinceCode("420000000000")
                .provinceName("湖北省")
                .cityCode("420100000000")
                .cityName("武汉市")
                .areaCode("420106000000")
                .areaName("武昌区")
                .streetCode("420106011000")
                .streetName("中南路街道")
                .build();

        webTestClient.post().uri("/tax/signing")
                .contentType(MediaType.APPLICATION_JSON)
                .header("encry-salt", "123456")
                .header("encry-passwd", "234567")
                .syncBody(build)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()//返回是什么类型的对象
                .consumeWith(document("tax_signing",
                        //相应文档
                        relaxedRequestFields(JavaDocReader.javaDoc(SigningDetailsReq.class)),
                        relaxedResponseFields(JavaDocReader.javaDoc(H5UrlDto.class))
                ));
    }

    /**
     * 身份认证
     *
     * @throws Exception
     */
    @Test
    public void attest() throws Exception {
        SigningDetailsReq build = SigningDetailsReq.builder()
                .idCardFront("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fnewpic.jxnews.com.cn%2F0%2F11%2F41%2F88%2F11418823_708254.jpg&refer=http%3A%2F%2Fnewpic.jxnews.com.cn&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1632293398&t=cecf694f548c5a955b1a523ef9f62bf0")
                .idCardNegative("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.legaldaily.com.cn%2Flocality%2Fimages%2F2012-05%2F03%2F002511f36021110c6ade26.jpg&refer=http%3A%2F%2Fwww.legaldaily.com.cn&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1632293440&t=bf974772ad97bbdd3d4f905f1a2b9f89")
                .idNumber("420110199809095678")
                .idType("1")
                .idTypeVal("身份证")
                .phone("18600020000")
                .userName("李慕白")
                .taxSignId(UUIDUtil.createUUID32())
                .provinceCode("420000000000")
                .provinceName("湖北省")
                .cityCode("420100000000")
                .cityName("武汉市")
                .areaCode("420106000000")
                .areaName("武昌区")
                .streetCode("420106011000")
                .streetName("中南路街道")
                .address("万金国际广场902室")
                .build();

        webTestClient.post().uri("/tax/attest")
                .contentType(MediaType.APPLICATION_JSON)
                .header("encry-salt", "123456")
                .header("encry-passwd", "234567")
                .syncBody(build)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()//返回是什么类型的对象
                .consumeWith(document("tax_attest",
                        //相应文档
                        relaxedRequestFields(JavaDocReader.javaDoc(SigningDetailsReq.class))
                ));
    }

    /**
     * 签约结果推送
     *
     * @throws Exception
     */
    @Test
    public void signResultPush() throws Exception {
        SignResultPushReq build = SignResultPushReq.builder()
                .isAuth(true)
                .transUserId(UUIDUtil.createUUID32())
                .build();

        webTestClient.post().uri("/tax/signResultPush")
                .contentType(MediaType.APPLICATION_JSON)
                .header("encry-salt", "123456")
                .header("encry-passwd", "234567")
                .syncBody(build)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()//返回是什么类型的对象
                .consumeWith(document("tax_signResultPush",
                        //相应文档
                        relaxedRequestFields(JavaDocReader.javaDoc(SignResultPushReq.class)),
                        relaxedResponseFields(JavaDocReader.javaDoc(SealUserRes.class))
                ));
    }

    /**
     * 签约记录查看
     */
    @Test
    public void signRecord() throws Exception {
        webTestClient.get()
                .uri("/tax/signRecord?taxSignId={taxSignId}", "b1610e49e63a692c5543f9dc000058ec75b4aeb7")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)//返回是什么类型的对象
                .consumeWith(body -> log.info(body.getResponseBody()))
                .consumeWith(document("tax_signRecord",
                        relaxedRequestParameters(parameterWithName("taxSignId").description("签约信息ID")),
                        relaxedResponseFields(JavaDocReader.javaDoc(H5UrlDto.class))//出参对象描述
                ));
    }

    /**
     * 认证详情
     *
     * @throws Exception
     */
    @Test
    public void taxAttestDetail() throws Exception {
        String salt = UUIDUtil.createUUID32();
        String passwd = UUIDUtil.createUUID32();
        String entId = UUIDUtil.createUUID32();
        webTestClient.get().uri(("/tax/taxAttestDetail"))
                .header("encry-salt", salt)
                .header("encry-passwd", passwd)
                .header("ent-id", entId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("tax_taxAttestDetail",
//                        relaxedRequestParameters(parameterWithName("withdrawalLedgerId").description("提现台账ID")),
                        relaxedResponseFields(JavaDocReader.javaDoc(TaxAttestDetailRes.class))
                ));
    }

    /**
     * 待签约列表
     *
     * @throws Exception
     */
    @Test
    public void signingList() throws Exception {
        String salt = UUIDUtil.createUUID32();
        String passwd = UUIDUtil.createUUID32();
        String entId = UUIDUtil.createUUID32();
        String templateId = UUIDUtil.createUUID32();
        webTestClient.get().uri("/tax/signingList?templateId={templateId}", templateId)
                .header("encry-salt", salt)
                .header("encry-passwd", passwd)
                .header("ent-id", entId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("tax_signingList",
                        relaxedRequestParameters(parameterWithName("templateId").description("协议模板ID")),
                        relaxedResponseFields(JavaDocReader.javaDoc(TaxAttestDetailRes.class))
                ));
    }
}

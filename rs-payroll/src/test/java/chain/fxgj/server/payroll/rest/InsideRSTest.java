package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.JavaDocReader;
import chain.fxgj.server.payroll.dto.testfile.IndexEmpEntDTOP;
import core.dto.request.BaseReqDTO;
import core.dto.response.index.IndexEmpEntDTO;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;


@FixMethodOrder(MethodSorters.JVM)
public class InsideRSTest extends BaseTestCase{

    @Test
    public void empEntList() throws Exception {
        BaseReqDTO baseReqDTO = new BaseReqDTO();
        baseReqDTO.setIdNumber("1");
        webTestClient.post().uri("/inside/empEntList")
                .contentType(MediaType.APPLICATION_JSON)
                .header("id", "fxgj")
                .syncBody(baseReqDTO)//入参
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IndexEmpEntDTO.class)//返回是什么类型的对象
                .consumeWith(document("inside_empEntList",
                        //相应文档
//                        relaxedRequestFields(JavaDocReader.javaDoc(BaseReqDTO.class)),
                        relaxedResponseFields(JavaDocReader.javaDoc(IndexEmpEntDTOP.class))));
    }
}
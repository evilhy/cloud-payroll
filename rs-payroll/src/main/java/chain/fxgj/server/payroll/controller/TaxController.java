package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ServiceHandleException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.server.payroll.dto.tax.SignResultPushReq;
import chain.fxgj.server.payroll.dto.tax.SigningDetailsReq;
import chain.fxgj.server.payroll.dto.tax.UploadDto;
import chain.fxgj.server.payroll.service.TaxService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.utils.commons.UUIDUtil;
import core.dto.ErrorConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.constraints.NotNull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/23 11:44
 */
@RestController
@Validated
@RequestMapping(value = "/tax")
@Slf4j
@SuppressWarnings("unchecked")
public class TaxController {

    @Autowired
    TaxService taxService;
    @Autowired
    PayrollProperties payrollProperties;

    /**
     * 签约详情
     *
     * @return
     */
    @GetMapping("/signingDetails")
    @TrackLog
    public Mono<SigningDetailsReq> signingDetails() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

//        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return SigningDetailsReq.builder()
                    .IdCardFront("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fnewpic.jxnews.com.cn%2F0%2F11%2F41%2F88%2F11418823_708254.jpg&refer=http%3A%2F%2Fnewpic.jxnews.com.cn&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1632293398&t=cecf694f548c5a955b1a523ef9f62bf0")
                    .IdCardNegative("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.legaldaily.com.cn%2Flocality%2Fimages%2F2012-05%2F03%2F002511f36021110c6ade26.jpg&refer=http%3A%2F%2Fwww.legaldaily.com.cn&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1632293440&t=bf974772ad97bbdd3d4f905f1a2b9f89")
                    .idNumber("420110199809095678")
                    .idType("1")
                    .phone("18600020000")
                    .userName("李慕白")
                    .taxSignId(UUIDUtil.createUUID32())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 身份证上传
     *
     * @param uploadfile 文件流
     * @return
     * @throws BusiVerifyException
     */
    @PostMapping("/upload")
    @TrackLog
    public Mono<UploadDto> upload(@NotNull @RequestPart("file") FilePart uploadfile) throws BusiVerifyException {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            String fileName = uploadfile.filename();
            log.info("身份证上传 fileName:[{}]", fileName);
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            String filePathName = "idCard-" + Calendar.getInstance().getTimeInMillis();
            Path path = Paths.get(payrollProperties.getSignUploadPath() + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "/");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            Path tempFile = Files.createTempFile(path, filePathName, suffix);
            try {
                //存放文件
                uploadfile.transferTo(tempFile);
                log.info("IdCard upload Success! fileName:{},fileSystem:{}", tempFile.getFileName(), tempFile.getFileSystem());
            } catch (Exception e) {
                throw new ServiceHandleException(e, ErrorConstant.SYS_ERROR.format("文件上传处理异常"));
            }

            return UploadDto.builder()
                    .filepath(tempFile.toString())
                    .build();
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 确认签约
     *
     * @return
     */
    @PostMapping("/signing")
    @TrackLog
    public Mono<Void> signing(@RequestBody SigningDetailsReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

//        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * @return
     */
    @PostMapping("/signResultPush")
    @TrackLog
    public Mono<Void> signResultPush(@RequestBody SignResultPushReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}

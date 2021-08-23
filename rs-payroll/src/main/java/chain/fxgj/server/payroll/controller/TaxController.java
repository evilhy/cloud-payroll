package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ParamsIllegalException;
import chain.css.exception.ServiceHandleException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.server.payroll.dto.tax.*;
import chain.fxgj.server.payroll.service.EmployeeWechatService;
import chain.fxgj.server.payroll.service.TaxService;
import chain.fxgj.server.payroll.util.ImageBase64Utils;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.EmployeeTaxSignFeignService;
import chain.utils.commons.JacksonUtil;
import chain.utils.fxgj.constant.DictEnums.CertTypeEnum;
import chain.utils.fxgj.constant.DictEnums.DelStatusEnum;
import chain.utils.fxgj.constant.DictEnums.TransDealStatusEnum;
import core.dto.ErrorConstant;
import core.dto.request.employeeTaxSign.EmployeeTaxSignQueryReq;
import core.dto.request.employeeTaxSign.EmployeeTaxSignSaveReq;
import core.dto.response.employeeTaxSign.EmployeeTaxSignDTO;
import core.dto.wechat.EmployeeWechatDTO;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    EmployeeWechatService employeeWechatService;
    @Autowired
    TaxService taxService;
    @Autowired
    EmployeeTaxSignFeignService employeeTaxSignFeignService;

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
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String jsessionId = userPrincipal.getSessionId();
        String entId = userPrincipal.getEntId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //查询登陆信息
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId);
            SigningDetailsReq signingDetail = SigningDetailsReq.builder()
//                    .idCardNegative()
//                    .idCardFront()
//                    .taxSignId()
                    .idType("1")
                    .userName(employeeWechatDTO.getName())
                    .phone(employeeWechatDTO.getPhone())
                    .idNumber(employeeWechatDTO.getIdNumber())
                    .build();

            //查询签约信息
            EmployeeTaxSignQueryReq signQueryReq = EmployeeTaxSignQueryReq.builder()
                    .entId(entId)
                    .idNumber(employeeWechatDTO.getIdNumber())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxSignDTO> list = employeeTaxSignFeignService.list(signQueryReq);
            EmployeeTaxSignDTO employeeTaxSignDTO = null;
            if (null != list && list.size() > 0) {
                employeeTaxSignDTO = list.get(0);
                if (TransDealStatusEnum.FAIL != employeeTaxSignDTO.getStatus()) {
                    signingDetail.setTaxSignId(employeeTaxSignDTO.getId());
                    signingDetail.setIdCardFront(employeeTaxSignDTO.getIdCardFront());
                    signingDetail.setIdCardNegative(employeeTaxSignDTO.getIdCardNegative());
                }
            }
            return signingDetail;
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
    public Mono<H5UrlDto> signing(@RequestBody SigningDetailsReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String jsessionId = userPrincipal.getSessionId();
        String entId = userPrincipal.getEntId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //查询登陆信息
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId);

            //查询签约信息
            EmployeeTaxSignQueryReq signQueryReq = EmployeeTaxSignQueryReq.builder()
                    .entId(entId)
                    .idNumber(employeeWechatDTO.getIdNumber())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxSignDTO> list = employeeTaxSignFeignService.list(signQueryReq);
            EmployeeTaxSignDTO employeeTaxSignDTO = null;
            if (null != list && list.size() > 0 && TransDealStatusEnum.FAIL != list.get(0).getStatus()) {
                log.info("=====> 用户在当前企业已进行签约 employeeWechatDTO:{}, signQueryReq:{}", JacksonUtil.objectToJson(employeeTaxSignDTO), JacksonUtil.objectToJson(signQueryReq));
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户在当前企业已进行签约"));
            }

            //保存
            EmployeeTaxSignSaveReq signSaveReq = EmployeeTaxSignSaveReq.builder()
                    .certTypeEnum(CertTypeEnum.PERSONAL_1)
                    .delStatusEnum(DelStatusEnum.normal)
                    .entId(entId)
                    .entName(userPrincipal.getEntName())
                    .idNumber(req.getIdNumber())
                    .phone(req.getPhone())
                    .status(TransDealStatusEnum.ING)
                    .userName(req.getUserName())
                    .build();
            EmployeeTaxSignDTO taxSignDTO = employeeTaxSignFeignService.save(signSaveReq);

            //TODO 图片压缩

            //身份证照片
            String idCardFront = ImageBase64Utils.imageToBase64(req.getIdCardFront());
            String idCardNegative = ImageBase64Utils.imageToBase64(req.getIdCardNegative());

            //验证身份信息
            SealUserReq userReq = SealUserReq.builder()
                    .fwOrg(taxSignDTO.getEntName())
                    .idCardImg1(idCardFront)
                    .idCardImg2(idCardNegative)
                    .idCardNo(req.getIdNumber())
                    .idType("2".equals(req.getIdType()) ? "HZ" : "SFZ")
                    .phoneNo(req.getPhone())
                    .transUserId(taxSignDTO.getId())
                    .userName(req.getUserName())
//                    .ygOrg()
                    .build();
            SealUserRes userResult = taxService.user(userReq);
            if (null == userResult || !"success".equals(userResult.getRntCode())) {
                //验证失败或网络异常
                //删除签约记录
                employeeTaxSignFeignService.delete(taxSignDTO.getId());
            }
            //验证身份信息成功，进入签约
            WalletH5Req walletH5Req = WalletH5Req.builder()
                    .fwOrg(taxSignDTO.getEntId())
                    .idCardNo(taxSignDTO.getIdNumber())
                    .idType(CertTypeEnum.PERSONAL_1 == taxSignDTO.getCertTypeEnum() ? "SFZ" : "HZ")
                    .phoneNo(taxSignDTO.getPhone())
                    .transUserId(taxSignDTO.getId())
                    .userName(taxSignDTO.getUserName())
//                    .ygOrg()
                    .build();
            WalletH5Res walletH5Res = taxService.walletH5(walletH5Req);

            if (null == walletH5Res) {
                log.info("=====> 签约异常 walletH5Req:{}", JacksonUtil.objectToJson(walletH5Req));
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("签约异常"));
            }

            if (walletH5Res.getIsSeal()) {
                signSaveReq = EmployeeTaxSignSaveReq.builder()
                        .status(TransDealStatusEnum.SUCCESS)
                        .id(taxSignDTO.getId())
                        .signDateTime(LocalDateTime.now())
                        .build();
                employeeTaxSignFeignService.save(signSaveReq);
            } else {
                return H5UrlDto.builder()
                        .url(walletH5Res.getUrl())
                        .build();
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 签约结果推送
     *
     * @param req
     * @return
     */
    @PostMapping("/signResultPush")
    @TrackLog
    public Mono<SealUserRes> signResultPush(@RequestBody SignResultPushReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            try {
                Optional.ofNullable(req).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("请求参数不能为空")));
                Optional.ofNullable(req.getIsAuth()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("签约状态不能为空")));
                Optional.ofNullable(req.getTransUserId()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("交易用户id不能为空")));

                //查询签约信息
                EmployeeTaxSignDTO employeeTaxSignDTO = employeeTaxSignFeignService.findById(req.getTransUserId());

                //更新签约结果
                EmployeeTaxSignSaveReq saveReq = null;
                if (req.getIsAuth()) {
                    saveReq = EmployeeTaxSignSaveReq.builder()
                            .id(employeeTaxSignDTO.getId())
                            .status(TransDealStatusEnum.SUCCESS)
                            .signDateTime(LocalDateTime.now())
                            .build();
                } else {
                    saveReq = EmployeeTaxSignSaveReq.builder()
                            .id(employeeTaxSignDTO.getId())
                            .status(TransDealStatusEnum.FAIL)
                            .build();
                }
                employeeTaxSignFeignService.save(saveReq);

            } catch (Exception e) {
                log.info("=====> 更新结果失败 req：{}", JacksonUtil.objectToJson(req));
                return SealUserRes.builder()
                        .rntMsg("失败")
                        .rntCode("fail")
                        .build();
            }
            return SealUserRes.builder()
                    .rntMsg("成功")
                    .rntCode("success")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 签约记录查看
     *
     * @return
     */
    @GetMapping("/signRecord")
    @TrackLog
    public Mono<H5UrlDto> signRecord(@RequestParam("taxSignId") String taxSignId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Optional.ofNullable(taxSignId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户ID不能不管为空")));

            String sealH5 = taxService.sealH5(taxSignId);
            return H5UrlDto.builder()
                    .url(sealH5)
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

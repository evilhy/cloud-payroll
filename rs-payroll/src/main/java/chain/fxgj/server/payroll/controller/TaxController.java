package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.server.payroll.dto.tax.*;
import chain.fxgj.server.payroll.service.EmployeeWechatService;
import chain.fxgj.server.payroll.service.TaxService;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.fxgj.server.payroll.util.ImageBase64Utils;
import chain.fxgj.server.payroll.util.ImgPicUtils;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.EmployeeTaxSignFeignService;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.fxgj.constant.DictEnums.AttestStatusEnum;
import chain.utils.fxgj.constant.DictEnums.CertTypeEnum;
import chain.utils.fxgj.constant.DictEnums.DelStatusEnum;
import chain.utils.fxgj.constant.DictEnums.IsStatusEnum;
import core.dto.ErrorConstant;
import core.dto.request.employeeTaxSign.EmployeeTaxSignQueryReq;
import core.dto.request.employeeTaxSign.EmployeeTaxSignSaveReq;
import core.dto.response.employeeTaxSign.EmployeeTaxSignDTO;
import core.dto.wechat.EmployeeWechatDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Mono<SigningDetailsReq> signingDetails(@RequestHeader(value = "encry-salt", required = false) String salt,
                                                  @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String jsessionId = userPrincipal.getSessionId();
        String entId = userPrincipal.getEntId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /tax/signingDetails 签约详情查询 userPrincipal：{}", JacksonUtil.objectToJson(userPrincipal));

            //查询登陆信息
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId);
            SigningDetailsReq signingDetail = SigningDetailsReq.builder()
//                    .idCardNegative()
//                    .idCardFront()
//                    .taxSignId()
                    .idType("1")
                    .idTypeVal("身份证")
                    .userName(EncrytorUtils.encryptField(employeeWechatDTO.getName(), salt, passwd))
                    .phone(EncrytorUtils.encryptField(employeeWechatDTO.getPhone(), salt, passwd))
                    .idNumber(EncrytorUtils.encryptField(employeeWechatDTO.getIdNumber(), salt, passwd))
                    .signStatus(IsStatusEnum.NO.getCode())
                    .signStatusVal(IsStatusEnum.NO.getDesc())
                    .attestStatus(AttestStatusEnum.NOT.getCode())
                    .attestStatusVal(AttestStatusEnum.NOT.getDesc())
                    .passwd(passwd)
                    .salt(salt)
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
                signingDetail.setTaxSignId(employeeTaxSignDTO.getId());
                if (AttestStatusEnum.FAIL != employeeTaxSignDTO.getAttestStatus()) {
                    //正面照
                    if (StringUtils.isNotBlank(employeeTaxSignDTO.getIdCardFront())) {
                        String filePath = employeeTaxSignDTO.getIdCardFront();
                        //图片压缩
                        if (new File(filePath).length() > 1024 * 160) {
                            ImgPicUtils.compression(filePath, filePath);
                        }

                        //身份证照片
                        String base64 = ImageBase64Utils.imageToBase64(filePath);

                        signingDetail.setIdCardFront("data:image/jpg;base64," + base64);
                    }
                    //反面照
                    if (StringUtils.isNotBlank(employeeTaxSignDTO.getIdCardNegative())) {
                        String filePath = employeeTaxSignDTO.getIdCardNegative();
                        //图片压缩
                        if (new File(filePath).length() > 1024 * 160) {
                            ImgPicUtils.compression(filePath, filePath);
                        }

                        //身份证照片
                        String base64 = ImageBase64Utils.imageToBase64(filePath);

                        signingDetail.setIdCardNegative("data:image/jpg;base64," + base64);
                    }
                }
                signingDetail.setSignStatus(null == employeeTaxSignDTO.getSignStatus() ? IsStatusEnum.NO.getCode() : employeeTaxSignDTO.getSignStatus().getCode());
                signingDetail.setSignStatusVal(null == employeeTaxSignDTO.getSignStatus() ? IsStatusEnum.NO.getDesc() : employeeTaxSignDTO.getSignStatus().getDesc());
                signingDetail.setAttestStatus(null == employeeTaxSignDTO.getAttestStatus() ? AttestStatusEnum.NOT.getCode() : employeeTaxSignDTO.getAttestStatus().getCode());
                signingDetail.setAttestStatusVal(null == employeeTaxSignDTO.getAttestStatus() ? AttestStatusEnum.NOT.getDesc() : employeeTaxSignDTO.getAttestStatus().getDesc());

                //是否签约
                //验证身份信息成功，进入签约
                WalletH5Req walletH5Req = WalletH5Req.builder()
                        .fwOrg(employeeTaxSignDTO.getEntName())
                        .idCardNo(employeeTaxSignDTO.getIdNumber())
                        .idType("SFZ")
                        .phoneNo(employeeTaxSignDTO.getPhone())
                        .transUserId(employeeTaxSignDTO.getId())
                        .userName(employeeTaxSignDTO.getUserName())
//                    .ygOrg()
                        .build();
                try {
                    if (IsStatusEnum.YES != employeeTaxSignDTO.getSignStatus()) {
                        WalletH5Res walletH5Res = taxService.walletH5(walletH5Req);
                        if (null != walletH5Res && walletH5Res.getIsSeal()) {
                            //已签约
                            EmployeeTaxSignSaveReq signSaveReq = EmployeeTaxSignSaveReq.builder()
                                    .id(employeeTaxSignDTO.getId())
                                    .signStatus(IsStatusEnum.YES)
                                    .signDateTime(LocalDateTime.now())
                                    .build();
                            employeeTaxSignFeignService.save(signSaveReq);
                            signingDetail.setSignStatus(IsStatusEnum.YES.getCode());
                            signingDetail.setSignStatusVal(IsStatusEnum.YES.getDesc());
                        }
                    }
                } catch (Exception e) {
                    log.info("=====> 验证是否签约成功失败，walletH5Req：{}",JacksonUtil.objectToJson(walletH5Req));
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
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @TrackLog
    public Mono<UploadDto> upload(@NotNull @RequestPart("file") FilePart uploadfile) throws BusiVerifyException {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /tax/upload    身份证上传 ");

            Path tempFile = null;
            String filePath = null;
            try {
                String url = payrollProperties.getSignUploadPath() + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + File.separator;
                File urlfile = new File(url);
                urlfile.mkdirs();

                Path path = Paths.get(url);
                tempFile = Files.createTempFile(path, "IDCARD-", uploadfile.filename());
                filePath = tempFile.toFile().getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AsynchronousFileChannel channel = null;
            try {
                channel = AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DataBufferUtils.write(uploadfile.content(), channel, 0).doOnComplete(() -> {
                System.out.println("finish");
            }).subscribe();

            //图片压缩
            if (new File(filePath).length() > 1024 * 160) {
                ImgPicUtils.compression(filePath, filePath);
            }

            //身份证照片
            String base64 = ImageBase64Utils.imageToBase64(filePath);

            return UploadDto.builder()
                    .filepath(filePath)
                    .imgBase("data:image/jpg;base64," + base64)
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
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /tax/signing 确认签约 req：{}", JacksonUtil.objectToJson(req));

            //查询签约信息
            EmployeeTaxSignDTO employeeTaxSignDTO = employeeTaxSignFeignService.findById(req.getTaxSignId());
            if (null == employeeTaxSignDTO) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("请先认证再签约"));
            }
            if (IsStatusEnum.YES == employeeTaxSignDTO.getSignStatus()) {
                log.info("=====> 用户在当前企业已进行签约 employeeWechatDTO:{}, req:{}", JacksonUtil.objectToJson(employeeTaxSignDTO), JacksonUtil.objectToJson(req));
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户在当前企业已进行签约"));
            }

            //验证身份信息成功，进入签约
            WalletH5Req walletH5Req = WalletH5Req.builder()
                    .fwOrg(employeeTaxSignDTO.getEntName())
                    .idCardNo(employeeTaxSignDTO.getIdNumber())
                    .idType("SFZ")
                    .phoneNo(employeeTaxSignDTO.getPhone())
                    .transUserId(employeeTaxSignDTO.getId())
                    .userName(employeeTaxSignDTO.getUserName())
//                    .ygOrg()
                    .build();
            WalletH5Res walletH5Res = taxService.walletH5(walletH5Req);

            if (null == walletH5Res) {
                log.info("=====> 签约异常 walletH5Req:{}", JacksonUtil.objectToJson(walletH5Req));
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("签约异常"));
            }

            if (walletH5Res.getIsSeal()) {
                //已签约
                EmployeeTaxSignSaveReq signSaveReq = EmployeeTaxSignSaveReq.builder()
                        .id(employeeTaxSignDTO.getId())
                        .signStatus(IsStatusEnum.YES)
                        .signDateTime(LocalDateTime.now())
                        .build();
                employeeTaxSignFeignService.save(signSaveReq);
            }
            //未签约
            return H5UrlDto.builder()
                    .url(walletH5Res.getUrl())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 身份认证
     *
     * @return
     */
    @PostMapping("/attest")
    @TrackLog
    public Mono<Void> attest(@RequestBody SigningDetailsReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String jsessionId = userPrincipal.getSessionId();
        String entId = userPrincipal.getEntId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            log.info("=====> /tax/attest 身份认证 userPrincipal：{}，req：{}", JacksonUtil.objectToJson(userPrincipal), JacksonUtil.objectToJson(req));
            Optional.ofNullable(req.getIdCardFront()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("身份证正面照不能为空")));
            Optional.ofNullable(req.getIdCardNegative()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("身份证反面照不能为空")));

            //查询登陆信息
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId);

            //查询签约信息
            EmployeeTaxSignQueryReq signQueryReq = EmployeeTaxSignQueryReq.builder()
                    .entId(entId)
                    .idNumber(employeeWechatDTO.getIdNumber())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxSignDTO> list = employeeTaxSignFeignService.list(signQueryReq);

            String transUserId = null;
            if (null != list && list.size() > 0) {
                EmployeeTaxSignDTO employeeTaxSignDTO = list.get(0);
                if (AttestStatusEnum.SUCCESS == employeeTaxSignDTO.getAttestStatus() || AttestStatusEnum.ING == employeeTaxSignDTO.getAttestStatus()) {
                    log.info("=====> 用户在当前企业已进行认证 employeeWechatDTO:{}, signQueryReq:{}", JacksonUtil.objectToJson(employeeTaxSignDTO), JacksonUtil.objectToJson(signQueryReq));
                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户在当前企业已进行认证"));
                }
                transUserId = employeeTaxSignDTO.getId();
            }

            //保存
            EmployeeTaxSignSaveReq signSaveReq = EmployeeTaxSignSaveReq.builder()
                    .id(transUserId)
                    .certTypeEnum(CertTypeEnum.PERSONAL_1)
                    .delStatusEnum(DelStatusEnum.normal)
                    .entId(entId)
                    .entName(userPrincipal.getEntName())
                    .idNumber(req.getIdNumber())
                    .phone(req.getPhone())
                    .signStatus(IsStatusEnum.NO)
                    .attestStatus(AttestStatusEnum.ING)
                    .userName(req.getUserName())
                    .attestFailMsg("")
                    .idCardFront(req.getIdCardFront())
                    .idCardNegative(req.getIdCardNegative())
                    .build();
            EmployeeTaxSignDTO taxSignDTO = employeeTaxSignFeignService.save(signSaveReq);
            transUserId = taxSignDTO.getId();

            //身份证照片
            String idCardFront = ImageBase64Utils.imageToBase64(req.getIdCardFront());
            String idCardNegative = ImageBase64Utils.imageToBase64(req.getIdCardNegative());

            //验证身份信息
            SealUserReq userReq = SealUserReq.builder()
                    .fwOrg(userPrincipal.getEntName())
                    .idCardNo(req.getIdNumber())
                    .idType("SFZ")
                    .phoneNo(req.getPhone())
                    .userName(req.getUserName())
                    .transUserId(transUserId)
                    .idCardImg1("data:image/jpg;base64," + idCardFront)
                    .idCardImg2("data:image/jpg;base64," + idCardNegative)
//                    .ygOrg()
                    .build();
            SealUserRes userResult = taxService.user(userReq);
            if (null != userResult && "fail".equals(userResult.getRntCode())) {
                //验证失败或网络异常
                log.info("=====> 身份信息验证过程发生异常 userReq:{}", JacksonUtil.objectToJson(userReq));
                signSaveReq = EmployeeTaxSignSaveReq.builder()
                        .id(taxSignDTO.getId())
                        .attestStatus(AttestStatusEnum.FAIL)
                        .attestFailMsg(userResult.getRntMsg())
                        .build();
                employeeTaxSignFeignService.save(signSaveReq);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format(userResult.getRntMsg()));
            }

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * 认证结果推送
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
            log.info("=====> /tax/signResultPush 认证结果推送 req：{}", JacksonUtil.objectToJson(req));

            Optional.ofNullable(req).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("请求参数不能为空")));
            Optional.ofNullable(req.getIsAuth()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("签约状态不能为空")));
            Optional.ofNullable(req.getTransUserId()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("交易用户id不能为空")));

            //查询签约信息
            EmployeeTaxSignDTO employeeTaxSignDTO = employeeTaxSignFeignService.findById(req.getTransUserId());
            if (null == employeeTaxSignDTO) {
//                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未查询到签约信息"));
                log.info("=====> 更新结果失败 ");
                log.info("=====> 未查询到签约信息，req:{}", JacksonUtil.objectToJson(req));
                return SealUserRes.builder()
                        .rntMsg("失败")
                        .rntCode("fail")
                        .build();
            }
            try {
                //更新认证结果
                EmployeeTaxSignSaveReq saveReq = null;
                if (req.getIsAuth()) {
                    saveReq = EmployeeTaxSignSaveReq.builder()
                            .id(employeeTaxSignDTO.getId())
                            .attestStatus(AttestStatusEnum.SUCCESS)
                            .signDateTime(LocalDateTime.now())
                            .build();
                } else {
                    saveReq = EmployeeTaxSignSaveReq.builder()
                            .id(employeeTaxSignDTO.getId())
                            .attestStatus(AttestStatusEnum.FAIL)
                            .build();
                }
                employeeTaxSignFeignService.save(saveReq);

            } catch (Exception e) {
                log.info("=====> 更新认证结果失败 req：{}", JacksonUtil.objectToJson(req));
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
            log.info("=====> /tax/signRecord    签约记录查看   taxSignId：{}", taxSignId);

            String sealH5 = taxService.sealH5(taxSignId);
            return H5UrlDto.builder()
                    .url(sealH5)
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

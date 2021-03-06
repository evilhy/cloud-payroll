package chain.fxgj.server.payroll.controller;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.feign.client.EnterpriseFeignService;
import chain.fxgj.server.payroll.dto.tax.*;
import chain.fxgj.server.payroll.service.EmployeeWechatService;
import chain.fxgj.server.payroll.service.TaxService;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.fxgj.server.payroll.util.ImageBase64Utils;
import chain.fxgj.server.payroll.util.ImgPicUtils;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.*;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.fxgj.constant.DictEnums.AttestStatusEnum;
import chain.utils.fxgj.constant.DictEnums.CertTypeEnum;
import chain.utils.fxgj.constant.DictEnums.DelStatusEnum;
import chain.utils.fxgj.constant.DictEnums.IsStatusEnum;
import chain.wage.manager.core.dto.response.enterprise.EntErpriseInfoDTO;
import core.dto.ErrorConstant;
import core.dto.request.employeeTaxAttest.CodingDTO;
import core.dto.request.employeeTaxAttest.EmployeeTaxAttestQueryReq;
import core.dto.request.employeeTaxAttest.EmployeeTaxAttestSaveReq;
import core.dto.request.employeeTaxSigning.EmployeeTaxSigningQueryReq;
import core.dto.request.employeeTaxSigning.EmployeeTaxSigningSaveReq;
import core.dto.response.employeeTaxAttest.EmployeeTaxAttestDTO;
import core.dto.response.employeeTaxSigning.EmployeeTaxSigningDTO;
import core.dto.response.entAttach.EnterpriseAttachRes;
import core.dto.response.group.GroupDTO;
import core.dto.response.groupAttach.GroupAttachInfoDTO;
import core.dto.response.wageWithdraw.WageWithdrawDTO;
import core.dto.response.withdrawalLedger.WithdrawalLedgerDTO;
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
import java.time.ZoneId;
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
    WithdrawalLedgerInfoServiceFeign withdrawalLedgerInfoServiceFeign;
    @Autowired
    EnterpriseAttachFeignService enterpriseAttachFeignService;
    @Autowired
    GroupAttachInfoServiceFeign groupAttachInfoServiceFeign;
    @Autowired
    EnterpriseFeignService enterpriseFeignService;
    @Autowired
    EmployeeTaxAttestFeignService employeeTaxAttestFeignService;
    @Autowired
    EmployeeTaxSigningFeignService employeeTaxSigningFeignService;
    @Autowired
    WageWithdrawFeignService wageWithdrawFeignService;
    @Autowired
    GroupFeignController groupFeignController;

    @Autowired
    PayrollProperties payrollProperties;

    /**
     * ????????????
     *
     * @return
     */
    @GetMapping("/signingDetails")
    @TrackLog
    public Mono<SigningDetailsReq> signingDetails(@RequestParam("withdrawalLedgerId") String withdrawalLedgerId,
                                                  @RequestHeader(value = "encry-salt", required = false) String salt,
                                                  @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String jsessionId = userPrincipal.getSessionId();
        String entId = userPrincipal.getEntId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /tax/signingDetails ?????????????????? userPrincipal???{}", JacksonUtil.objectToJson(userPrincipal));

            //??????????????????
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId, userPrincipal.getName());

            SigningDetailsReq signingDetail = null;

            //??????????????????
            WithdrawalLedgerDTO withdrawalLedgerDTO = withdrawalLedgerInfoServiceFeign.findById(withdrawalLedgerId);
            if (null == withdrawalLedgerDTO) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????"));
            }
            String groupId = withdrawalLedgerDTO.getGroupId();
            //??????????????????????????????????????????
            GroupAttachInfoDTO attach = groupAttachInfoServiceFeign.findGroupAttachById(groupId);
            IsStatusEnum isStatusEnum = IsStatusEnum.NO;
            if (null != attach) {
                isStatusEnum = null == attach.getIsSign() ? IsStatusEnum.NO : attach.getIsSign();
            }

            //????????????????????????????????????
            if (IsStatusEnum.NO == isStatusEnum) {
                signingDetail = SigningDetailsReq.builder()
                        .isSign(isStatusEnum.getCode())
                        .isSignVal(isStatusEnum.getDesc())
                        .build();
                return signingDetail;
            }

            //?????????????????????
            signingDetail = SigningDetailsReq.builder()
//                    .idCardNegative()
//                    .idCardFront()
//                    .taxSignId()
                    .isSign(isStatusEnum.getCode())
                    .isSignVal(isStatusEnum.getDesc())
                    .idType("1")
                    .idTypeVal("?????????")
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

            //??????????????????
            EmployeeTaxAttestQueryReq attestQueryReq = EmployeeTaxAttestQueryReq.builder()
                    .idNumber(employeeWechatDTO.getIdNumber())
                    .userName(employeeWechatDTO.getName())
                    .phone(employeeWechatDTO.getPhone())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxAttestDTO> attestDTOList = employeeTaxAttestFeignService.list(attestQueryReq);
            if (null != attestDTOList && attestDTOList.size() > 0) {
                EmployeeTaxAttestDTO employeeTaxAttestDTO = attestDTOList.get(0);
                signingDetail.setProvinceCode(null == employeeTaxAttestDTO.getProvince() ? null : employeeTaxAttestDTO.getProvince().getCode());
                signingDetail.setProvinceName(null == employeeTaxAttestDTO.getProvince() ? null : employeeTaxAttestDTO.getProvince().getValue());
                signingDetail.setCityCode(null == employeeTaxAttestDTO.getCity() ? null : employeeTaxAttestDTO.getCity().getCode());
                signingDetail.setCityName(null == employeeTaxAttestDTO.getCity() ? null : employeeTaxAttestDTO.getCity().getValue());
                signingDetail.setAreaCode(null == employeeTaxAttestDTO.getArea() ? null : employeeTaxAttestDTO.getArea().getCode());
                signingDetail.setAreaName(null == employeeTaxAttestDTO.getArea() ? null : employeeTaxAttestDTO.getArea().getValue());
                signingDetail.setStreetCode(null == employeeTaxAttestDTO.getStreet() ? null : employeeTaxAttestDTO.getStreet().getCode());
                signingDetail.setStreetName(null == employeeTaxAttestDTO.getStreet() ? null : employeeTaxAttestDTO.getStreet().getValue());
                signingDetail.setAddress(employeeTaxAttestDTO.getAddress());
                signingDetail.setAttestStatus(null == employeeTaxAttestDTO.getAttestStatus() ? AttestStatusEnum.NOT.getCode() : employeeTaxAttestDTO.getAttestStatus().getCode());
                signingDetail.setAttestStatusVal(null == employeeTaxAttestDTO.getAttestStatus() ? AttestStatusEnum.NOT.getDesc() : employeeTaxAttestDTO.getAttestStatus().getDesc());
                signingDetail.setAttestFailMsg(null != employeeTaxAttestDTO.getAttestStatus() && AttestStatusEnum.FAIL == employeeTaxAttestDTO.getAttestStatus() ? employeeTaxAttestDTO.getAttestFailMsg() : null);

                //?????????
                if (StringUtils.isNotBlank(employeeTaxAttestDTO.getIdCardFront())) {
                    String filePath = employeeTaxAttestDTO.getIdCardFront();
                    //????????????
                    if (new File(filePath).length() > 1024 * 160) {
                        ImgPicUtils.compression(filePath, filePath);
                    }

                    //???????????????
                    String base64 = ImageBase64Utils.imageToBase64(filePath);
                    signingDetail.setIdCardFront("data:image/jpg;base64," + base64);
                }
                //?????????
                if (StringUtils.isNotBlank(employeeTaxAttestDTO.getIdCardNegative())) {
                    String filePath = employeeTaxAttestDTO.getIdCardNegative();
                    //????????????
                    if (new File(filePath).length() > 1024 * 160) {
                        ImgPicUtils.compression(filePath, filePath);
                    }

                    //???????????????
                    String base64 = ImageBase64Utils.imageToBase64(filePath);
                    signingDetail.setIdCardNegative("data:image/jpg;base64," + base64);
                }

                //????????????????????????ID
                WageWithdrawDTO withdrawDTO = wageWithdrawFeignService.findById(withdrawalLedgerDTO.getWageSheetId());
                if (null == withdrawDTO) {
                    log.info("=====> ???????????????????????? wageSheetId:{}", withdrawalLedgerDTO.getWageSheetId());
                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????"));
                }

                //??????????????????
                EmployeeTaxSigningQueryReq signingQueryReq = EmployeeTaxSigningQueryReq.builder()
                        .entId(withdrawalLedgerDTO.getEntId())
                        .groupId(withdrawalLedgerDTO.getGroupId())
                        .templateId(withdrawDTO.getTemplateId())
                        .empTaxAttestId(employeeTaxAttestDTO.getId())
                        .build();
                List<EmployeeTaxSigningDTO> signingDTOS = employeeTaxSigningFeignService.list(signingQueryReq);
                if (null != signingDTOS && signingDTOS.size() > 0) {
                    EmployeeTaxSigningDTO employeeTaxSigningDTO = signingDTOS.get(0);
                    signingDetail.setSignStatus(null == employeeTaxSigningDTO.getSignStatus() ? IsStatusEnum.NO.getCode() : employeeTaxSigningDTO.getSignStatus().getCode());
                    signingDetail.setSignStatusVal(null == employeeTaxSigningDTO.getSignStatus() ? IsStatusEnum.NO.getDesc() : employeeTaxSigningDTO.getSignStatus().getDesc());
                    signingDetail.setTaxSignId(employeeTaxSigningDTO.getId());

                    //???????????????????????????????????????
                    chain.cloud.tax.dto.fxgj.WalletH5Req walletH5Req = chain.cloud.tax.dto.fxgj.WalletH5Req.builder()
//                                .fwOrg(employeeTaxSigningDTO.getEntName())
                            .fwOrgId(employeeTaxSigningDTO.getEntNum())
//                                .ygOrg(employeeTaxSigningDTO.getGroupName())
                            .ygOrgId(employeeTaxSigningDTO.getGroupNum())
                            .templateId(employeeTaxSigningDTO.getTemplateId())
                            .idType("SFZ")
                            .idCardNo(employeeTaxAttestDTO.getIdNumber())
                            .phone(employeeTaxAttestDTO.getPhone())
                            .transUserId(employeeTaxAttestDTO.getId())
                            .userName(employeeTaxAttestDTO.getUserName())
                            .build();
                    try {
                        if (IsStatusEnum.YES != employeeTaxSigningDTO.getSignStatus()) {

                            chain.cloud.tax.dto.fxgj.WalletH5Res walletH5Res = taxService.walletH5(walletH5Req);
                            if (null != walletH5Res && walletH5Res.getIsSeal()) {
                                //?????????
                                EmployeeTaxSigningSaveReq signSaveReq = EmployeeTaxSigningSaveReq.builder()
                                        .id(employeeTaxSigningDTO.getId())
                                        .signStatus(IsStatusEnum.YES)
                                        .signDateTime(LocalDateTime.now())
                                        .build();
                                employeeTaxSigningFeignService.save(signSaveReq);
                                signingDetail.setSignStatus(IsStatusEnum.YES.getCode());
                                signingDetail.setSignStatusVal(IsStatusEnum.YES.getDesc());
                            }
                        }
                    } catch (Exception e) {
                        log.info("=====> ?????????????????????????????????walletH5Req???{}", JacksonUtil.objectToJson(walletH5Req));
                    }

                }
            }
            return signingDetail;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ???????????????
     *
     * @param uploadfile ?????????
     * @return
     * @throws BusiVerifyException
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @TrackLog
    public Mono<UploadDto> upload(@NotNull @RequestPart("file") FilePart uploadfile) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /tax/upload    ??????????????? ");

            Path tempFile = null;
            String filePath = null;
            try {
                String url = payrollProperties.getSignUploadPath() + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + File.separator;
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

            //????????????
            File file = new File(filePath);
            log.info("=====> ????????????????????????{}", file.length() / 1024);
//            if (file.length() > 1024 * 90) {
            //????????????????????????
            String path = file.getPath();
            String name = file.getName();
            String replace = path.replace(name, "min/");
            File file1 = new File(replace);
            if (!file1.exists()) {
                file1.mkdirs();
            }
            String compressionPath = replace + name;
            ImgPicUtils.compression(filePath, compressionPath);
//            }
            log.info("=====> ??????????????????????????????{}", new File(compressionPath).length() / 1024);

            //???????????????
            String base64 = ImageBase64Utils.imageToBase64(compressionPath);
            if (StringUtils.isBlank(base64)) {
                //?????????????????????????????????
                base64 = ImageBase64Utils.imageToBase64(compressionPath);
                if (StringUtils.isBlank(base64)) {
                    log.info("=====> ???????????????????????????????????????compressionPath???{}", compressionPath);
                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
                }
            }
            String imgBase = "data:image/jpg;base64," + base64;
            log.info("?????????base64:{}" + imgBase);

            return UploadDto.builder()
                    .filepath(compressionPath)
                    .imgBase(imgBase)
                    .build();
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * ????????????
     *
     * @return
     */
    @PostMapping("/signing")
    @TrackLog
    public Mono<H5UrlDto> signing(@RequestBody SigningDetailsReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String jsessionId = userPrincipal.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /tax/signing ???????????? req???{}", JacksonUtil.objectToJson(req));
            //??????????????????
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId, userPrincipal.getName());

            //??????????????????
            EmployeeTaxAttestQueryReq attestQueryReq = EmployeeTaxAttestQueryReq.builder()
                    .idNumber(userPrincipal.getIdNumber())
                    .userName(employeeWechatDTO.getName())
                    .phone(employeeWechatDTO.getPhone())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxAttestDTO> attestDTOList = employeeTaxAttestFeignService.list(attestQueryReq);
            if (null == attestDTOList || attestDTOList.size() <= 0) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????"));
            }
            EmployeeTaxAttestDTO employeeTaxAttestDTO = attestDTOList.get(0);

            //??????????????????
            String groupId = null;
            String entId = null;
            String templateId = null;
            String id = req.getTaxSignId();
            if (StringUtils.isNotBlank(req.getTaxSignId())) {
                EmployeeTaxSigningDTO employeeTaxSignDTO = employeeTaxSigningFeignService.findById(req.getTaxSignId());
                if (IsStatusEnum.YES == employeeTaxSignDTO.getSignStatus()) {
                    log.info("=====> ???????????????????????????????????? employeeWechatDTO:{}, req:{}", JacksonUtil.objectToJson(employeeTaxSignDTO), JacksonUtil.objectToJson(req));
                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
                }
                groupId = employeeTaxSignDTO.getGroupId();
                entId = employeeTaxSignDTO.getEntId();
                templateId = employeeTaxSignDTO.getTemplateId();
                id = employeeTaxSignDTO.getId();
            }

            if (StringUtils.isNotBlank(req.getWithdrawalLedgerId())) {
                //??????????????????
                WithdrawalLedgerDTO withdrawalLedgerDTO = withdrawalLedgerInfoServiceFeign.findById(req.getWithdrawalLedgerId());
                if (null == withdrawalLedgerDTO) {
                    log.info("=====> ????????????????????? withdrawalLedgerId:{}", req.getWithdrawalLedgerId());
                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????"));
                }

                //????????????????????????ID
                WageWithdrawDTO withdrawDTO = wageWithdrawFeignService.findById(withdrawalLedgerDTO.getWageSheetId());
                if (null == withdrawDTO) {
                    log.info("=====> ???????????????????????? wageSheetId:{}", withdrawalLedgerDTO.getWageSheetId());
                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????"));
                }

                groupId = withdrawDTO.getGroupId();
                entId = withdrawDTO.getEntId();
                templateId = withdrawDTO.getTemplateId();
            }

            GroupAttachInfoDTO groupAttachInfoDTO = groupAttachInfoServiceFeign.findGroupAttachById(groupId);
            if (null == groupAttachInfoDTO) {
                log.info("=====> ???????????????????????????????????? groupId:{}", groupId);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
            }

            GroupDTO groupDTO = groupFeignController.findById(groupId);
            if (null == groupDTO) {
                log.info("=====> ?????????????????????????????? groupId:{}", groupId);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("??????????????????????????????"));
            }

            EntErpriseInfoDTO entErpriseInfoDTO = enterpriseFeignService.findById(entId);
            if (null == entErpriseInfoDTO) {
                log.info("=====> ???????????????????????? entId:{}", entId);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????"));
            }

            EnterpriseAttachRes enterpriseAttachRes = enterpriseAttachFeignService.attachInfo(entId);
            if (null == enterpriseAttachRes) {
                log.info("=====> ?????????????????????????????? entId:{}", entId);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("??????????????????????????????"));
            }

            //?????????????????????
            EmployeeTaxSigningQueryReq signingQueryReq = EmployeeTaxSigningQueryReq.builder()
                    .id(id)
                    .entId(entErpriseInfoDTO.getId())
                    .groupId(groupDTO.getGroupId())
                    .templateId(templateId)
                    .empTaxAttestId(employeeTaxAttestDTO.getId())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxSigningDTO> signingDTOS = employeeTaxSigningFeignService.list(signingQueryReq);
            if (null != signingDTOS && signingDTOS.size() > 0) {
                EmployeeTaxSigningDTO employeeTaxSigningDTO = signingDTOS.get(0);
                if (IsStatusEnum.YES == employeeTaxSigningDTO.getSignStatus()) {
                    log.info("=====> ???????????????????????????????????? employeeWechatDTO:{}, req:{}", JacksonUtil.objectToJson(employeeTaxSigningDTO), JacksonUtil.objectToJson(req));
                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
                }
                id = employeeTaxSigningDTO.getId();
            }

            //??????????????????
            EmployeeTaxSigningSaveReq signSaveReq = EmployeeTaxSigningSaveReq.builder()
                    .id(id)
                    .delStatusEnum(DelStatusEnum.normal)
                    .empTaxAttestId(employeeTaxAttestDTO.getId())
                    .entId(entErpriseInfoDTO.getId())
                    .entName(entErpriseInfoDTO.getEntName())
                    .entNum(enterpriseAttachRes.getEntNum())
                    .groupId(groupDTO.getGroupId())
                    .groupName(groupDTO.getGroupName())
                    .groupNum(groupAttachInfoDTO.getGroupNum())
//                    .signDateTime()
                    .signStatus(IsStatusEnum.NO)
                    .templateId(templateId)
                    .build();
            EmployeeTaxSigningDTO signingDTO = employeeTaxSigningFeignService.save(signSaveReq);

            //???????????????????????????????????????
            chain.cloud.tax.dto.fxgj.WalletH5Req walletH5Req = chain.cloud.tax.dto.fxgj.WalletH5Req.builder()
//                    .fwOrg(entErpriseInfoDTO.getEntName())
                    .fwOrgId(enterpriseAttachRes.getEntNum())
//                    .ygOrg(groupDTO.getGroupName())
                    .ygOrgId(groupAttachInfoDTO.getGroupNum())
                    .templateId(templateId)
                    .idType("SFZ")
                    .idCardNo(employeeTaxAttestDTO.getIdNumber())
                    .phone(employeeTaxAttestDTO.getPhone())
                    .transUserId(employeeTaxAttestDTO.getId())
                    .userName(employeeTaxAttestDTO.getUserName())
                    .build();
            chain.cloud.tax.dto.fxgj.WalletH5Res walletH5Res = taxService.walletH5(walletH5Req);

            if (null == walletH5Res) {
                log.info("=====> ???????????? walletH5Req:{}", JacksonUtil.objectToJson(walletH5Req));
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????"));
            }

            if (walletH5Res.getIsSeal()) {
                //?????????
                EmployeeTaxSigningSaveReq signSaveReq1 = EmployeeTaxSigningSaveReq.builder()
                        .id(signingDTO.getId())
                        .signStatus(IsStatusEnum.YES)
                        .signDateTime(LocalDateTime.now())
                        .build();
                employeeTaxSigningFeignService.save(signSaveReq1);
            }
            //?????????
            return H5UrlDto.builder()
                    .url(walletH5Res.getUrl())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????
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

            log.info("=====> /tax/attest ???????????? userPrincipal???{}???req???{}", JacksonUtil.objectToJson(userPrincipal), JacksonUtil.objectToJson(req));
            Optional.ofNullable(req.getIdCardFront()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("??????????????????????????????")));
            Optional.ofNullable(req.getIdCardNegative()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("??????????????????????????????")));

            //??????????????????
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId, userPrincipal.getName());

            //??????????????????
            EmployeeTaxAttestQueryReq attestQueryReq = EmployeeTaxAttestQueryReq.builder()
                    .idNumber(userPrincipal.getIdNumber())
                    .userName(employeeWechatDTO.getName())
                    .phone(employeeWechatDTO.getPhone())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxAttestDTO> attestDTOList = employeeTaxAttestFeignService.list(attestQueryReq);

            String transUserId = null;
            if (null != attestDTOList && attestDTOList.size() > 0) {
                EmployeeTaxAttestDTO employeeTaxSignDTO = attestDTOList.get(0);
                if (AttestStatusEnum.SUCCESS == employeeTaxSignDTO.getAttestStatus() || AttestStatusEnum.ING == employeeTaxSignDTO.getAttestStatus()) {
                    log.info("=====> ???????????????????????????????????? employeeWechatDTO:{}, attestQueryReq:{}", JacksonUtil.objectToJson(employeeTaxSignDTO), JacksonUtil.objectToJson(attestQueryReq));
                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
                }
                transUserId = employeeTaxSignDTO.getId();
            }

            //??????
            EmployeeTaxAttestSaveReq attestSaveReq = EmployeeTaxAttestSaveReq.builder()
                    .id(transUserId)
                    .certTypeEnum(CertTypeEnum.PERSONAL_1)
                    .delStatusEnum(DelStatusEnum.normal)
                    .idNumber(req.getIdNumber())
                    .phone(req.getPhone())
                    .attestStatus(AttestStatusEnum.ING)
                    .userName(req.getUserName())
                    .attestFailMsg("")
                    .idCardFront(req.getIdCardFront())
                    .idCardNegative(req.getIdCardNegative())
                    .province(StringUtils.isNotBlank(req.getProvinceCode()) || StringUtils.isNotBlank(req.getProvinceName()) ? CodingDTO.builder().code(req.getProvinceCode()).value(req.getProvinceName()).build() : null)
                    .city(StringUtils.isNotBlank(req.getCityCode()) || StringUtils.isNotBlank(req.getCityName()) ? CodingDTO.builder().code(req.getProvinceCode()).value(req.getCityName()).build() : null)
                    .area(StringUtils.isNotBlank(req.getAreaCode()) || StringUtils.isNotBlank(req.getAreaName()) ? CodingDTO.builder().code(req.getProvinceCode()).value(req.getAreaName()).build() : null)
                    .street(StringUtils.isNotBlank(req.getStreetCode()) || StringUtils.isNotBlank(req.getStreetName()) ? CodingDTO.builder().code(req.getProvinceCode()).value(req.getStreetName()).build() : null)
                    .address(req.getAddress())
                    .build();
            EmployeeTaxAttestDTO taxSignDTO = employeeTaxAttestFeignService.save(attestSaveReq);
            transUserId = taxSignDTO.getId();

            //???????????????
            String idCardFront = ImageBase64Utils.imageToBase64(req.getIdCardFront());
            String idCardNegative = ImageBase64Utils.imageToBase64(req.getIdCardNegative());

            StringBuilder sb = new StringBuilder();
            StringBuilder append = sb.append(req.getProvinceName())
                    .append(req.getCityName())
                    .append(req.getAreaName())
                    .append(req.getAddress());

            //??????????????????
            chain.cloud.tax.dto.fxgj.SealUserReq userReq = chain.cloud.tax.dto.fxgj.SealUserReq.builder()
                    .idCardNo(req.getIdNumber())
                    .idType("SFZ")
                    .phone(req.getPhone())
                    .userName(req.getUserName())
                    .transUserId(transUserId)
                    .idCardImg1("data:image/jpg;base64," + idCardFront)
                    .idCardImg2("data:image/jpg;base64," + idCardNegative)
                    .address(append.toString())
                    .build();
            try {
                chain.cloud.tax.dto.fxgj.SealUserRes userResult = taxService.user(userReq);
                if (null != userResult && "fail".equals(userResult.getRntCode())) {
                    attestSaveReq = EmployeeTaxAttestSaveReq.builder()
                            .id(taxSignDTO.getId())
                            .attestStatus(AttestStatusEnum.FAIL)
                            .attestFailMsg(userResult.getRntMsg())
                            .build();
                    employeeTaxAttestFeignService.save(attestSaveReq);
                }
            } catch (Exception e) {
                //???????????????????????????
                log.info("=====> ???????????????????????????????????? userReq:{}", JacksonUtil.objectToJson(userReq));
                attestSaveReq = EmployeeTaxAttestSaveReq.builder()
                        .id(taxSignDTO.getId())
                        .attestStatus(AttestStatusEnum.FAIL)
                        .attestFailMsg("??????????????????????????????????????????")
                        .build();
                employeeTaxAttestFeignService.save(attestSaveReq);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????????????????"));
            }

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ??????????????????
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
            log.info("=====> /tax/signResultPush ?????????????????? req???{}", JacksonUtil.objectToJson(req));

            Optional.ofNullable(req).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????")));
            Optional.ofNullable(req.getIsAuth()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????")));
            Optional.ofNullable(req.getTransUserId()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????id????????????")));

            //??????????????????
            EmployeeTaxAttestDTO employeeTaxAttestDTO = employeeTaxAttestFeignService.findById(req.getTransUserId());
            if (null == employeeTaxAttestDTO) {
//                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????"));
                log.info("=====> ?????????????????? ");
                log.info("=====> ???????????????????????????req:{}", JacksonUtil.objectToJson(req));
                return SealUserRes.builder()
                        .rntMsg("??????")
                        .rntCode("fail")
                        .build();
            }
            try {
                //??????????????????
                EmployeeTaxAttestSaveReq saveReq = null;
                if (req.getIsAuth()) {
                    saveReq = EmployeeTaxAttestSaveReq.builder()
                            .id(employeeTaxAttestDTO.getId())
                            .attestStatus(AttestStatusEnum.SUCCESS)
                            .build();
                } else {
                    saveReq = EmployeeTaxAttestSaveReq.builder()
                            .id(employeeTaxAttestDTO.getId())
                            .attestStatus(AttestStatusEnum.FAIL)
                            .attestFailMsg(req.getMsg())
                            .build();
                }
                employeeTaxAttestFeignService.save(saveReq);

            } catch (Exception e) {
                log.info("=====> ???????????????????????? req???{}", JacksonUtil.objectToJson(req));
                return SealUserRes.builder()
                        .rntMsg("??????")
                        .rntCode("fail")
                        .build();
            }
            return SealUserRes.builder()
                    .rntMsg("??????")
                    .rntCode("success")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @GetMapping("/signRecord")
    @TrackLog
    public Mono<H5UrlDto> signRecord(@RequestParam(value = "taxSignId", required = false) String taxSignId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String jsessionId = userPrincipal.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
//            Optional.ofNullable(taxSignId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("??????ID??????????????????")));
            log.info("=====> /tax/signRecord    ??????????????????   taxSignId???{}", taxSignId);

            //??????????????????
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId, userPrincipal.getName());

            //??????????????????
            EmployeeTaxAttestQueryReq attestQueryReq = EmployeeTaxAttestQueryReq.builder()
                    .idNumber(employeeWechatDTO.getIdNumber())
                    .userName(employeeWechatDTO.getName())
                    .phone(employeeWechatDTO.getPhone())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxAttestDTO> attestDTOList = employeeTaxAttestFeignService.list(attestQueryReq);
            String empTaxAttestId = null;
            if (null != attestDTOList && attestDTOList.size() > 0) {
                empTaxAttestId = attestDTOList.get(0).getId();
            } else {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("??????????????????????????????????????????"));
            }

            //????????????
            EnterpriseAttachRes enterpriseAttachRes = enterpriseAttachFeignService.attachInfo(userPrincipal.getEntId());
            if (null == enterpriseAttachRes) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("???????????????????????????"));
            }

            chain.cloud.tax.dto.fxgj.SealH5Req sealH5Req = chain.cloud.tax.dto.fxgj.SealH5Req.builder()
                    .fwOrgId(enterpriseAttachRes.getEntNum())
                    .isUrl(true)
                    .transUserId(empTaxAttestId)
                    .build();
            chain.cloud.tax.dto.fxgj.SealH5Res h5Res = taxService.sealH5(sealH5Req);
            log.info("====> ????????????????????????  h5Res???{}", JacksonUtil.objectToJson(h5Res));
            if (null == h5Res) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????????????????"));
            }

            return H5UrlDto.builder()
                    .url(h5Res.getUrl())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????
     *
     * @param salt
     * @param passwd
     * @return
     */
    @GetMapping("/taxAttestDetail")
    @TrackLog
    public Mono<TaxAttestDetailRes> taxAttestDetail(@RequestHeader(value = "encry-salt", required = false) String salt,
                                                    @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String jsessionId = userPrincipal.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /tax/signingDetails ?????????????????? userPrincipal???{}", JacksonUtil.objectToJson(userPrincipal));

            //??????????????????
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId, userPrincipal.getName());

            TaxAttestDetailRes taxAttestDetailRes = TaxAttestDetailRes.builder()
                    .idType("1")
                    .idTypeVal("?????????")
                    .userName(EncrytorUtils.encryptField(employeeWechatDTO.getName(), salt, passwd))
                    .phone(EncrytorUtils.encryptField(employeeWechatDTO.getPhone(), salt, passwd))
                    .idNumber(EncrytorUtils.encryptField(employeeWechatDTO.getIdNumber(), salt, passwd))
                    .attestStatus(AttestStatusEnum.NOT.getCode())
                    .attestStatusVal(AttestStatusEnum.NOT.getDesc())
                    .passwd(passwd)
                    .salt(salt)
                    .build();

            //??????????????????
            EmployeeTaxAttestQueryReq attestQueryReq = EmployeeTaxAttestQueryReq.builder()
                    .idNumber(employeeWechatDTO.getIdNumber())
                    .userName(employeeWechatDTO.getName())
                    .phone(employeeWechatDTO.getPhone())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxAttestDTO> attestDTOList = employeeTaxAttestFeignService.list(attestQueryReq);
            if (null != attestDTOList && attestDTOList.size() > 0) {
                EmployeeTaxAttestDTO employeeTaxAttestDTO = attestDTOList.get(0);
                taxAttestDetailRes.setTaxAttestId(employeeTaxAttestDTO.getId());
                taxAttestDetailRes.setProvinceCode(null == employeeTaxAttestDTO.getProvince() ? null : employeeTaxAttestDTO.getProvince().getCode());
                taxAttestDetailRes.setProvinceName(null == employeeTaxAttestDTO.getProvince() ? null : employeeTaxAttestDTO.getProvince().getValue());
                taxAttestDetailRes.setCityCode(null == employeeTaxAttestDTO.getCity() ? null : employeeTaxAttestDTO.getCity().getCode());
                taxAttestDetailRes.setCityName(null == employeeTaxAttestDTO.getCity() ? null : employeeTaxAttestDTO.getCity().getValue());
                taxAttestDetailRes.setAreaCode(null == employeeTaxAttestDTO.getArea() ? null : employeeTaxAttestDTO.getArea().getCode());
                taxAttestDetailRes.setAreaName(null == employeeTaxAttestDTO.getArea() ? null : employeeTaxAttestDTO.getArea().getValue());
                taxAttestDetailRes.setStreetCode(null == employeeTaxAttestDTO.getStreet() ? null : employeeTaxAttestDTO.getStreet().getCode());
                taxAttestDetailRes.setStreetName(null == employeeTaxAttestDTO.getStreet() ? null : employeeTaxAttestDTO.getStreet().getValue());
                taxAttestDetailRes.setAddress(employeeTaxAttestDTO.getAddress());
                taxAttestDetailRes.setAttestStatus(null == employeeTaxAttestDTO.getAttestStatus() ? AttestStatusEnum.NOT.getCode() : employeeTaxAttestDTO.getAttestStatus().getCode());
                taxAttestDetailRes.setAttestStatusVal(null == employeeTaxAttestDTO.getAttestStatus() ? AttestStatusEnum.NOT.getDesc() : employeeTaxAttestDTO.getAttestStatus().getDesc());
                taxAttestDetailRes.setAttestFailMsg(null != employeeTaxAttestDTO.getAttestStatus() && AttestStatusEnum.FAIL == employeeTaxAttestDTO.getAttestStatus() ? employeeTaxAttestDTO.getAttestFailMsg() : null);

                //?????????
                if (StringUtils.isNotBlank(employeeTaxAttestDTO.getIdCardFront())) {
                    String filePath = employeeTaxAttestDTO.getIdCardFront();
                    //????????????
                    if (new File(filePath).length() > 1024 * 160) {
                        ImgPicUtils.compression(filePath, filePath);
                    }

                    //???????????????
                    String base64 = ImageBase64Utils.imageToBase64(filePath);
                    taxAttestDetailRes.setIdCardFront("data:image/jpg;base64," + base64);
                }
                //?????????
                if (StringUtils.isNotBlank(employeeTaxAttestDTO.getIdCardNegative())) {
                    String filePath = employeeTaxAttestDTO.getIdCardNegative();
                    //????????????
                    if (new File(filePath).length() > 1024 * 160) {
                        ImgPicUtils.compression(filePath, filePath);
                    }

                    //???????????????
                    String base64 = ImageBase64Utils.imageToBase64(filePath);
                    taxAttestDetailRes.setIdCardNegative("data:image/jpg;base64," + base64);
                }
            }
            return taxAttestDetailRes;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ???????????????
     *
     * @return
     */
    @GetMapping("/signingList")
    @TrackLog
    public Mono<List<SigningDetailRes>> signingList(@RequestParam(value = "templateId", required = false) String templateId,
                                                    @RequestHeader(value = "encry-salt", required = false) String salt,
                                                    @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String jsessionId = userPrincipal.getSessionId();
        String entId = userPrincipal.getEntId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /tax/signingDetails ?????????????????? userPrincipal???{}", JacksonUtil.objectToJson(userPrincipal));
            List<SigningDetailRes> list = new ArrayList<>();

            //??????????????????
            EmployeeWechatDTO employeeWechatDTO = employeeWechatService.findByJsessionId(jsessionId, userPrincipal.getName());

            //??????????????????
            EmployeeTaxAttestQueryReq attestQueryReq = EmployeeTaxAttestQueryReq.builder()
                    .idNumber(employeeWechatDTO.getIdNumber())
                    .userName(employeeWechatDTO.getName())
                    .phone(employeeWechatDTO.getPhone())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxAttestDTO> attestDTOList = employeeTaxAttestFeignService.list(attestQueryReq);
            if (null != attestDTOList && attestDTOList.size() > 0) {
                EmployeeTaxAttestDTO employeeTaxAttestDTO = attestDTOList.get(0);
                String empTaxAttestId = employeeTaxAttestDTO.getId();

                //??????????????????
                EmployeeTaxSigningQueryReq signingQueryReq = EmployeeTaxSigningQueryReq.builder()
                        .entId(entId)
                        .empTaxAttestId(empTaxAttestId)
                        .signStatus(IsStatusEnum.NO)
                        .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                        .build();
                if (StringUtils.isNotBlank(templateId)) {
                    signingQueryReq.setTemplateId(templateId);
                }
                List<EmployeeTaxSigningDTO> signingDTOS = employeeTaxSigningFeignService.list(signingQueryReq);
                if (null != signingDTOS && signingDTOS.size() > 0) {
                    for (EmployeeTaxSigningDTO employeeTaxSigningDTO : signingDTOS) {
                        SigningDetailRes signingDetail = SigningDetailRes.builder()
                                .empTaxAttestId(employeeTaxSigningDTO.getEmpTaxAttestId())
                                .entName(employeeTaxSigningDTO.getEntName())
                                .groupId(employeeTaxSigningDTO.getGroupId())
                                .entNum(employeeTaxSigningDTO.getEntNum())
                                .groupName(employeeTaxSigningDTO.getGroupName())
                                .entId(employeeTaxSigningDTO.getEntId())
                                .groupNum(employeeTaxSigningDTO.getGroupNum())
                                .taxSignId(employeeTaxSigningDTO.getId())
                                .signDateTime(null == employeeTaxSigningDTO.getSignDateTime() ? null : employeeTaxSigningDTO.getSignDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                                .signStatus(null == employeeTaxSigningDTO.getSignStatus() ? IsStatusEnum.NO.getCode() : employeeTaxSigningDTO.getSignStatus().getCode())
                                .signStatusVal(null == employeeTaxSigningDTO.getSignStatus() ? IsStatusEnum.NO.getDesc() : employeeTaxSigningDTO.getSignStatus().getDesc())
                                .templateId(employeeTaxSigningDTO.getTemplateId())
                                .templateName(employeeTaxSigningDTO.getTemplateName())
                                .templateNo(employeeTaxSigningDTO.getTemplateNo())
                                .build();

                        //???????????????????????????????????????
                        chain.cloud.tax.dto.fxgj.WalletH5Req walletH5Req = chain.cloud.tax.dto.fxgj.WalletH5Req.builder()
//                                .fwOrg(employeeTaxSigningDTO.getEntName())
                                .fwOrgId(employeeTaxSigningDTO.getEntNum())
//                                .ygOrg(employeeTaxSigningDTO.getGroupName())
                                .ygOrgId(employeeTaxSigningDTO.getGroupNum())
                                .templateId(employeeTaxSigningDTO.getTemplateId())
                                .idType("SFZ")
                                .idCardNo(employeeTaxAttestDTO.getIdNumber())
                                .phone(employeeTaxAttestDTO.getPhone())
                                .transUserId(employeeTaxAttestDTO.getId())
                                .userName(employeeTaxAttestDTO.getUserName())
                                .build();
                        try {
                            if (IsStatusEnum.YES != employeeTaxSigningDTO.getSignStatus()) {

                                chain.cloud.tax.dto.fxgj.WalletH5Res walletH5Res = taxService.walletH5(walletH5Req);
                                if (null != walletH5Res && walletH5Res.getIsSeal()) {
                                    //?????????
                                    EmployeeTaxSigningSaveReq signSaveReq = EmployeeTaxSigningSaveReq.builder()
                                            .id(employeeTaxSigningDTO.getId())
                                            .signStatus(IsStatusEnum.YES)
                                            .signDateTime(LocalDateTime.now())
                                            .build();
                                    employeeTaxSigningFeignService.save(signSaveReq);
                                    continue;
                                }
                            }
                        } catch (Exception e) {
                            log.info("=====> ?????????????????????????????????walletH5Req???{}", JacksonUtil.objectToJson(walletH5Req));
                        }
                        list.add(signingDetail);
                    }
                }
            }
            return list;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.exception.ServiceHandleException;
import chain.css.log.annotation.TrackLog;
import chain.feign.hxinside.ent.service.EmployeeInfoServiceFeign;
import chain.fxgj.ent.core.dto.request.EmployeeQueryRequest;
import chain.fxgj.ent.core.dto.request.employee.EmpInfoUpdReq;
import chain.fxgj.ent.core.dto.response.EmployeeInfoRes;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.base.HeaderDTO;
import chain.fxgj.server.payroll.dto.request.ReadWageDTO;
import chain.fxgj.server.payroll.dto.request.*;
import chain.fxgj.server.payroll.dto.request.ReqPhone;
import chain.fxgj.server.payroll.dto.response.Res100302;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.EmployeeEncrytorService;
import chain.fxgj.server.payroll.service.PaswordService;
import chain.fxgj.server.payroll.service.impl.CallInsideServiceImpl;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.fxgj.server.payroll.util.TransferUtil;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.ids.client.feign.UnAuthFeignClient;
import chain.ids.core.commons.dto.login.SmsDTO;
import chain.ids.core.commons.dto.sms.SmsCodeSendDTO;
import chain.payroll.client.feign.InsideFeignController;
import chain.utils.commons.JacksonUtil;
import chain.utils.fxgj.constant.DictEnums.*;
import core.dto.request.*;
import core.dto.response.index.EmpEntResDTO;
import core.dto.response.inside.SkinThemeInfoDto;
import core.dto.response.inside.SkinThemeInfoReq;
import core.dto.response.inside.WageRetReceiptDTO;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@Validated
@RequestMapping("/inside")
@Slf4j
@SuppressWarnings("unchecked")
public class InsideController {

    @Autowired
    CallInsideServiceImpl callInsideService;
    @Autowired
    private EmpWechatService empWechatService;
    @Autowired
    EmployeeInfoServiceFeign employeeInfoServiceFeign;
    @Autowired
    InsideFeignController insideFeignController;
    @Autowired
    PaswordService paswordService;
    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    UnAuthFeignClient unAuthFeignClient;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * ?????????????????????
     * busyType ??????
     * 0 ?????????????????????(?????????????????????)
     * 1 ???????????????????????????(???????????????????????????????????????jsessionId ????????????????????????)
     * 2 ???????????????????????????
     *
     * @return
     */
    @PostMapping("/sendCode")
    @TrackLog
    @PermitAll
    public Mono<Res100302> sendCode(@RequestBody SendCodeReqDTO sendCodeReqDTO, @RequestHeader(value = "X-Real-IP", required = false) String clientIp,
                                    @RequestHeader(value = "jsession-id", required = false) String jsessionId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        log.info("sendCode.sendCodeReqDTO:[{}]", JacksonUtil.objectToJson(sendCodeReqDTO));
        log.info("sendCode.X-Real-IP:[{}]", clientIp);
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String busiType = sendCodeReqDTO.getBusiType();
            String phone = "";
            if (StringUtils.equals("0", busiType)) {//0 ?????????????????????(?????????????????????)
                phone = sendCodeReqDTO.getPhone();
                if (StringUtils.isBlank(phone)) {
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????"));
                }
            } else if (StringUtils.equals("1", busiType)) {//1 ???????????????????????????(????????????????????????jsessionId ????????????????????????)
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                if (null == wechatInfoDetail) {
                    log.error("??????jsessionId:[{}]??????????????????", jsessionId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????"));
                }
                phone = wechatInfoDetail.getPhone();
                if (StringUtils.isBlank(phone)) {
                    log.error("????????????????????????sendCode.wechatInfoDetail:[{}]", JacksonUtil.objectToJson(wechatInfoDetail));
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!"));
                }
            } else if (StringUtils.equals("2", busiType)) {//2 ???????????????????????????
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                log.info("sedCode.wechatInfoDetail:[{}]", JacksonUtil.objectToJson(wechatInfoDetail));
                if (null == wechatInfoDetail) {
                    log.error("??????jsessionId:[{}]??????????????????", jsessionId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!!"));
                }
                String idNumber = wechatInfoDetail.getIdNumber();
                String entId = wechatInfoDetail.getEntId();
                String[] groupIds = wechatInfoDetail.getGroupIds();
                if (StringUtils.isBlank(idNumber) || StringUtils.isBlank(entId)) {
                    log.error("idNumber:[{}], entId:[{}]", idNumber, entId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!!!"));
                }
                List<String> groups = new ArrayList<>();
                String groupId = sendCodeReqDTO.getGroupId();
                if (StringUtils.isBlank(groupId)) {
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!"));
                }
//                ???????????????????????????????????? ???????????????groups???????????????
//                //????????????groupId???
//                boolean tab = true;
//                for (String group_id : groupIds) {
//                    if (StringUtils.equals(groupId, group_id)) {
//                        //?????????groups?????????????????????????????????groupId????????????false???????????????
//                        tab = false;
//                    }
//                }
//                if (tab) {
//                    log.error("?????????????????????????????????groupId:[{}]", groupId);
//                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!"));
//                }
                groups.add(groupId);
                EmployeeQueryRequest employeeQueryRequest = EmployeeQueryRequest.builder()
                        .entId(entId)
                        .groupIds(groups)
                        .idNumber(idNumber)
                        .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                        .build();
                log.info("sendCode.employeeQueryRequest[{}]", JacksonUtil.objectToJson(employeeQueryRequest));
                List<EmployeeInfoRes> employeeInfoRes = employeeInfoServiceFeign.empInfoList(employeeQueryRequest);
                log.info("employeeInfoRes.size():[{}]", employeeInfoRes.size());
                if (null != employeeInfoRes && employeeInfoRes.size() == 1) {
                    EmployeeInfoRes employeeInfoRes1 = employeeInfoRes.get(0);
                    phone = employeeInfoRes1.getPhone();
                } else {
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
                }
            } else {
                log.error("?????????????????????busiType:[{}]", sendCodeReqDTO.getBusiType());
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????!"));
            }
            log.info("sendCode.phone:[{}]", phone);
            if (StringUtils.isBlank(phone)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("?????????????????????????????????????????????"));
            }
            //?????????ip?????????????????????inside????????????????????????cloud-wage-manager??????????????????
//            MsgCodeLogRequestDTO dto = new MsgCodeLogRequestDTO();
//            dto.setSystemId(0);
//            dto.setCheckType(1);
//            dto.setBusiType(MsgBuisTypeEnum.SMS_01.getCode());
//            dto.setMsgMedium(phone);
//            dto.setValidTime(120);
//            MsgCodeLogResponeDTO msgCodeLogResponeDTO = callInsideService.sendCode(dto, clientIp);
            SmsCodeSendDTO smsCodeSendDTO = new SmsCodeSendDTO();
            smsCodeSendDTO.setPhone(phone);
            smsCodeSendDTO.setSmsId("S004");
            smsCodeSendDTO.setSysId("fxgj");
            smsCodeSendDTO.setTimeOutMinute(2);
            SmsDTO sendSmsCode = unAuthFeignClient.sendSmsCode(smsCodeSendDTO, "fxgj");
            String key = "inside_send" + phone;
            redisTemplate.opsForValue().set(key, sendSmsCode.getCheckId(), 2, TimeUnit.MINUTES);
            Res100302 res100302 = new Res100302();
            res100302.setCodeId(sendSmsCode.getCheckId());
            log.info("sendCodeRet:[{}]", JacksonUtil.objectToJson(res100302));
            return res100302;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????
     *
     * @param resReceiptDTO
     * @return
     */
    @TrackLog
    @PostMapping("/receipt")
    public Mono<Void> receipt(@RequestBody ResReceiptDTO resReceiptDTO) {
        log.info("receipt resReceiptDTO:[{}]", JacksonUtil.objectToJson(resReceiptDTO));
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            PayrollResReceiptDTO wageResReceiptDTO = new PayrollResReceiptDTO();
            BeanUtils.copyProperties(resReceiptDTO, wageResReceiptDTO);
            WageRetReceiptDTO wageRetReceiptDTO = insideFeignController.receipt(wageResReceiptDTO);
            try {
                log.info("????????????receipt wageRetReceiptDTO:[{}]", JacksonUtil.objectToJson(wageRetReceiptDTO));
                String idNumber = wageRetReceiptDTO.getIdNumber();
                String groupId = wageRetReceiptDTO.getGroupId();
                LocalDateTime crtDateTime = wageRetReceiptDTO.getCrtDateTime();
                //????????????
//                mysqlDataSynToMongo(idNumber, groupId, String.valueOf(crtDateTime.getYear()), "", principal);
            } catch (Exception e) {
                log.info("?????????,??????????????????:[{}]", e);
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ???????????????
     *
     * @param readWageDTO
     * @return
     */
    @PostMapping("/read")
    @TrackLog
    public Mono<Void> readWage(@RequestBody ReadWageDTO readWageDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        String idNumber = WebContext.getCurrentUser().getIdNumberEncrytor();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            core.dto.request.ReadWageDTO wageReadWageDTO = new core.dto.request.ReadWageDTO();
            readWageDTO.setIdNumber(idNumber);
            BeanUtils.copyProperties(readWageDTO, wageReadWageDTO);
            log.info("wageReadWageDTO:[{}]", JacksonUtil.objectToJson(wageReadWageDTO));
            insideFeignController.readWage(wageReadWageDTO);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ???????????????????????????
     *
     * @param req100702
     * @return
     * @throws Exception
     */
    @PostMapping("/bindWX")
    @TrackLog
    public Mono<Void> bandWX(@RequestBody Req100702 req100702) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("bindWX.req100702:[{}]", JacksonUtil.objectToJson(req100702));
            CacheReq100702 wageReq100702 = new CacheReq100702();
            BeanUtils.copyProperties(req100702, wageReq100702);

            wageReq100702.setCodeId(verifyCode(req100702.getPhone()));
            CacheUserPrincipal wageUserPrincipal = new CacheUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);

            CacheBindRequestDTO wageBindRequestDTO = new CacheBindRequestDTO();
            wageBindRequestDTO.setCacheReq100702(wageReq100702);
            wageBindRequestDTO.setCacheUserPrincipal(wageUserPrincipal);

            insideFeignController.bandWX(wageBindRequestDTO);
            log.info("??????????????????????????????");

            //???????????????????????????MySql?????????????????????
            EmpInfoUpdReq empInfoUpdReq = EmpInfoUpdReq.builder()
                    .idNumber(req100702.getIdNumber())
                    .bindStatus(IsBindWechatEnum.ISBINDWECHAT)
                    .build();
            employeeInfoServiceFeign.updEmpInfo(empInfoUpdReq);
            log.info("??????MySql???????????????????????????");
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ???????????????????????????,????????????
     *
     * @param req100701
     * @return
     * @throws Exception
     */
    @PostMapping("/rz")
    @TrackLog
    public Mono<Void> rz(@RequestBody Req100701 req100701) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String wechatId = String.valueOf(WebContext.getCurrentUser().getWechatId());
        String pwd = req100701.getPwd();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CacheReq100701 wageReq100701 = new CacheReq100701();
            BeanUtils.copyProperties(req100701, wageReq100701);
            //wageReq100701.setIdNumber(wageReq100701.getIdNumber().toUpperCase());//??????????????????

            //???????????????????????????
            String password = paswordService.checkNumberPassword(pwd, wechatId);
            wageReq100701.setPwd(password);
            wageReq100701.setCodeId(verifyCode(req100701.getPhone()));
            CacheUserPrincipal wageUserPrincipal = new CacheUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);

            CacheRzRequestDTO wageRzRequestDTO = new CacheRzRequestDTO();
            wageRzRequestDTO.setCacheReq100701(wageReq100701);
            wageRzRequestDTO.setCacheUserPrincipal(wageUserPrincipal);
            String retStr = insideFeignController.rz(wageRzRequestDTO);
            if (!StringUtils.equals("0000", retStr)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format(retStr));
            }

            log.info("??????????????????????????????-????????????");
            //???????????????????????????MySql?????????????????????
            EmpInfoUpdReq empInfoUpdReq = EmpInfoUpdReq.builder()
                    .idNumber(employeeEncrytorService.decryptIdNumber(req100701.getIdNumber()))
                    .bindStatus(IsBindWechatEnum.ISBINDWECHAT)
                    .build();
            employeeInfoServiceFeign.updEmpInfo(empInfoUpdReq);
            log.info("??????MySql???????????????????????????");

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }


    /**
     * ????????????
     *
     * @param pwd
     * @return
     * @throws Exception
     */
    @PostMapping("/setPwd")
    @TrackLog
    public Mono<Void> setPwd(@RequestBody String pwd) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            CacheUserPrincipal wageUserPrincipal = new CacheUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);

            insideFeignController.setPwd(pwd, wageUserPrincipal);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ??????????????????
     *
     * @param updPwdDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/updPwd")
    @TrackLog
    public Mono<Void> updPwd(@RequestBody UpdPwdDTO updPwdDTO) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            CacheUpdPwdDTO wageUpdPwdDTO = new CacheUpdPwdDTO();
            BeanUtils.copyProperties(updPwdDTO, wageUpdPwdDTO);

            UserPrincipal currentUser = WebContext.getCurrentUser();
            CacheUserPrincipal wageUserPrincipal = new CacheUserPrincipal();
            BeanUtils.copyProperties(currentUser, wageUserPrincipal);

            CacheUpPwdRequestDTO wageUpPwdRequestDTO = new CacheUpPwdRequestDTO();
            wageUpPwdRequestDTO.setQueryPwd(currentUser.getQueryPwd());
            wageUpPwdRequestDTO.setCacheUpdPwdDTO(wageUpdPwdDTO);
            wageUpPwdRequestDTO.setCacheUserPrincipal(wageUserPrincipal);
            insideFeignController.updPwd(wageUpPwdRequestDTO);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ?????????????????????
     * <p>
     * ???????????????????????????????????????????????????busiType???????????????????????????
     *
     * @param reqPhone
     * @return
     * @throws Exception
     */
    @PostMapping("/checkPhoneCode")
    @TrackLog
    public Mono<Void> checkPhoneCode(@RequestBody ReqPhone reqPhone, @RequestHeader(value = "jsession-id", required = false) String jsessionId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        log.info("checkPhoneCode.reqPhone:[{}]", JacksonUtil.objectToJson(reqPhone));
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String busiType = reqPhone.getBusiType();
            String phone = "";
            if (StringUtils.equals("0", busiType)) {//0 ?????????????????????(?????????????????????)
                phone = reqPhone.getPhone();
                if (StringUtils.isBlank(phone)) {
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????"));
                }
            } else if (StringUtils.equals("1", busiType)) {//1 ???????????????????????????(????????????????????????jsessionId ????????????????????????)
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                if (null == wechatInfoDetail) {
                    log.error("??????jsessionId:[{}]??????????????????", jsessionId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????"));
                }
                phone = wechatInfoDetail.getPhone();
                if (StringUtils.isBlank(phone)) {
                    log.error("????????????????????????sendCode.wechatInfoDetail:[{}]", JacksonUtil.objectToJson(wechatInfoDetail));
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!"));
                }
            } else if (StringUtils.equals("2", busiType)) {//2 ???????????????????????????
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                if (null == wechatInfoDetail) {
                    log.error("??????jsessionId:[{}]??????????????????", jsessionId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!!"));
                }
                String idNumber = wechatInfoDetail.getIdNumber();
                String entId = wechatInfoDetail.getEntId();
                String[] groupIds = wechatInfoDetail.getGroupIds();
                if (StringUtils.isBlank(idNumber) || StringUtils.isBlank(entId)) {
                    log.error("idNumber:[{}], entId:[{}]", idNumber, entId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!!!"));
                }
                List<String> groups = new ArrayList<>();
                String groupId = reqPhone.getGroupId();
                if (StringUtils.isBlank(groupId)) {
                    log.error("groupId:[{}]", groupId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!"));
                }
//                //????????????groupId????????????????????????
//                boolean tab = true;
//                for (String group_id : groupIds) {
//                    if (StringUtils.equals(groupId, group_id)) {
//                        //?????????groups?????????????????????????????????groupId????????????false???????????????
//                        tab = false;
//                    }
//                }
//                if (tab) {
//                    log.error("?????????????????????????????????groupId:[{}]", groupId);
//                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("??????????????????!"));
//                }
                groups.add(groupId);
                EmployeeQueryRequest employeeQueryRequest = EmployeeQueryRequest.builder()
                        .entId(entId)
                        .groupIds(groups)
                        .idNumber(idNumber)
                        .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                        .build();
                log.info("sendCode.employeeQueryRequest[{}]", JacksonUtil.objectToJson(employeeQueryRequest));
                List<EmployeeInfoRes> employeeInfoRes = employeeInfoServiceFeign.empInfoList(employeeQueryRequest);
                if (null != employeeInfoRes && employeeInfoRes.size() == 1) {
                    EmployeeInfoRes employeeInfoRes1 = employeeInfoRes.get(0);
                    phone = employeeInfoRes1.getPhone();
                } else {
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
                }
            } else {
                log.error("?????????????????????busiType:[{}]", busiType);
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????!"));
            }

            if (StringUtils.isBlank(phone)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("?????????????????????????????????????????????"));
            }
            core.dto.request.ReqPhone wageReqPhone = new core.dto.request.ReqPhone();
            wageReqPhone.setCode(reqPhone.getCode());
            wageReqPhone.setCodeId(verifyCode(phone));
            wageReqPhone.setPhone(phone);
            log.info("checkPhoneCode.wageReqPhone:[{}]", JacksonUtil.objectToJson(wageReqPhone));

            String retStr = insideFeignController.checkPhoneCode(wageReqPhone);
            if (!StringUtils.equals("0000", retStr)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format(retStr));
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ???????????????
     *
     * @param reqPhone
     * @return
     * @throws Exception
     */
    @PostMapping("/updPhone")
    @TrackLog
    public Mono<Void> updPhone(@RequestHeader("ent-id") String entId,
                               @RequestBody ReqPhone reqPhone) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        log.info("updPhone reqPhone:[{}]", JacksonUtil.objectToJson(reqPhone));
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //??????Mongo?????????
            CacheReqPhone wageReqPhone = new CacheReqPhone();
            BeanUtils.copyProperties(reqPhone, wageReqPhone);

            wageReqPhone.setCodeId(verifyCode(reqPhone.getPhone()));
            CacheUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(userPrincipal);
            CacheUpdPhoneRequestDTO cacheUpdPhoneRequestDTO = new CacheUpdPhoneRequestDTO();
            cacheUpdPhoneRequestDTO.setCacheReqPhone(wageReqPhone);
            cacheUpdPhoneRequestDTO.setCacheUserPrincipal(wageUserPrincipal);
            cacheUpdPhoneRequestDTO.setEntId(entId);
            insideFeignController.updPhone(cacheUpdPhoneRequestDTO);

            //??????Mysql?????????
            EmpInfoUpdReq empInfoUpdReq = EmpInfoUpdReq.builder()
                    .entId(entId)
                    .idNumber(userPrincipal.getIdNumber())
                    .phone(reqPhone.getPhone())
                    .build();
            log.info("updPhone.feign:[{}]", JacksonUtil.objectToJson(empInfoUpdReq));
            employeeInfoServiceFeign.updEmpInfo(empInfoUpdReq);

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }


    /**
     * ???????????????
     *
     * @param updBankCardDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/updBankCard")
    @TrackLog
    public Mono<String> updBankCard(@RequestBody UpdBankCardDTO updBankCardDTO) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String idNumber = WebContext.getCurrentUser().getIdNumber();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            String regex = "[0-9]{1,}";
            if (!updBankCardDTO.getCardNo().matches(regex)) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_013.getErrorMsg());
            }
            return insideFeignController.updBankCard(updBankCardDTO, idNumber);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????????????????(??????????????????)
     *
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/empEntList")
    @TrackLog
    public Mono<List<EmpEntResDTO>> empEntList(@RequestHeader(value = "encry-salt", required = false) String salt,
                                               @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                               @RequestHeader(value = "ent-id", required = false) String entId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String idNumber = userPrincipal.getIdNumber();
            log.info("empEntList.idNumber:[{}]", idNumber);
            List<FundLiquidationEnum> dataAuths = userPrincipal.getDataAuths();
            BaseReqDTO baseReqDTO = BaseReqDTO.builder()
                    .idNumber(idNumber)
                    .entId(entId)
                    .dataAuths(dataAuths)
                    .build();
            if (StringUtils.isBlank(idNumber)) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????????????????"));
            }
            List<EmpEntResDTO> empEntResDTOList = insideFeignController.empEntList(baseReqDTO);

            //????????????
            List<EmpEntResDTO> empEntResDTOListRes = new ArrayList<>();
            if (null != empEntResDTOList && empEntResDTOList.size() > 0) {
                for (EmpEntResDTO empEntResDTO : empEntResDTOList) {
                    EmpEntResDTO empEntResDTORes = EmpEntResDTO.builder()
                            .entId(EncrytorUtils.encryptField(empEntResDTO.getEntId(), salt, passwd))
                            .entName(EncrytorUtils.encryptField(empEntResDTO.getEntName(), salt, passwd))
                            .shortEntName(EncrytorUtils.encryptField(empEntResDTO.getShortEntName(), salt, passwd))
                            .liquidation(empEntResDTO.getLiquidation())
                            .version(empEntResDTO.getVersion())
                            .subVersion(empEntResDTO.getSubVersion())
                            .salt(salt)
                            .passwd(passwd)
                            .build();
                    empEntResDTOListRes.add(empEntResDTORes);
                }
            }
            log.info("empEntResDTOListRes:[{}]", JacksonUtil.objectToJson(empEntResDTOListRes));
            return empEntResDTOListRes;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    @GetMapping("/theme")
    @TrackLog
    public Mono<SkinThemeInfoDto> getSkin() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return insideFeignController.getSkin(userPrincipal.getSessionId(), userPrincipal.getAppPartner().getCode().toString());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ??????????????????
     *
     * @param req
     * @return
     */
    @PostMapping("/theme")
    @TrackLog
    public Mono<Void> setSkin(@RequestBody SkinThemeInfoReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        HeaderDTO header = WebContext.getCurrentHeader();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            insideFeignController.setSkin(req);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ??????????????????
     *
     * @param phone
     * @return
     */
    public String verifyCode(String phone) {

        String key = "inside_send" + phone;
        String codeId = (String) redisTemplate.opsForValue().get(key);
        if (null == codeId) {
            throw new ParamsIllegalException(ErrorConstant.Error0004.format());
        }
        return codeId;
    }

}

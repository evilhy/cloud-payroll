package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.exception.ServiceHandleException;
import chain.css.log.annotation.TrackLog;
import chain.feign.hxinside.ent.service.EmployeeInfoServiceFeign;
import chain.fxgj.ent.core.dto.request.EmployeeQueryRequest;
import chain.fxgj.ent.core.dto.response.EmployeeInfoRes;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.MsgCodeLogRequestDTO;
import chain.fxgj.server.payroll.dto.MsgCodeLogResponeDTO;
import chain.fxgj.server.payroll.dto.base.HeaderDTO;
import chain.fxgj.server.payroll.dto.request.ReadWageDTO;
import chain.fxgj.server.payroll.dto.request.*;
import chain.fxgj.server.payroll.dto.request.ReqPhone;
import chain.fxgj.server.payroll.dto.response.Res100302;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.PaswordService;
import chain.fxgj.server.payroll.service.impl.CallInsideServiceImpl;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.fxgj.server.payroll.util.TransferUtil;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.InsideFeignController;
import chain.utils.commons.JacksonUtil;
import chain.utils.fxgj.constant.DictEnums.DelStatusEnum;
import chain.utils.fxgj.constant.DictEnums.MsgBuisTypeEnum;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;

@RestController
@Validated
@RequestMapping("/inside")
@Slf4j
public class InsideController {

//    @Autowired
//    InsideFeignService insideFeignService;
    @Qualifier("applicationTaskExecutor")
    Executor executor;
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

    /**
     * 发送短信验证码
     * busyType 说明
     * 0 明文传输手机号(此时手机号必填)
     * 1 微信绑定手机号验证(上手的手机号可以为空，根据jsessionId 找到绑定的手机号)
     * 2 通过企业绑定的手机
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
            if (StringUtils.equals("0", busiType)) {//0 明文传输手机号(此时手机号必填)
                phone = sendCodeReqDTO.getPhone();
                if (StringUtils.isBlank(phone)) {
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("请填写手机号"));
                }
            } else if (StringUtils.equals("1", busiType)) {//1 微信绑定手机号验证(手机号为空，根据jsessionId 找到绑定的手机号)
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                if (null == wechatInfoDetail) {
                    log.error("根据jsessionId:[{}]未查询到数据", jsessionId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信发送失败"));
                }
                phone = wechatInfoDetail.getPhone();
                if (StringUtils.isBlank(phone)) {
                    log.error("缓存中无手机信息sendCode.wechatInfoDetail:[{}]", JacksonUtil.objectToJson(wechatInfoDetail));
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信发送失败!"));
                }
            } else if (StringUtils.equals("2", busiType)) {//2 通过企业绑定的手机
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                log.info("sedCode.wechatInfoDetail:[{}]", JacksonUtil.objectToJson(wechatInfoDetail));
                if (null == wechatInfoDetail) {
                    log.error("根据jsessionId:[{}]未查询到数据", jsessionId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信发送失败!!"));
                }
                String idNumber = wechatInfoDetail.getIdNumber();
                String entId = wechatInfoDetail.getEntId();
                String[] groupIds = wechatInfoDetail.getGroupIds();
                if (StringUtils.isBlank(idNumber) || StringUtils.isBlank(entId)) {
                    log.error("idNumber:[{}], entId:[{}]", idNumber, entId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信发送失败!!!"));
                }
                List<String> groups = new ArrayList<>();
                String groupId = sendCodeReqDTO.getGroupId();
                if (StringUtils.isBlank(groupId)) {
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信发送失败!"));
                }
//                注释原因：是不是在缓存中 缓存中的无groups，需要查看
//                //校验入参groupId，
//                boolean tab = true;
//                for (String group_id : groupIds) {
//                    if (StringUtils.equals(groupId, group_id)) {
//                        //在缓存groups中能匹配到入参传进来的groupId，设置成false，不跑一次
//                        tab = false;
//                    }
//                }
//                if (tab) {
//                    log.error("在缓存中未匹配到对应的groupId:[{}]", groupId);
//                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信发送失败!"));
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
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信发送失败，请联系客服"));
                }
            } else {
                log.error("业务类型不存在busiType:[{}]", sendCodeReqDTO.getBusiType());
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信发送失败，请联系客服!"));
            }
            log.info("sendCode.phone:[{}]", phone);
            if (StringUtils.isBlank(phone)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("企业员工无手机号，短信发送失败"));
            }
            //需要加ip，所以直接调用inside发送短信，不调用cloud-wage-manager，上面的注释
            MsgCodeLogRequestDTO dto = new MsgCodeLogRequestDTO();
            dto.setSystemId(0);
            dto.setCheckType(1);
            dto.setBusiType(MsgBuisTypeEnum.SMS_01.getCode());
            dto.setMsgMedium(phone);
            dto.setValidTime(120);
            MsgCodeLogResponeDTO msgCodeLogResponeDTO = callInsideService.sendCode(dto, clientIp);
            Res100302 res100302 = new Res100302();
            res100302.setCodeId(msgCodeLogResponeDTO.getCodeId());
            log.info("sendCodeRet:[{}]", JacksonUtil.objectToJson(res100302));
            return res100302;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 员工回执
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
                log.info("同步数据receipt wageRetReceiptDTO:[{}]", JacksonUtil.objectToJson(wageRetReceiptDTO));
                String idNumber = wageRetReceiptDTO.getIdNumber();
                String groupId = wageRetReceiptDTO.getGroupId();
                LocalDateTime crtDateTime = wageRetReceiptDTO.getCrtDateTime();
                //切库注释
//                mysqlDataSynToMongo(idNumber, groupId, String.valueOf(crtDateTime.getYear()), "", principal);
            } catch (Exception e) {
                log.info("回执后,同步数据失败:[{}]", e);
            }
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 已读工资条
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
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 微信号绑定身份证号
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

            CacheReq100702 wageReq100702 = new CacheReq100702();
            BeanUtils.copyProperties(req100702, wageReq100702);

            CacheUserPrincipal wageUserPrincipal = new CacheUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);

            CacheBindRequestDTO wageBindRequestDTO = new CacheBindRequestDTO();
            wageBindRequestDTO.setCacheReq100702(wageReq100702);
            wageBindRequestDTO.setCacheUserPrincipal(wageUserPrincipal);

            insideFeignController.bandWX(wageBindRequestDTO);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 微信号绑定身份证号,无手机号
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

            //数字键盘密码，解密
            String password = paswordService.checkNumberPassword(pwd, wechatId);
            wageReq100701.setPwd(password);

            CacheUserPrincipal wageUserPrincipal = new CacheUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);

            CacheRzRequestDTO wageRzRequestDTO = new CacheRzRequestDTO();
            wageRzRequestDTO.setCacheReq100701(wageReq100701);
            wageRzRequestDTO.setCacheUserPrincipal(wageUserPrincipal);
            String retStr = insideFeignController.rz(wageRzRequestDTO);
            if (!StringUtils.equals("0000", retStr)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format(retStr));
            }
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }


    /**
     * 设置密码
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
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 修改查询密码
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
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 验证手机验证码
     *
     * 数据脱敏后，无法获取手机号，现增加busiType区分场景获取手机号
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
            if (StringUtils.equals("0", busiType)) {//0 明文传输手机号(此时手机号必填)
                phone = reqPhone.getPhone();
                if (StringUtils.isBlank(phone)) {
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("请填写手机号"));
                }
            } else if (StringUtils.equals("1", busiType)) {//1 微信绑定手机号验证(手机号为空，根据jsessionId 找到绑定的手机号)
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                if (null == wechatInfoDetail) {
                    log.error("根据jsessionId:[{}]未查询到数据", jsessionId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信验证失败"));
                }
                phone = wechatInfoDetail.getPhone();
                if (StringUtils.isBlank(phone)) {
                    log.error("缓存中无手机信息sendCode.wechatInfoDetail:[{}]", JacksonUtil.objectToJson(wechatInfoDetail));
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信验证失败!"));
                }
            } else if (StringUtils.equals("2", busiType)) {//2 通过企业绑定的手机
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                if (null == wechatInfoDetail) {
                    log.error("根据jsessionId:[{}]未查询到数据", jsessionId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信验证失败!!"));
                }
                String idNumber = wechatInfoDetail.getIdNumber();
                String entId = wechatInfoDetail.getEntId();
                String[] groupIds = wechatInfoDetail.getGroupIds();
                if (StringUtils.isBlank(idNumber) || StringUtils.isBlank(entId)) {
                    log.error("idNumber:[{}], entId:[{}]", idNumber, entId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信验证失败!!!"));
                }
                List<String> groups = new ArrayList<>();
                String groupId = reqPhone.getGroupId();
                if (StringUtils.isBlank(groupId)) {
                    log.error("groupId:[{}]", groupId);
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信验证失败!"));
                }
//                //校验入参groupId，是不是在缓存中
//                boolean tab = true;
//                for (String group_id : groupIds) {
//                    if (StringUtils.equals(groupId, group_id)) {
//                        //在缓存groups中能匹配到入参传进来的groupId，设置成false，不跑一次
//                        tab = false;
//                    }
//                }
//                if (tab) {
//                    log.error("在缓存中未匹配到对应的groupId:[{}]", groupId);
//                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信验证失败!"));
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
                    throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信验证失败，请联系客服"));
                }
            } else {
                log.error("业务类型不存在busiType:[{}]", busiType);
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("短信验证失败，请联系客服!"));
            }

            if (StringUtils.isBlank(phone)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("企业员工无手机号，短信验证失败"));
            }
            core.dto.request.ReqPhone wageReqPhone = new core.dto.request.ReqPhone();
            wageReqPhone.setCode(reqPhone.getCode());
            wageReqPhone.setCodeId(reqPhone.getCodeId());
            wageReqPhone.setPhone(phone);
            log.info("checkPhoneCode.wageReqPhone:[{}]", JacksonUtil.objectToJson(wageReqPhone));

            String retStr = insideFeignController.checkPhoneCode(wageReqPhone);
            if (!StringUtils.equals("0000", retStr)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format(retStr));
            }
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 修改手机号
     *
     * @param reqPhone
     * @return
     * @throws Exception
     */
    @PostMapping("/updPhone")
    @TrackLog
    public Mono<Void> updPhone(@RequestBody ReqPhone reqPhone) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        log.info("updPhone reqPhone:[{}]", JacksonUtil.objectToJson(reqPhone));
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CacheReqPhone wageReqPhone = new CacheReqPhone();
            BeanUtils.copyProperties(reqPhone, wageReqPhone);

            CacheUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(userPrincipal);
            CacheUpdPhoneRequestDTO cacheUpdPhoneRequestDTO = new CacheUpdPhoneRequestDTO();
            cacheUpdPhoneRequestDTO.setCacheReqPhone(wageReqPhone);
            cacheUpdPhoneRequestDTO.setCacheUserPrincipal(wageUserPrincipal);
            insideFeignController.updPhone(cacheUpdPhoneRequestDTO);

            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }


    /**
     * 修改银行卡
     *
     * @param updBankCardDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/updBankCard")
    @TrackLog
    public Mono<String> updBankCard(@RequestBody UpdBankCardDTO updBankCardDTO) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            String regex = "[0-9]{1,}";
            if (!updBankCardDTO.getCardNo().matches(regex)) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_013.getErrorMsg());
            }
            return insideFeignController.updBankCard(updBankCardDTO);
        }).subscribeOn(Schedulers.elastic());
    }

// 切库注释
//    public void mysqlDataSynToMongo(String idNumber, String groupId, String year, String type, UserPrincipal principal) {
//        WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
//        BeanUtils.copyProperties(principal, wageUserPrincipal);
//        wageSynFeignService.pushSyncDataToCache(idNumber, groupId, year, type, wageUserPrincipal);
//    }

    /**
     * 查询员工企业列表(切库新增接口)
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
            BaseReqDTO baseReqDTO = BaseReqDTO.builder()
                    .idNumber(idNumber)
                    .entId(entId)
                    .build();
            if (StringUtils.isBlank(idNumber)) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("身份证为空查询不到数据"));
            }
            List<EmpEntResDTO> empEntResDTOList = insideFeignController.empEntList(baseReqDTO);

            //数据加密
            List<EmpEntResDTO> empEntResDTOListRes = new ArrayList<>();
            if (null != empEntResDTOList && empEntResDTOList.size() > 0) {
                for (EmpEntResDTO empEntResDTO : empEntResDTOList) {
                    EmpEntResDTO empEntResDTORes = EmpEntResDTO.builder()
                            .entId(EncrytorUtils.encryptField(empEntResDTO.getEntId(), salt, passwd))
                            .entName(EncrytorUtils.encryptField(empEntResDTO.getEntName(), salt, passwd))
                            .shortEntName(EncrytorUtils.encryptField(empEntResDTO.getShortEntName(), salt, passwd))
                            .salt(salt)
                            .passwd(passwd)
                            .build();
                    empEntResDTOListRes.add(empEntResDTORes);
                }
            }
            log.info("empEntResDTOListRes:[{}]", JacksonUtil.objectToJson(empEntResDTOListRes));
            return empEntResDTOListRes;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 获取该用户主题信息
     * @return
     */
    @GetMapping("/theme")
    @TrackLog
    public Mono<SkinThemeInfoDto> getSkin(){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        HeaderDTO header = WebContext.getCurrentHeader();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return insideFeignController.getSkin();
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 设置主题信息
     * @param req
     * @return
     */
    @PostMapping("/theme")
    @TrackLog
    public Mono<Void> setSkin(@RequestBody SkinThemeInfoReq req){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        HeaderDTO header = WebContext.getCurrentHeader();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            insideFeignController.setSkin(req);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

}

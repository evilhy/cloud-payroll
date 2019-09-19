package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.exception.ServiceHandleException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.MsgBuisTypeEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.dto.msg.MsgCodeLogRequestDTO;
import chain.fxgj.core.common.dto.msg.MsgCodeLogResponeDTO;
import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.InsideService;
import chain.fxgj.core.common.service.WechatBindService;
import chain.fxgj.core.jpa.model.CardbinInfo;
import chain.fxgj.feign.client.InsideFeignService;
import chain.fxgj.feign.dto.request.*;
import chain.fxgj.feign.dto.response.WageRes100302;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.request.*;
import chain.fxgj.server.payroll.dto.response.Res100302;
import chain.fxgj.server.payroll.service.CallInsideService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/inside")
@Slf4j
public class InsideController {

    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    WechatBindService wechatBindService;
    @Autowired
    InsideService insideService;
    @Autowired
    CallInsideService callInsideService;
    @Autowired
    InsideFeignService insideFeignService;

    /**
     * 发送短信验证码
     *
     * @param req100302
     * @return
     */
    @PostMapping("/sendCode")
    @TrackLog
    @PermitAll
    public Mono<Res100302> sendCode(@RequestBody Req100302 req100302) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageReq100302 wageIndexDTO = new WageReq100302();
            wageIndexDTO.setPhone(req100302.getPhone());

            WageRes100302 wageRes100302 = insideFeignService.sendCode(wageIndexDTO);
            Res100302 res100302 = new Res100302();
            BeanUtils.copyProperties(wageRes100302, res100302);
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
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageResReceiptDTO wageResReceiptDTO = new WageResReceiptDTO();
            BeanUtils.copyProperties(resReceiptDTO, wageResReceiptDTO);
            insideFeignService.receipt(wageResReceiptDTO);
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
            WageReadWageDTO wageReadWageDTO = new WageReadWageDTO();
            readWageDTO.setIdNumber(idNumber);
            BeanUtils.copyProperties(readWageDTO, wageReadWageDTO);
            log.info("wageReadWageDTO:[{}]", JacksonUtil.objectToJson(wageReadWageDTO));
            insideFeignService.readWage(wageReadWageDTO);
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

            WageReq100702 wageReq100702 = new WageReq100702();
            BeanUtils.copyProperties(req100702, wageReq100702);

            WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);

            WageBindRequestDTO wageBindRequestDTO = new WageBindRequestDTO();
            wageBindRequestDTO.setWageReq100702(wageReq100702);
            wageBindRequestDTO.setWageUserPrincipal(wageUserPrincipal);

            insideFeignService.bandWX(wageBindRequestDTO);
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
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            WageReq100701 wageReq100701 = new WageReq100701();
            BeanUtils.copyProperties(req100701, wageReq100701);

            WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);

            WageRzRequestDTO wageRzRequestDTO = new WageRzRequestDTO();
            wageRzRequestDTO.setWageReq100701(wageReq100701);
            wageRzRequestDTO.setWageUserPrincipal(wageUserPrincipal);
            String retStr = insideFeignService.rz(wageRzRequestDTO);
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

            WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);

            insideFeignService.setPwd(pwd, wageUserPrincipal);
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

            WageUpdPwdDTO wageUpdPwdDTO = new WageUpdPwdDTO();
            BeanUtils.copyProperties(updPwdDTO, wageUpdPwdDTO);

            UserPrincipal currentUser = WebContext.getCurrentUser();
            WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
            BeanUtils.copyProperties(currentUser, wageUserPrincipal);

            WageUpPwdRequestDTO wageUpPwdRequestDTO = new WageUpPwdRequestDTO();
            wageUpPwdRequestDTO.setQueryPwd(currentUser.getQueryPwd());
            wageUpPwdRequestDTO.setWageUpdPwdDTO(wageUpdPwdDTO);
            wageUpPwdRequestDTO.setWageUserPrincipal(wageUserPrincipal);
            insideFeignService.updPwd(wageUpPwdRequestDTO);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 验证手机验证码
     *
     * @param reqPhone
     * @return
     * @throws Exception
     */
    @PostMapping("/checkPhoneCode")
    @TrackLog
    public Mono<Void> checkPhoneCode(@RequestBody ReqPhone reqPhone) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            WageReqPhone wageReqPhone = new WageReqPhone();
            BeanUtils.copyProperties(reqPhone, wageReqPhone);

            String retStr = insideFeignService.checkPhoneCode(wageReqPhone);
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

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageReqPhone wageReqPhone = new WageReqPhone();
            BeanUtils.copyProperties(reqPhone, wageReqPhone);

            WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageReqPhone);

            WageUpdPhoneRequestDTO wageUpdPhoneRequestDTO = new WageUpdPhoneRequestDTO();
            wageUpdPhoneRequestDTO.setWageReqPhone(wageReqPhone);
            wageUpdPhoneRequestDTO.setWageUserPrincipal(wageUserPrincipal);

            insideFeignService.updPhone(wageUpdPhoneRequestDTO);
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

            WageUpdBankCardDTO wageUpdBankCardDTO = new WageUpdBankCardDTO();
            BeanUtils.copyProperties(updBankCardDTO, wageUpdBankCardDTO);

            WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);

            WageUpdBankCardRequestDTO wageUpdBankCardRequestDTO = new WageUpdBankCardRequestDTO();
            wageUpdBankCardRequestDTO.setWageUpdBankCardDTO(wageUpdBankCardDTO);
            wageUpdBankCardRequestDTO.setWageUserPrincipal(wageUserPrincipal);

            String retStr = insideFeignService.updBankCard(wageUpdBankCardRequestDTO);
            return retStr;
        }).subscribeOn(Schedulers.elastic());
    }

}

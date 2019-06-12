package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.MsgBuisTypeEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.dto.msg.MsgCodeLogRequestDTO;
import chain.fxgj.core.common.dto.msg.MsgCodeLogResponeDTO;
import chain.fxgj.core.common.service.*;
import chain.fxgj.core.jpa.model.CardbinInfo;
import chain.fxgj.server.payroll.dto.request.*;
import chain.fxgj.server.payroll.dto.response.Res100302;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.util.Map;

@CrossOrigin
@RestController
@Validated
@RequestMapping("/inside")
@Slf4j
public class InsideRS {

    @Autowired
    PayrollProperties payrollProperties;

    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    PayRollAsyncService payRollAsyncService;
    @Autowired
    WechatBindService wechatBindService;
    @Autowired
    InsideService insideService;
    @Autowired
    CallInsideService callInsideService;


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

            MsgCodeLogRequestDTO dto = new MsgCodeLogRequestDTO();
            dto.setSystemId(0);
            dto.setCheckType(1);
            dto.setBusiType(MsgBuisTypeEnum.SMS_01.getCode());
            dto.setMsgMedium(req100302.getPhone());

            MsgCodeLogResponeDTO msgCodeLogResponeDTO = callInsideService.sendCode(dto);

            Res100302 res100302 = new Res100302();
            res100302.setCodeId(msgCodeLogResponeDTO.getCodeId());
            res100302.setCode(msgCodeLogResponeDTO.getCode());
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

            insideService.recepitConfirm(resReceiptDTO);
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

            readWageDTO.setIdNumber(idNumber);
            insideService.readWage(readWageDTO);
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

            String sessionId = userPrincipal.getSessionId();
            String openId = userPrincipal.getOpenId();
            String idNumber = req100702.getIdNumber();
            //验证短信码
            //this.checkPhoneCode(req100702.getPhone(), req100702.getCode());
            callInsideService.checkPhoneCode(req100702.getPhone(), req100702.getCode());
            //请求绑定
            insideService.bandWechat(openId, idNumber, req100702.getPhone());
            //绑定成功后确认登录
            idNumber = employeeEncrytorService.decryptIdNumber(req100702.getIdNumber());
            empWechatService.setWechatInfo(sessionId, openId, userPrincipal.getNickname(), userPrincipal.getHeadimgurl(), idNumber,AppPartnerEnum.FXGJ);
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

            String sessionId = userPrincipal.getSessionId();
            String openId = userPrincipal.getOpenId();
            String idNumber = employeeEncrytorService.decryptIdNumber(req100701.getIdNumber());
            log.info("请求参数:{}", req100701);
            //验证手机号是否存在
            wechatBindService.checkPhone(idNumber, req100701.getPhone());

            //验证短信码
            //this.checkPhoneCode(req100701.getPhone(), req100701.getCode());
            callInsideService.checkPhoneCode(req100701.getPhone(), req100701.getCode());

            //请求管家绑定
            insideService.bandWechatAndPhone(openId, idNumber, req100701.getPhone(), req100701.getPwd());
            //绑定成功后确认登录
            empWechatService.setWechatInfo(sessionId, openId, userPrincipal.getNickname(), userPrincipal.getHeadimgurl(), idNumber, AppPartnerEnum.FXGJ);
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

            log.info("userPrincipal:{}", userPrincipal);
            UserPrincipal wechatInfo = empWechatService.getWechatInfo(userPrincipal.getSessionId());
            log.info("wechatInfo:{}", wechatInfo);
            SetPwdDTO setPwdDTO = new SetPwdDTO();
            setPwdDTO.setPwd(pwd);
            setPwdDTO.setWechatId(userPrincipal.getWechatId());
            log.info("设置密码请求参数前:{}", setPwdDTO);
            if (StringUtils.isBlank(userPrincipal.getWechatId())) {
                String wechatId = empWechatService.getWechatId(wechatInfo.getOpenId());
                setPwdDTO.setWechatId(wechatId);
                log.info("设置密码请求参数后:{}", setPwdDTO);
            }

            insideService.setPwd(setPwdDTO.getWechatId(), pwd);

            empWechatService.setWechatInfo(userPrincipal.getSessionId(), userPrincipal.getOpenId(), userPrincipal.getNickname(), userPrincipal.getHeadimgurl(), userPrincipal.getIdNumber(),AppPartnerEnum.FXGJ);

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

        String queryPwd = WebContext.getCurrentUser().getQueryPwd();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //判断原密码是否正确
            if (!queryPwd.equals(employeeEncrytorService.encryptPwd(updPwdDTO.getOldPwd()))) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_005.getErrorMsg());
            }
            this.setPwd(updPwdDTO.getPwd());
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

            //this.checkPhoneCode(reqPhone.getPhone(), reqPhone.getCode());
            callInsideService.checkPhoneCode(reqPhone.getPhone(), reqPhone.getCode());
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

            if (StringUtils.isNotBlank(reqPhone.getCode())) {
                //this.checkPhoneCode(reqPhone.getPhone(), reqPhone.getCode());
                callInsideService.checkPhoneCode(reqPhone.getPhone(), reqPhone.getCode());
            }
            //验证手机号是否存在
            wechatBindService.checkPhone(userPrincipal.getIdNumber(), reqPhone.getPhone());

            insideService.updPhone(userPrincipal.getWechatId(), userPrincipal.getIdNumber(), reqPhone.getPhone());
            empWechatService.setWechatInfo(userPrincipal.getSessionId(), userPrincipal.getOpenId(), userPrincipal.getNickname(), userPrincipal.getHeadimgurl(), userPrincipal.getIdNumber(),AppPartnerEnum.FXGJ);

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
            //验证银行卡
            CardbinInfo cardbinInfo = empWechatService.checkCard(userPrincipal.getIdNumber(), updBankCardDTO);

            updBankCardDTO.setIssuerBankId(cardbinInfo.getIssuerCode());
            updBankCardDTO.setIssuerName(cardbinInfo.getIssuerName());

            insideService.updBankCard(updBankCardDTO);

            return cardbinInfo.getIssuerName();
        }).subscribeOn(Schedulers.elastic());
    }

}

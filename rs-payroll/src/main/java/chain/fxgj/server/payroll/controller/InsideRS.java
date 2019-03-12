package chain.fxgj.server.payroll.controller;

import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.constant.DictEnums.MsgBuisTypeEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.fxgj.core.common.dto.msg.MsgCodeLogCheckRequestDTO;
import chain.fxgj.core.common.dto.msg.MsgCodeLogRequestDTO;
import chain.fxgj.core.common.dto.msg.MsgCodeLogResponeDTO;
import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.PayRollAsyncService;
import chain.fxgj.core.jpa.model.CardbinInfo;
import chain.fxgj.server.payroll.dto.base.ErrorDTO;
import chain.fxgj.server.payroll.dto.request.*;
import chain.fxgj.server.payroll.dto.response.Res100302;
import chain.fxgj.server.payroll.service.WechatBindService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebSecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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

    /**
     * 发送短信验证码
     * @param req100302
     * @return
     */
    @PostMapping("/sendCode")
    @TrackLog
    @PermitAll
    public Mono<Res100302> sendCode(@RequestBody Req100302 req100302) {
        return Mono.fromCallable(()->{
            MsgCodeLogRequestDTO dto = new MsgCodeLogRequestDTO();
            dto.setSystemId(0);
            dto.setCheckType(1);
            dto.setBusiType(MsgBuisTypeEnum.SMS_01.getCode());
            dto.setMsgMedium(req100302.getPhone());

            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "msgCode/smsCode");
            Response response = webTarget.request()
                    .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                    .post(Entity.entity(dto, MediaType.APPLICATION_JSON_TYPE));
            MsgCodeLogResponeDTO responeDTO = response.readEntity(MsgCodeLogResponeDTO.class);

            Res100302 res100302 = new Res100302();
            res100302.setCodeId(responeDTO.getCodeId());
            res100302.setCode(responeDTO.getCode());
            return res100302;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 员工回执
     * @param resReceiptDTO
     * @return
     */
    @TrackLog
    @PostMapping("/receipt")
    public Mono<Void> receipt(@RequestBody ResReceiptDTO resReceiptDTO) {
        return Mono.fromCallable(()->{
            //请求管家回执
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "roll/receipt");
            log.info("管家url:{}", webTarget.getUri());
            Response response = webTarget.request()
                    .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                    .post(Entity.entity(resReceiptDTO, MediaType.APPLICATION_JSON_TYPE));
            log.debug("{},{}", response.getStatus(), response.readEntity(String.class));
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 已读工资条
     * @param readWageDTO
     * @return
     */
    @PostMapping("/read")
    @TrackLog
    public Mono<Void> readWage(@RequestBody ReadWageDTO readWageDTO) {
        return Mono.fromCallable(()->{
            String idNumber = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal().getIdNumberEncrytor();
            readWageDTO.setIdNumber(idNumber);
            payRollAsyncService.readWage(readWageDTO);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 微信号绑定身份证号
     * @param req100702
     * @return
     * @throws Exception
     */
    @PostMapping("/bindWX")
    @TrackLog
    public Mono<Void> bandWX(@RequestBody Req100702 req100702) throws Exception {
        return Mono.fromCallable(()->{
            UserPrincipal userPrincipal=WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();
            String sessionId = userPrincipal.getSessionId();
            String openId = userPrincipal.getOpenId();
            String idNumber = req100702.getIdNumber();
            //验证短信码
            this.checkPhoneCode(req100702.getPhone(),req100702.getCode());
            //请求管家绑定
            Client client = ClientBuilder.newClient();
            BindWechatDTO bindWechatDTO = new BindWechatDTO();
            bindWechatDTO.setOpenId(openId);
            bindWechatDTO.setIdNumber(idNumber);
            bindWechatDTO.setPhone(req100702.getPhone());
            WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "roll/bindWechat");
            Response response = webTarget.request()
                    .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                    .post(Entity.entity(bindWechatDTO, MediaType.APPLICATION_JSON_TYPE));
            log.debug("{},{}", response.getStatus(), response.readEntity(String.class));
            //绑定成功后确认登录
            empWechatService.setWechatInfo(sessionId, openId,userPrincipal.getNickname(),userPrincipal.getHeadimgurl(), idNumber);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 微信号绑定身份证号,无手机号
     * @param req100701
     * @return
     * @throws Exception
     */
    @PostMapping("/rz")
    @TrackLog
    public Mono<Void> rz(@RequestBody Req100701 req100701) throws Exception {
        return Mono.fromCallable(()->{
            UserPrincipal userPrincipal=WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();
            String sessionId = userPrincipal.getSessionId();
            String openId = userPrincipal.getOpenId();
            String idNumber=employeeEncrytorService.decryptIdNumber(req100701.getIdNumber());
            log.info("请求参数:{}",req100701);
            //验证手机号是否存在
            wechatBindService.checkPhone(idNumber,req100701.getPhone());

            //验证短信码
            this.checkPhoneCode(req100701.getPhone(),req100701.getCode());

            //请求管家绑定
            Client client = ClientBuilder.newClient();
            BindWechatDTO bindWechatDTO = new BindWechatDTO();
            bindWechatDTO.setOpenId(openId);
            bindWechatDTO.setIdNumber(idNumber);
            bindWechatDTO.setPhone(req100701.getPhone());
            bindWechatDTO.setPwd(req100701.getPwd());
            WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "roll/bindWechat1");
            Response response = webTarget.request()
                    .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                    .post(Entity.entity(bindWechatDTO, MediaType.APPLICATION_JSON_TYPE));
            log.debug("{},{}", response.getStatus(), response.readEntity(String.class));
            //绑定成功后确认登录
            empWechatService.setWechatInfo(sessionId, openId,userPrincipal.getNickname(),userPrincipal.getHeadimgurl(),idNumber);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }


    /**
     * 设置密码
     * @param pwd
     * @return
     * @throws Exception
     */
    @PostMapping("/setPwd")
    @TrackLog
    public Mono<Void> setPwd(String pwd) throws Exception {
        return Mono.fromCallable(()->{
            UserPrincipal userPrincipal=WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();
            log.info("userPrincipal:{}",userPrincipal);
            UserPrincipal wechatInfo = empWechatService.getWechatInfo(userPrincipal.getSessionId());
            log.info("wechatInfo:{}",wechatInfo);
            SetPwdDTO setPwdDTO=new SetPwdDTO();
            setPwdDTO.setPwd(pwd);
            setPwdDTO.setWechatId(userPrincipal.getWechatId());
            log.info("设置密码请求参数前:{}",setPwdDTO);
            if(StringUtils.isBlank(userPrincipal.getWechatId())){
                String wechatId = empWechatService.getWechatId(wechatInfo.getOpenId());
                setPwdDTO.setWechatId(wechatId);
                log.info("设置密码请求参数后:{}",setPwdDTO);
            }
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "roll/setPwd");
            Response response = webTarget.request()
                    .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                    .post(Entity.entity(setPwdDTO, MediaType.APPLICATION_JSON_TYPE));
            log.debug("{},{}", response.getStatus(), response.readEntity(String.class));

            empWechatService.setWechatInfo(userPrincipal.getSessionId(), userPrincipal.getOpenId(),userPrincipal.getNickname(),userPrincipal.getHeadimgurl(),userPrincipal.getIdNumber());

            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 修改查询密码
     * @param updPwdDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/updPwd")
    @TrackLog
    public Mono<Void> updPwd(@RequestBody UpdPwdDTO updPwdDTO) throws Exception {
        return Mono.fromCallable(()->{
            String queryPwd = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal().getQueryPwd();
            //判断原密码是否正确
            if(!queryPwd.equals(employeeEncrytorService.encryptPwd(updPwdDTO.getOldPwd()))){
                throw new ParamsIllegalException(ErrorConstant.WECHAR_005.getErrorMsg());
            }
            this.setPwd(updPwdDTO.getPwd());
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 验证手机验证码
     * @param reqPhone
     * @return
     * @throws Exception
     */
    @PostMapping("/checkPhoneCode")
    @TrackLog
    public Mono<Void> checkPhoneCode(@RequestBody ReqPhone reqPhone) throws Exception {
        return Mono.fromCallable(()->{
            this.checkPhoneCode(reqPhone.getPhone(),reqPhone.getCode());
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 修改手机号
     * @param reqPhone
     * @return
     * @throws Exception
     */
    @PostMapping("/updPhone")
    @TrackLog
    public Mono<Void> updPhone(@RequestBody ReqPhone reqPhone) throws Exception {
        return Mono.fromCallable(()->{
            UserPrincipal userPrincipal=WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();
            if(StringUtils.isNotBlank(reqPhone.getCode())) {
                this.checkPhoneCode(reqPhone.getPhone(), reqPhone.getCode());
            }
            //验证手机号是否存在
            wechatBindService.checkPhone(userPrincipal.getIdNumber(),reqPhone.getPhone());
            UpdPhoneDTO updPhoneDTO=new UpdPhoneDTO();
            updPhoneDTO.setIdNumber(userPrincipal.getIdNumber());
            updPhoneDTO.setPhone(reqPhone.getPhone());
            updPhoneDTO.setWechatId(userPrincipal.getWechatId());
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "roll/updPhone");
            Response response = webTarget.request()
                    .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                    .post(Entity.entity(updPhoneDTO, MediaType.APPLICATION_JSON_TYPE));
            log.debug("{},{}", response.getStatus(), response.readEntity(String.class));

            empWechatService.setWechatInfo(userPrincipal.getSessionId(), userPrincipal.getOpenId(),userPrincipal.getNickname(),userPrincipal.getHeadimgurl(),userPrincipal.getIdNumber());

            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    //验证短信验证码
    private void checkPhoneCode(String phone,String code){
        //验证短信码
        MsgCodeLogCheckRequestDTO dto = new MsgCodeLogCheckRequestDTO();
        dto.setSystemId(0);
        dto.setCheckType(1);
        dto.setBusiType(MsgBuisTypeEnum.SMS_01.getCode());
        dto.setCode(code);
        dto.setMsgMedium(phone);
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "msgCode/smsCodeCheck");
        log.info("管家url:{}", webTarget.getUri());
        Response response = webTarget.request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .post(Entity.entity(dto, MediaType.APPLICATION_JSON_TYPE));
        log.debug("{}", response.getStatus());
        if (response.getStatus() == 500) {
            throw new ParamsIllegalException(ErrorConstant.WECHAR_008.getErrorMsg());
        }
        if (response.getStatus() != 200) {
            ErrorDTO errorDTO = response.readEntity(ErrorDTO.class);
            throw new ParamsIllegalException(new ErrorMsg(errorDTO.getErrCode(), errorDTO.getErrMsg()));
        }
        MsgCodeLogResponeDTO msgCodeLogResponeDTO = response.readEntity(MsgCodeLogResponeDTO.class);
        if (msgCodeLogResponeDTO.getMsgStatus() != 1) {
            throw new ParamsIllegalException(ErrorConstant.Error0004.getErrorMsg());
        }
    }

    /**
     * 修改银行卡
     * @param updBankCardDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/updBankCard")
    @TrackLog
    public Mono<String> updBankCard(@RequestBody UpdBankCardDTO updBankCardDTO) throws Exception {
        return Mono.fromCallable(()->{
            UserPrincipal userPrincipal=WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();
            String regex = "[0-9]{1,}";
            if(!updBankCardDTO.getCardNo().matches(regex)){
                throw new ParamsIllegalException(ErrorConstant.WECHAR_013.getErrorMsg());
            }
            //验证银行卡
            CardbinInfo cardbinInfo=empWechatService.checkCard(userPrincipal.getIdNumber(), updBankCardDTO);

            updBankCardDTO.setIssuerBankId(cardbinInfo.getIssuerCode());
            updBankCardDTO.setIssuerName(cardbinInfo.getIssuerName());

            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "roll/updBankCard");
            Response response = webTarget.request()
                    .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                    .post(Entity.entity(updBankCardDTO, MediaType.APPLICATION_JSON_TYPE));
            log.debug("{},{}", response.getStatus(), response.readEntity(String.class));

            return cardbinInfo.getIssuerName();
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 修改银行卡已读
     * @param logIds
     * @return
     */
    @PostMapping("/bankCardIsNew")
    @TrackLog
    @Async
    public Mono<Void> bankCardIsNew(@RequestBody List<String> logIds){
        return Mono.fromCallable(()->{
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "roll/bankCardIsNew");
            Response response = webTarget.request()
                    .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                    .post(Entity.entity(logIds, MediaType.APPLICATION_JSON_TYPE));
            log.debug("{},{}", response.getStatus(), response.readEntity(String.class));
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

}

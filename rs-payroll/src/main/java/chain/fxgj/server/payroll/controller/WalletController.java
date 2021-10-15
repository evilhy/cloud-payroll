package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.PageDTO;
import chain.fxgj.server.payroll.dto.tax.SigningDetailRes;
import chain.fxgj.server.payroll.dto.tax.WalletH5Req;
import chain.fxgj.server.payroll.dto.tax.WalletH5Res;
import chain.fxgj.server.payroll.dto.wallet.*;
import chain.fxgj.server.payroll.service.TaxService;
import chain.fxgj.server.payroll.service.WalletService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.EmployeeTaxAttestFeignService;
import chain.payroll.client.feign.EmployeeTaxSigningFeignService;
import chain.payroll.client.feign.WechatFeignController;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.JsonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.fxgj.constant.DictEnums.AttestStatusEnum;
import chain.utils.fxgj.constant.DictEnums.DelStatusEnum;
import chain.utils.fxgj.constant.DictEnums.IsStatusEnum;
import core.dto.request.employeeTaxAttest.EmployeeTaxAttestQueryReq;
import core.dto.request.employeeTaxSigning.EmployeeTaxSigningQueryReq;
import core.dto.request.employeeTaxSigning.EmployeeTaxSigningSaveReq;
import core.dto.response.employeeTaxAttest.EmployeeTaxAttestDTO;
import core.dto.response.employeeTaxSigning.EmployeeTaxSigningDTO;
import core.dto.wechat.EmployeeWechatDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 放薪钱包
 */
@RestController
@RequestMapping("/wallet")
@Slf4j
public class WalletController {

    @Autowired
    WalletService walletService;

    @Autowired
    WechatFeignController wechatFeignController;
    @Autowired
    EmployeeTaxAttestFeignService employeeTaxAttestFeignService;
    @Autowired
    EmployeeTaxSigningFeignService employeeTaxSigningFeignService;
    @Autowired
    TaxService taxService;

    /**
     * 获取登陆人信息
     *
     * @param jsessionId
     * @return
     */
    public EmployeeWechatDTO findByJsessionId(String jsessionId, String name) {
        log.info("=====> 根据JsessionId查询用户信息 jsessionId:{}", jsessionId);
        EmployeeWechatDTO dto = wechatFeignController.findByJsessionId(jsessionId);
        if (null == dto) {
            throw new ParamsIllegalException(ErrorConstant.Error0001.format("登录人"));
        }
        if (StringUtils.isBlank(dto.getName())) {
            dto.setName(name);
        }
        return dto;

    }

    /**
     * 查询当前用户钱包余额
     *
     * @param entId
     * @return
     */
    @GetMapping("/balance")
    @TrackLog
    public Mono<WalletBalanceDTO> balance(@RequestHeader(value = "encry-salt", required = false) String salt,
                                          @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                          @RequestHeader(value = "ent-id") String entId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /wallet/balance 查询当前用户钱包余额 entId:{}, jsessionId:{}, salt:{}, passwd:{}", entId, jsessionId, salt, passwd);

            //查询当前登陆人信息
            EmployeeWechatDTO dto = findByJsessionId(jsessionId, currentUser.getName());

            //业务处理
            WalletBalanceDTO balanceDTO = walletService.balance(entId, dto, salt, passwd);
            return balanceDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 查询
     * 1.员工银行卡</p>
     * 2.钱包余额</p>
     * 查询当前企业下的钱包余额、银行卡数(去重)
     *
     * @return
     */
    @GetMapping("/empCardAndBalance")
    @TrackLog
    public Mono<EmpCardAndBalanceResDTO> empCardAdnBalance(@RequestHeader(value = "encry-salt", required = false) String salt,
                                                           @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                                           @RequestHeader(value = "ent-id") String entId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /wallet/empCardAndBalance 查询当前企业下的钱包余额、银行卡数(去重) entId:{}, jsessionId:{}, salt:{}, passwd:{}", entId, jsessionId, salt, passwd);

            //查询当前登陆人信息
            EmployeeWechatDTO dto = findByJsessionId(jsessionId, currentUser.getName());
            EmpCardAndBalanceResDTO resDTO = walletService.empCardAdnBalance(entId, salt, passwd, dto);

            //查询认证信息
            Boolean isAttest = true;
            Integer signNumber = 0;
            EmployeeTaxAttestQueryReq attestQueryReq = EmployeeTaxAttestQueryReq.builder()
                    .idNumber(dto.getIdNumber())
                    .userName(dto.getName())
                    .phone(dto.getPhone())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxAttestDTO> attestDTOList = employeeTaxAttestFeignService.list(attestQueryReq);
            if (null != attestDTOList && attestDTOList.size() > 0) {
                EmployeeTaxAttestDTO employeeTaxAttestDTO = attestDTOList.get(0);
                String empTaxAttestId = employeeTaxAttestDTO.getId();
                isAttest = AttestStatusEnum.SUCCESS == employeeTaxAttestDTO.getAttestStatus() || AttestStatusEnum.ING == employeeTaxAttestDTO.getAttestStatus();

                //未签约协议数
                EmployeeTaxSigningQueryReq signingQueryReq = EmployeeTaxSigningQueryReq.builder()
                        .entId(entId)
                        .empTaxAttestId(empTaxAttestId)
                        .signStatus(IsStatusEnum.NO)
                        .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                        .build();
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
                                .build();

                        //验证身份信息成功，进入签约
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
                                    //已签约
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
                            log.info("=====> 验证是否签约成功失败，walletH5Req：{}", JacksonUtil.objectToJson(walletH5Req));
                        }
                        signNumber = signNumber + 1;
                    }
                }
            }
            resDTO.setIsAttest(isAttest);
            resDTO.setSignNumber(signNumber);
            return resDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 提现台账分页列表
     *
     * @param entId
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/withdrawalLedgerPage")
    @TrackLog
    public Mono<PageDTO<WithdrawalLedgerPageRes>> withdrawalLedgerPage(@RequestHeader(value = "ent-id") String entId,
                                                                       @RequestHeader(value = "encry-salt", required = false) String salt,
                                                                       @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                                                       @RequestBody WithdrawalLedgerPageReq req) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        PageRequest pageRequest = WebContext.getPageRequest();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /wallet/withdrawalLedgerPage 提现台账分页列表 entId:{}, jsessionId:{}, salt:{}, passwd:{}, req:{}", entId, jsessionId, salt, passwd, JsonUtil.objectToJson(req));

            //查询当前登陆人信息
            EmployeeWechatDTO dto = findByJsessionId(jsessionId, currentUser.getName());
            PageDTO<WithdrawalLedgerPageRes> pageDTO = walletService.withdrawalLedgerPage(entId, dto, req, salt, passwd, pageRequest);
            log.info("=====> /wallet/withdrawalLedgerPage 提现台账分页列表 返回：{}", JsonUtil.objectToJson(pageDTO));
            return pageDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 提现台账详情
     *
     * @param entId
     * @param withdrawalLedgerId 提现台账ID
     * @return
     * @throws Exception
     */
    @GetMapping("/withdrawalLedgerDetail/{withdrawalLedgerId}")
    @TrackLog
    public Mono<WithdrawalLedgerDetailRes> withdrawalLedgerDetail(@RequestHeader(value = "ent-id") String entId,
                                                                  @RequestHeader(value = "encry-salt", required = false) String salt,
                                                                  @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                                                  @PathVariable(value = "withdrawalLedgerId") String withdrawalLedgerId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /wallet/withdrawalLedgerDetail/{} 提现台账详情 entId:{}, jsessionId:{}, salt:{}, passwd:{}", withdrawalLedgerId, entId, jsessionId, salt, passwd);

            //查询当前登陆人信息
            EmployeeWechatDTO dto = findByJsessionId(jsessionId, currentUser.getName());
            WithdrawalLedgerDetailRes res = walletService.withdrawalLedgerDetail(withdrawalLedgerId, entId, dto, salt, passwd);
            return res;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 提现进度详情
     *
     * @param entId
     * @param withdrawalLedgerId
     * @return
     * @throws Exception
     */
    @GetMapping("/withdrawalRecordDetail/{withdrawalLedgerId}")
    @TrackLog
    public Mono<WithdrawalRecordDetailRes> withdrawalRecordDetail(@RequestHeader(value = "ent-id") String entId,
                                                                  @RequestHeader(value = "encry-salt", required = false) String salt,
                                                                  @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                                                  @PathVariable(value = "withdrawalLedgerId") String withdrawalLedgerId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /wallet/withdrawalRecordDetail/{} 提现进度详情 entId:{}, jsessionId:{}, salt:{}, passwd:{}", withdrawalLedgerId, entId, jsessionId, salt, passwd);

            //查询当前登陆人信息
            EmployeeWechatDTO dto = findByJsessionId(jsessionId, currentUser.getName());
            WithdrawalRecordDetailRes res = walletService.withdrawalRecordDetail(withdrawalLedgerId, entId, dto, salt, passwd);
            return res;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 收款账户列表
     */
    @GetMapping("/employeeCardList")
    @TrackLog
    public Mono<List<EmployeeCardDTO>> employeeCardList(@RequestHeader(value = "encry-salt", required = false) String salt,
                                                        @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                                        @RequestHeader(value = "ent-id") String entId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /wallet/employeeCardList 收款账户列表 entId:{}, jsessionId:{}, salt:{}, passwd:{}", entId, jsessionId, salt, passwd);

            //查询当前登陆人信息
            EmployeeWechatDTO dto = findByJsessionId(jsessionId, currentUser.getName());
            List<EmployeeCardDTO> list = walletService.employeeCardList(entId, dto, salt, passwd);
            return list;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 确认提现
     *
     * @param entId
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/withdraw")
    @TrackLog
    public Mono<Void> withdraw(@RequestHeader(value = "ent-id") String entId,
                               @RequestBody WithdrawalReq req) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /wallet/withdraw 确认提现 entId:{}, jsessionId:{}, req:{}", entId, jsessionId, JsonUtil.objectToJson(req));

            //查询当前登陆人信息
            EmployeeWechatDTO dto = findByJsessionId(jsessionId, currentUser.getName());
            walletService.withdraw(entId, dto, req);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}

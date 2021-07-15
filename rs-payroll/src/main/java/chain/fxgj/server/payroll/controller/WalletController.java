package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.dto.PageDTO;
import chain.fxgj.server.payroll.dto.wallet.*;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.payroll.client.feign.WalletFeignController;
import chain.utils.commons.UUIDUtil;
import chain.utils.fxgj.constant.DictEnums.DelStatusEnum;
import chain.utils.fxgj.constant.DictEnums.TransDealStatusEnum;
import chain.utils.fxgj.constant.DictEnums.WithdrawalStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    WalletFeignController walletFeignController;
    @Autowired
    EmpWechatService empWechatService;

    /**
     * 钱包余额</p>
     *
     * @param entId
     * @return
     */
    @GetMapping("/balance")
    @TrackLog
    public Mono<WalletBalanceDTO> balance(@RequestHeader(value = "ent-id") String entId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal currentUser = WebContext.getCurrentUser();
//        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //TODO 文档生成

            return WalletBalanceDTO.builder()
                    .availableAmount(new BigDecimal("50000.00"))
                    .totalAmount(new BigDecimal("30000.00"))
                    .frozenAmount(new BigDecimal("20000.00"))
                    .build();
        }).subscribeOn(Schedulers.elastic());
    }


    /**
     * 查询
     * 1.员工银行卡</p>
     * 2.钱包余额</p>
     * 查询当前企业下的银行卡数，去重
     *
     * @return
     */
    @GetMapping("/empCardAndBalance")
    @TrackLog
    public Mono<chain.fxgj.server.payroll.dto.wallet.EmpCardAndBalanceResDTO> empCardAdnBalance(@RequestHeader(value = "encry-salt", required = false) String salt,
                                                                                                @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                                                                                @RequestHeader(value = "ent-id") String entId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal currentUser = WebContext.getCurrentUser();
//        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {

            //TODO 文档生成
//            BaseReqDTO baseReqDTO = BaseReqDTO.builder()
//                    .idNumber(currentUser.getIdNumber())
//                    .entId(entId)
//                    .build();
//            MDC.setContextMap(mdcContext);
//            EmpCardAndBalanceResDTO empCardResDTO = walletFeignController.empCardAndBalance(baseReqDTO);
//            empCardResDTO.setBalance(EncrytorUtils.encryptField(empCardResDTO.getBalance(), salt, passwd));
//            empCardResDTO.setPasswd(passwd);
//            empCardResDTO.setSalt(salt);
//
//            //查询完成之后,更新缓存中的entId
//            if (StringUtils.isNotBlank(jsessionId)) {
//                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
//                empWechatService.upWechatInfoDetail(jsessionId, entId, wechatInfoDetail);
//            }else {
//                log.info("empCardAndBalance未更新缓存中的entId");
//            }
//            return empCardResDTO;
            return chain.fxgj.server.payroll.dto.wallet.EmpCardAndBalanceResDTO.builder()
                    .availableAmount(new BigDecimal("30000.00"))
                    .balance(new BigDecimal("50000.00"))
                    .frozenAmount(new BigDecimal("20000.00"))
                    .cardNum(2)
                    .employeeWalletId(UUIDUtil.createUUID32())
                    .passwd(passwd)
                    .salt(salt)
                    .walletNumber("fxgj-000000001000000")
                    .withdrawStatus(WithdrawalStatusEnum.Await.getCode())
                    .withdrawStatusVal(WithdrawalStatusEnum.Await.getDesc())
                    .build();
        }).subscribeOn(Schedulers.elastic());
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
                                                                       @RequestBody WithdrawalLedgerPageReq req) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal currentUser = WebContext.getCurrentUser();
//        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {

            //TODO 文档生成
            List<WithdrawalLedgerPageRes> list = new ArrayList<>();
            list.add(WithdrawalLedgerPageRes.builder()
                    .account("10250000003208171")
                    .accountId(UUIDUtil.createUUID32())
                    .accountName("张三")
                    .accountStar("1025********8171")
                    .accountOpenBank("华夏银行")
                    .accountStatus(0)
                    .accountStatusVal("正常")
                    .bankClose(false)
                    .crtDateTime(System.currentTimeMillis())
                    .custName("张三")
                    .employeeCardNo("6230200013873745")
                    .entId(entId)
                    .fundDate(1)
                    .fundDateVal("1月份")
                    .fundType(0)
                    .fundTypeVal("工资")
                    .idNumber("370782199612200038")
                    .issueTime(System.currentTimeMillis())
                    .month(2)
                    .openBank("华夏银行")
                    .payDateTime(System.nanoTime())
                    .remark("这是备注......")
                    .transAmount(new BigDecimal("12000.00"))
                    .updDateTime(System.nanoTime())
                    .walletNumber("818010100100065002")
                    .withdrawalLedgerId(UUIDUtil.createUUID32())
                    .withdrawalStatus(WithdrawalStatusEnum.Await.getCode())
                    .withdrawalStatusVal(WithdrawalStatusEnum.Await.getDesc())
                    .year(2021)
                    .build());
            return new PageDTO<>(1, 1, 1, 1, true, true, list);
        }).subscribeOn(Schedulers.elastic());
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
                                                                  @PathVariable(value = "withdrawalLedgerId") String withdrawalLedgerId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal currentUser = WebContext.getCurrentUser();
//        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {

            //TODO 文档生成

            return WithdrawalLedgerDetailRes.builder()
                    .account("10250000003208171")
                    .accountId(UUIDUtil.createUUID32())
                    .accountName("张三")
                    .accountStar("1025********8171")
                    .accountOpenBank("华夏银行")
                    .crtDateTime(System.currentTimeMillis())
                    .custName("张三")
                    .employeeCardNo("6230200013873745")
                    .entId(entId)
                    .fundType(0)
                    .fundTypeVal("工资")
                    .fundDateVal("1月份")
                    .fundDate(0)
                    .idNumber("370782199612200038")
                    .issueTime(System.currentTimeMillis())
                    .month(2)
                    .openBank("华夏银行")
                    .payDateTime(System.nanoTime())
                    .remark("这是备注......")
                    .transAmount(new BigDecimal("12000.00"))
                    .updDateTime(System.nanoTime())
                    .withdrawalLedgerId(UUIDUtil.createUUID32())
                    .withdrawalStatus(WithdrawalStatusEnum.Await.getCode())
                    .withdrawalStatusVal(WithdrawalStatusEnum.Await.getDesc())
                    .year(2021)
                    .employeeCardStar("6230*******3745")
                    .entName("北京开科唯识技术有限公司")
                    .groupName("北京开科唯识技术有限公司武汉分公司")
                    .transNo(UUIDUtil.createUUID32())
                    .wageSheetName("开科唯识云中心1月工资")
                    .withdrawalLedgerId(UUIDUtil.createUUID32())
                    .withdrawalRecordLogId(UUIDUtil.createUUID32())
                    .build();
        }).subscribeOn(Schedulers.elastic());
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
                                                                  @PathVariable(value = "withdrawalLedgerId") String withdrawalLedgerId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal currentUser = WebContext.getCurrentUser();
//        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {

            //TODO 文档生成
            WithdrawalLedgerDetailRes withdrawalLedgerDetail = WithdrawalLedgerDetailRes.builder()
                    .account("10250000003208171")
                    .accountId(UUIDUtil.createUUID32())
                    .accountName("张三")
                    .accountStar("1025********8171")
                    .accountOpenBank("华夏银行")
                    .crtDateTime(System.currentTimeMillis())
                    .custName("张三")
                    .employeeCardNo("6230200013873745")
                    .entId(entId)
                    .fundType(0)
                    .fundTypeVal("工资")
                    .fundDateVal("1月份")
                    .fundDate(0)
                    .idNumber("370782199612200038")
                    .issueTime(System.currentTimeMillis())
                    .month(2)
                    .openBank("华夏银行")
                    .payDateTime(System.nanoTime())
                    .remark("这是备注......")
                    .transAmount(new BigDecimal("12000.00"))
                    .updDateTime(System.nanoTime())
                    .withdrawalLedgerId(UUIDUtil.createUUID32())
                    .withdrawalStatus(WithdrawalStatusEnum.Await.getCode())
                    .withdrawalStatusVal(WithdrawalStatusEnum.Await.getDesc())
                    .year(2021)
                    .employeeCardStar("6230*******3745")
                    .entName("北京开科唯识技术有限公司")
                    .groupName("北京开科唯识技术有限公司武汉分公司")
                    .transNo(UUIDUtil.createUUID32())
                    .wageSheetName("开科唯识云中心1月工资")
                    .withdrawalLedgerId(UUIDUtil.createUUID32())
                    .withdrawalRecordLogId(UUIDUtil.createUUID32())
                    .build();
            WithdrawalRecordLogDTO withdrawalRecordLog = WithdrawalRecordLogDTO.builder()
                    .applyDateTime(System.nanoTime())
                    .crtDateTime(System.currentTimeMillis())
                    .delStatus(DelStatusEnum.normal.getCode())
                    .delStatusVal(DelStatusEnum.normal.getDesc())
                    .employeeCardNo("6230200013873745")
                    .failDesc(null)
                    .openBank("华夏银行")
                    .payDateTime(System.nanoTime())
                    .predictDateTime(System.nanoTime())
                    .remark("这是备注")
                    .transAmount(new BigDecimal("20000.00"))
                    .transNo(UUIDUtil.createUUID32())
                    .transStatus(TransDealStatusEnum.ING.getCode())
                    .transStatusVal(TransDealStatusEnum.ING.getDesc())
                    .withdrawalLedgerId(UUIDUtil.createUUID32())
                    .withdrawalRecordLogId(UUIDUtil.createUUID32())
                    .updDateTime(System.nanoTime())
                    .build();
            return WithdrawalRecordDetailRes.builder()
                    .withdrawalLedgerDetail(withdrawalLedgerDetail)
                    .withdrawalRecordLog(withdrawalRecordLog)
                    .build();
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 收款账户列表
     */
    @GetMapping("/employeeCardList")
    @TrackLog
    public Mono<List<EmployeeCardDTO>> employeeCardList(@RequestHeader(value = "ent-id") String entId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal currentUser = WebContext.getCurrentUser();
//        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {

            //TODO 文档生成
            List<EmployeeCardDTO> list = new ArrayList<>();
            list.add(EmployeeCardDTO.builder()
                    .cardNo("6230200013873745")
                    .employeeCardId(UUIDUtil.createUUID32())
                    .issuerBankId("034049")
                    .issuerName("华夏银行")
                    .build());
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 确认提现
     */
    @PostMapping("/withdraw")
    @TrackLog
    public Mono<Void> withdraw(@RequestHeader(value = "ent-id") String entId,
                               @RequestBody WithdrawalReq req) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal currentUser = WebContext.getCurrentUser();
//        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {

            //TODO 文档生成

            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }
}

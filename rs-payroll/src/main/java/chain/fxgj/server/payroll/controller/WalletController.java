package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.PageDTO;
import chain.fxgj.server.payroll.dto.wallet.*;
import chain.fxgj.server.payroll.service.WalletService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.WechatFeignController;
import chain.utils.commons.JsonUtil;
import chain.utils.commons.UUIDUtil;
import core.dto.wechat.EmployeeWechatDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    /**
     * 获取登陆人信息
     *
     * @param jsessionId
     * @return
     */
    public EmployeeWechatDTO findByJsessionId(String jsessionId) {
        log.info("=====> 根据JsessionId查询用户信息 jsessionId:{}", jsessionId);
        EmployeeWechatDTO dto = wechatFeignController.findByJsessionId(jsessionId);
        if (null == dto) {
            throw new ParamsIllegalException(ErrorConstant.Error0001.format("登录人"));
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
            EmployeeWechatDTO dto = findByJsessionId(jsessionId);

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
            EmployeeWechatDTO dto = findByJsessionId(jsessionId);
            EmpCardAndBalanceResDTO resDTO = walletService.empCardAdnBalance(entId, salt, passwd, dto);
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
            EmployeeWechatDTO dto = findByJsessionId(jsessionId);
            PageDTO<WithdrawalLedgerPageRes> pageDTO = walletService.withdrawalLedgerPage(entId, dto, req, salt, passwd, pageRequest);
            log.info("=====> /wallet/withdrawalLedgerPage 提现台账分页列表 返回：{}",JsonUtil.objectToJson(pageDTO));
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
            EmployeeWechatDTO dto = findByJsessionId(jsessionId);
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
            EmployeeWechatDTO dto = findByJsessionId(jsessionId);
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
            EmployeeWechatDTO dto = findByJsessionId(jsessionId);
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
            log.info("=====> /wallet/withdraw 确认提现 entId:{}, jsessionId:{}, req:{}", entId, jsessionId,JsonUtil.objectToJson(req));

            //查询当前登陆人信息
            EmployeeWechatDTO dto = findByJsessionId(jsessionId);
            walletService.withdraw(entId, dto, req);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * 是否允许提现
     *
     * @param entId
     * @return
     * @throws Exception
     */
    @GetMapping("/isAllowWithdraw")
    @TrackLog
    public Mono<IsAllowWithdrawRes> isAllowWithdraw(@RequestHeader(value = "ent-id") String entId) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String jsessionId = currentUser.getSessionId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /wallet/isAllowWithdraw 是否允许提现 entId:{}, jsessionId:{}", entId, jsessionId);

            //查询当前登陆人信息
            EmployeeWechatDTO dto = findByJsessionId(jsessionId);

            IsAllowWithdrawRes res = walletService.isAllowWithdraw(entId, dto);
            return res;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

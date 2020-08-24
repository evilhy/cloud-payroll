package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.payroll.client.feign.WalletFeignController;
import core.dto.request.BaseReqDTO;
import core.dto.response.wallet.EmpCardAndBalanceResDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
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

    /**
     * 钱包余额</p>
     *
     * @param baseReqDTO
     * @return
     */
    @PostMapping("/balance")
    @TrackLog
    public Mono<BigDecimal> balance(@RequestBody BaseReqDTO baseReqDTO) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //暂时固定返回0
            return BigDecimal.ZERO;
        }).subscribeOn(Schedulers.elastic());
    }



    /**
     * 查询
     * 1.员工银行卡</p>
     * 2.钱包余额</p>
     *      查询当前企业下的银行卡数，去重
     *
     * @param baseReqDTO
     * @return
     */
    @GetMapping("/empCardAdnBalance")
    @TrackLog
    public Mono<EmpCardAndBalanceResDTO> empCardAdnBalance(@RequestHeader(value = "encry-salt", required = false) String salt,
                                                            @RequestHeader(value = "encry-passwd", required = false) String passwd,
                                                            @RequestBody BaseReqDTO baseReqDTO) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            EmpCardAndBalanceResDTO empCardResDTO = walletFeignController.empCardAndBalance(baseReqDTO);
            empCardResDTO.setBalance(EncrytorUtils.encryptField(empCardResDTO.getBalance(), salt, passwd));
            empCardResDTO.setPasswd(passwd);
            empCardResDTO.setSalt(salt);
            return empCardResDTO;
        }).subscribeOn(Schedulers.elastic());
    }





}

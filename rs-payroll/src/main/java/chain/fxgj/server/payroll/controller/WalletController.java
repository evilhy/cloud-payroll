package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.WalletFeignController;
import core.dto.request.BaseReqDTO;
import core.dto.response.wagesheet.WageSheetDTO;
import core.dto.response.wallet.EmpCardAndBalanceResDTO;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    EmpWechatService empWechatService;

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
            BaseReqDTO baseReqDTO = BaseReqDTO.builder()
                    .idNumber(currentUser.getIdNumber())
                    .entId(entId)
                    .build();
            MDC.setContextMap(mdcContext);
            EmpCardAndBalanceResDTO empCardResDTO = walletFeignController.empCardAndBalance(baseReqDTO);
            empCardResDTO.setBalance(EncrytorUtils.encryptField(empCardResDTO.getBalance(), salt, passwd));
            empCardResDTO.setPasswd(passwd);
            empCardResDTO.setSalt(salt);

            //查询完成之后,更新缓存中的entId
            if (StringUtils.isNotBlank(jsessionId)) {
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                empWechatService.upWechatInfoDetail(jsessionId, entId, wechatInfoDetail);
            }else {
                log.info("empCardAndBalance未更新缓存中的entId");
            }
            return empCardResDTO;
        }).subscribeOn(Schedulers.elastic());
    }





}

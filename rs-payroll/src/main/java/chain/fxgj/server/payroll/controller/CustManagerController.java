package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.feign.client.CustManagerFeignService;
import chain.fxgj.feign.dto.custmanager.WageManagerInfoDTO;
import chain.fxgj.server.payroll.dto.response.ManagerInfoDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.wage.manager.core.dto.dataquery.OpeningTipsDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * 客户经理
 */
@RestController
@Validated
@RequestMapping("/manager")
@Slf4j
public class CustManagerController {
    @Autowired
    CustManagerFeignService custManagerFeignService;

    /**
     * 查询客户经理信息
     *
     * @return
     */
    @GetMapping("/managerInfo")
    @TrackLog
    public Mono<ManagerInfoDTO> sendCode() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal currentUser = WebContext.getCurrentUser();
        String idNumber = currentUser.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.debug("==>身份证号:{}", idNumber);
            WageManagerInfoDTO wageManagerInfoDTO = custManagerFeignService.sendCode(idNumber);
            ManagerInfoDTO managerInfoDTO = new ManagerInfoDTO();
            if (null != wageManagerInfoDTO) {
                BeanUtils.copyProperties(wageManagerInfoDTO, managerInfoDTO);
            }
            return managerInfoDTO;
        }).subscribeOn(Schedulers.elastic());

    }

    /**
     * 通知企业分配客户经理
     */
    @PostMapping("/distribute")
    @TrackLog
    public Mono<Void> distribute() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String entId = currentUser.getEntId();
        log.info("entId:[{}]", entId);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            custManagerFeignService.distribute(entId);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     *
     * 客户经理入口，弹窗提示
     *
     * 查询员工所属企业，
     * 1.是否有客户经理
     * 2.银行卡所属银行，本行、他行
     *
     * @return
     */
    @GetMapping("/openingTips")
    public OpeningTipsDTO openingTips() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String idNumber = currentUser.getIdNumber();
        log.info("openingTips.idNumber:[{}]", idNumber);
        OpeningTipsDTO openingTipsDTO = custManagerFeignService.openingTips(idNumber);
        return openingTipsDTO;
    }

}

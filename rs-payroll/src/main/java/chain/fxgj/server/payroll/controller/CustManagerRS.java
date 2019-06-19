package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.service.ManagerService;
import chain.fxgj.server.payroll.dto.request.DistributeDTO;
import chain.fxgj.server.payroll.dto.response.ManagerInfoDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * 客户经理
 */
@CrossOrigin
@RestController
@Validated
@RequestMapping("/manager")
@Slf4j
public class CustManagerRS {
    @Autowired
    private ManagerService managerService;

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
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            String idNumber = currentUser.getIdNumber();
            log.debug("==>身份证号:{}", idNumber);
            ManagerInfoDTO managerInfoDTO = managerService.managerInfoByIdNumber(idNumber);
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
        log.info("entId:[{}]",entId);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            managerService.noticEnterprise(entId);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }
}

package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.data.client.EmployeeBillFeign;
import chain.fxgj.data.dto.bill.EmployeeBillDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * @author syd
 * @description 账单
 * @date 2021/1/6.
 */
@RestController
@RequestMapping("/bill")
@Slf4j
public class BillController {
    @Autowired
    EmployeeBillFeign employeeBillFeign;

    /**
     * 查询客户经理信息
     *
     * @return
     */
    @GetMapping
    @TrackLog
    public Mono<EmployeeBillDTO> bill() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String idNumber = currentUser.getIdNumber();
        String entId = currentUser.getEntId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("idNumber:{},entId:{}", idNumber, entId);
            EmployeeBillDTO billDTO = employeeBillFeign.employeeYearBill(entId, idNumber);
            log.info("-->[{}]", JacksonUtil.objectToJson(billDTO));
            return billDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }


}
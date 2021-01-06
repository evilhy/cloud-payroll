package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.data.client.EmployeeBillFeign;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
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
//        UserPrincipal currentUser = WebContext.getCurrentUser();
//        String idNumber = currentUser.getIdNumber();
//        String entId = currentUser.getEntId();
//        log.info("idNumber:{},entId:{}", idNumber, entId);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            EmployeeBillDTO dto = new EmployeeBillDTO();
            dto.setDifferDays(367);
            dto.setIdNumber("421120198512136895");
            dto.setEmployeeName("刘寒冰");
            dto.setDifferAmount("10000.00");
            dto.setFirstTierCitiesAvgAmount("9.9");
            dto.setSecondTierCitiesAvgAmount("6.8");
            dto.setIndustryAvgAmount("10.3");
            dto.setMaxAmount("202010");
            dto.setMaxMonth("9076.36");
            dto.setMaxSingleAmount("19076.36");
            dto.setMaxSingleAmountDate("20201015");
            dto.setMinAmount("1500");
            dto.setMinMonth("202003");
            dto.setMonthCount(8);
            dto.setPercent("20.5");
            dto.setPushTimes(7);
            dto.setTotalAmount("69009.60");
            List<EmployeeBillDTO.FundInfo> list = new ArrayList<>();
            EmployeeBillDTO.FundInfo info = new EmployeeBillDTO.FundInfo();
            info.setFundType("工资");
            info.setFundAmount("50230.1");
            info.setFundPercent("82.5");

            EmployeeBillDTO.FundInfo info0 = new EmployeeBillDTO.FundInfo();
            info0.setFundType("报销");
            info0.setFundAmount("0");
            info0.setFundPercent("0");

            EmployeeBillDTO.FundInfo info1 = new EmployeeBillDTO.FundInfo();
            info1.setFundType("补贴");
            info1.setFundAmount("10000");
            info1.setFundPercent("16.5");

            EmployeeBillDTO.FundInfo info2 = new EmployeeBillDTO.FundInfo();
            info2.setFundType("其他");
            info2.setFundAmount("93");
            info2.setFundPercent("1");
            list.add(info);
            list.add(info0);
            list.add(info1);
            list.add(info2);
            dto.setFundWages(list);
            return dto;
//            return employeeBillFeign.employeeYearBill(entId, idNumber);
        }).subscribeOn(Schedulers.elastic());
    }
}

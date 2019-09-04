package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.service.SynDataTimerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@Validated
@RequestMapping(value = "/synctimer")
@Slf4j
public class SynTimerRS {

    @Autowired
    private SynDataTimerService synDataTimerService;

    /**
     * 同步工资详情,包括 wage_detail_info,wage_sheet_info,wage_show_info
     * @param date
     * @return
     */
    @GetMapping(value = {"/wagedata/{date}","/wagedata"})
    @TrackLog
    public Mono<Integer> synWageDataInfo(@PathVariable(value = "date",required = false) String date) {
        return Mono.fromCallable(() -> {
            Integer count = synDataTimerService.synWageDataInfo(date);
            return count;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 同步用户数据，包括employee,employee_wechat_info
     * @param date
     * @return
     */
    @GetMapping(value = {"/empdata/{date}","/empdata"})
    @TrackLog
    public Mono<Integer> synEmpInfoDataInfo(@PathVariable(value = "date",required = false) String date) {
        return Mono.fromCallable(() -> {
            Integer count = synDataTimerService.synEmpInfoDataInfo(date);
            return count;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 同步企业，机构信息，包括 ent_erprise_info,ent_group_info
     * @param date
     * @return
     */
    @GetMapping(value = {"/entgroupdata/{date}","/entgroupdata"})
    @TrackLog
    public Mono<Integer> synEntGroupDataInfo(@PathVariable(value = "date",required = false) String date) {
        return Mono.fromCallable(() -> {
            Integer count = synDataTimerService.synEntGroupDataInfo(date);
            return count;
        }).subscribeOn(Schedulers.elastic());
    }

}

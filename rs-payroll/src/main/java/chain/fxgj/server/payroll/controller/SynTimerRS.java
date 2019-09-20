package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.feign.client.SynTimerFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(value = "/unAuth/synctimer")
@Slf4j
public class SynTimerRS {

    //@Autowired
    //private SynDataTimerService synDataTimerService;
    @Autowired
    private SynTimerFeignService synTimerFeignService;

    /**
     * 同步工资详情,包括 wage_detail_info,wage_sheet_info,wage_show_info
     * @param date
     * @return
     */
    @GetMapping(value = {"/wagedata/{date}","/wagedata"})
    @TrackLog
    public void synWageDataInfo(@PathVariable(value = "date",required = false) String date) {
        long starTime=System.currentTimeMillis();
        Integer count=synTimerFeignService.wagedata(date);
        log.info("wagedata-->{},耗时:{}",count,System.currentTimeMillis()-starTime);
    }

    /**
     * 同步用户数据，包括employee,employee_wechat_info
     * @param date
     * @return
     */
    @GetMapping(value = {"/empdata/{date}","/empdata"})
    @TrackLog
    public void synEmpInfoDataInfo(@PathVariable(value = "date",required = false) String date) {
        long starTime=System.currentTimeMillis();
        Integer count=synTimerFeignService.empdata(date);
        log.info("empdata-->{},耗时:{}",count,System.currentTimeMillis()-starTime);
    }

    /**
     * 同步企业，机构信息，包括 ent_erprise_info,ent_group_info
     * @param date
     * @return
     */
    @GetMapping(value = {"/entgroupdata/{date}","/entgroupdata"})
    @TrackLog
    public void synEntGroupDataInfo(@PathVariable(value = "date",required = false) String date) {
        long starTime=System.currentTimeMillis();
        Integer count=synTimerFeignService.entgroupdata(date);
        log.info("entgroupdata-->{},耗时:{}",count,System.currentTimeMillis()-starTime);
    }

}

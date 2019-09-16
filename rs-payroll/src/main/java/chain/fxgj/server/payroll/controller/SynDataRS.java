package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.service.SynDataService;
import chain.fxgj.feign.client.SynDataFeignService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated

@RequestMapping(value = "/unAuth/sync")
@Slf4j
@SuppressWarnings("unchecked")
public class SynDataRS {


    //@Autowired
   // private SynDataService synDataService;
    @Autowired
    private SynDataFeignService synDataFeignService;


    @GetMapping("/wagedetail")
    @TrackLog
    public void wagedetail(String date) {
        log.info("wagedetail开始同步....");
        long starTime=System.currentTimeMillis();
        if (StringUtils.isEmpty(date)){
            throw new RuntimeException("同步年份不能为空");
        }
        //Integer count=synDataService.wagedetail(date);
        Integer count=synDataFeignService.wagedetail(date);
        log.info("wagedetail:{},耗时{}",count,System.currentTimeMillis()-starTime);
    }

    @GetMapping("/empinfo")
    @TrackLog
    public void empinfo(String date) {
        log.info("empinfo开始同步....");
        long starTime=System.currentTimeMillis();
        //Integer count = synDataService.empinfo(date);
        Integer count=synDataFeignService.empinfo(date);
        log.info("empinfo:{},耗时{}",count,System.currentTimeMillis()-starTime);
    }

    @GetMapping("/empwetchat")
    @TrackLog
    public void empwetchat() {
        log.info("empwetchat开始同步....");
        long starTime=System.currentTimeMillis();
        //Integer count = synDataService.empwetchat();
        Integer count = synDataFeignService.empwetchat();
        log.info("empwetchat-->{},耗时:{}",count,System.currentTimeMillis()-starTime);
    }


    @GetMapping("/enterprise")
    @TrackLog
    public void enterprise() {
        log.info("enterprise开始同步....");
        long starTime=System.currentTimeMillis();
        //Integer count = synDataService.enterprise();
        Integer count = synDataFeignService.enterprise();
        log.info("enterprise-->{},耗时:{}",count,System.currentTimeMillis()-starTime);
    }

    @GetMapping("/entgroup")
    @TrackLog
    public void entgroup() {
        log.info("entgroup开始同步....");
        long starTime=System.currentTimeMillis();
        //Integer count = synDataService.entgroup();
        Integer count = synDataFeignService.entgroup();
        log.info("entgroup-->{},耗时:{}",count,System.currentTimeMillis()-starTime);
    }

    @GetMapping("/manager")
    @TrackLog
    public void manager() {
        log.info("manager开始同步....");
        long starTime=System.currentTimeMillis();
        //Integer count = synDataService.manager();
        Integer count = synDataFeignService.manager();
        log.info("manager-->{},耗时:{}",count,System.currentTimeMillis()-starTime);
    }

    @GetMapping("/wagesheet")
    @TrackLog
    public void sheet() {
        log.info("sheet开始同步....");
        long starTime=System.currentTimeMillis();
       // Integer count = synDataService.wageSheet();
        Integer count = synDataFeignService.wagesheet();
        log.info("wagesheet-->{},耗时:{}",count,System.currentTimeMillis()-starTime);
    }

    @GetMapping("/wageshow")
    @TrackLog
    public void wageShow() {
        log.info("wageShow开始同步....");
        long starTime=System.currentTimeMillis();
       // Integer count = synDataService.wageShow();
        Integer count = synDataFeignService.wageshow();
        log.info("wageShow-->{},耗时:{}",count,System.currentTimeMillis()-starTime);
    }






}

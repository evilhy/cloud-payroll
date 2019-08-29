package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.service.SynDataService;
import chain.fxgj.server.payroll.dto.advertising.AdvertisingRotationDTO;
import chain.fxgj.server.payroll.dto.response.IndexDTO;
import chain.fxgj.server.payroll.dto.response.NewestWageLogDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/sync")
@Slf4j
@SuppressWarnings("unchecked")
public class SynDataRS {


    @Autowired
    private SynDataService synDataService;


    @GetMapping("/wagedetail")
    @TrackLog
    public Mono<Integer> wagedetail(String date) {
        return Mono.fromCallable(() -> {
            //MDC.setContextMap(mdcContext);
            Integer count = synDataService.wagedetail(date);
            return count;
        }).subscribeOn(Schedulers.elastic());
    }

    @GetMapping("/empinfo")
    @TrackLog
    public Mono<Integer> empinfo() {
        return Mono.fromCallable(() -> {
            Integer count = synDataService.empinfo();
            return count;
        }).subscribeOn(Schedulers.elastic());
    }

    @GetMapping("/empwetchat")
    @TrackLog
    public Mono<Integer> empwetchat() {
        return Mono.fromCallable(() -> {
            Integer count = synDataService.empwetchat();
            return count;
        }).subscribeOn(Schedulers.elastic());
    }


    @GetMapping("/enterprise")
    @TrackLog
    public Mono<Integer> enterprise() {
        return Mono.fromCallable(() -> {
            Integer count = synDataService.enterprise();
            return count;
        }).subscribeOn(Schedulers.elastic());
    }

    @GetMapping("/entgroup")
    @TrackLog
    public Mono<Integer> entgroup() {
        return Mono.fromCallable(() -> {
            Integer count = synDataService.entgroup();
            return count;
        }).subscribeOn(Schedulers.elastic());
    }

    @GetMapping("/manager")
    @TrackLog
    public Mono<Integer> manager() {
        return Mono.fromCallable(() -> {
            Integer count = synDataService.manager();
            return count;
        }).subscribeOn(Schedulers.elastic());
    }






}

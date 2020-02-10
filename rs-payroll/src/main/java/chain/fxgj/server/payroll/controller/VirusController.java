package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.payroll.client.feign.VirusFeignService;
import chain.payroll.dto.PageDTO;
import chain.payroll.dto.request.virus.VirusRequestDto;
import chain.payroll.dto.response.virus.NcpVirusPromiseListDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.ws.rs.DefaultValue;
import java.util.Map;

/**
 * @author flash
 * @date 2020/2/10 10:02
 */
@RestController
@Validated
@RequestMapping(value = "/virus")
@Slf4j
public class VirusController {

    @Autowired
    VirusFeignService virusFeignService;

    /**
     * 查询列表
     * @param pageNum
     * @param size
     * @return
     */
    @GetMapping
    @TrackLog
    public Mono<PageDTO<NcpVirusPromiseListDto>> list(@RequestHeader("page-num") @DefaultValue("1") int pageNum,
                                                      @RequestHeader("limit") @DefaultValue("20") int size,
                                                      @RequestParam String openId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return virusFeignService.list(pageNum,size,openId);
        }).subscribeOn(Schedulers.elastic());
    }

    @PostMapping
    @TrackLog
    public Mono<Long> post(@RequestBody VirusRequestDto virusRequestDto){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return virusFeignService.post(virusRequestDto);
        }).subscribeOn(Schedulers.elastic());
    }

}

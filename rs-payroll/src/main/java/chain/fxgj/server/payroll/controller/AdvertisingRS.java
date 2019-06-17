package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.service.AdvertisementService;
import chain.fxgj.server.payroll.dto.advertising.AdvertisingRotationDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Map;


@RestController
@Validated
@RequestMapping(value = "/advertising")
@Slf4j
public class AdvertisingRS {

    @Autowired
    AdvertisementService advertisementService;

//    /**
//     * 轮播图查询
//     */
//    @GetMapping("/rotation")
//    @TrackLog
//    @PermitAll
//    public Mono<List<AdvertisingRotationDTO>> rotation(@RequestParam("channelId") Integer channelId) {
//        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//
//        return Mono.fromCallable(() -> {
//            MDC.setContextMap(mdcContext);
//            log.info("channelId:[{}](0放薪管家web,1放薪经理,2微信工资条,3放薪虎符)", channelId);
//            List<AdvertisingRotationDTO> advertisingRotationDTOS = advertisementService.rotation(channelId);
//            return advertisingRotationDTOS;
//        }).subscribeOn(Schedulers.elastic());
//    }

    /**
     * 轮播图查询
     */
    @GetMapping("/rotation")
    @TrackLog
    @PermitAll
    public Mono<List<AdvertisingRotationDTO>> rotation(@RequestParam("channelId") Integer channelId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("channelId:[{}](0放薪管家web,1放薪经理,2微信工资条,3放薪虎符)", channelId);
            log.info("entId:[{}]", principal.getEntId());
            List<AdvertisingRotationDTO> advertisingRotationDTOS = advertisementService.rotation(channelId, principal.getEntId());
            return advertisingRotationDTOS;
        }).subscribeOn(Schedulers.elastic());
    }
}

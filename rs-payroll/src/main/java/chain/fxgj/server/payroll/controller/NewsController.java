package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.news.dto.PageDTO;
import chain.news.dto.news.*;
import chain.news.server.service.NewsNoticeServiceFeign;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author flash
 * @date 2020/8/24 10:02
 */
@RestController
@Validated
@RequestMapping(value = "/news")
@Slf4j
public class NewsController {

    @Autowired
    NewsNoticeServiceFeign newsNoticeServiceFeign;

    /**
     * 首页滚动展示信息
     * @param req
     * @return
     */
    @PostMapping("/bulletInfo")
    @TrackLog
    public Mono<NewsBulletInfoDto> bulletInfo(@RequestBody NewsInfoReq req){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return NewsBulletInfoDto.builder().build();
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 各类消息类型未读个数统计
     * @param req
     * @return
     */
    @PostMapping("/statisticInfo")
    @TrackLog
    public Mono<List<NewsStatisticInfoDto>> statisticInfo(@RequestBody NewsInfoReq req){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            List<NewsStatisticInfoDto> dtos = new ArrayList<>();
            return dtos;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 分页查询消息列表
     * @param req
     * @return
     */
    @PostMapping
    @TrackLog
    public Mono<PageDTO<NewsInfoDto>> pageList(@RequestBody NewsInfoReq req){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return new PageDTO<NewsInfoDto>();
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 操作消息
     * @param req
     * @return
     */
    @PostMapping("/operate")
    @TrackLog
    public Mono<Void> operate(@RequestBody NewsOperateInfoReq req){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }


}

package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.payroll.client.feign.RegionFeignService;
import chain.utils.fxgj.constant.DictEnums.RegionLevelEnum;
import core.dto.response.region.RegionDictionaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description:地区 控制层
 * @Author: du
 * @Date: 2020/12/3 16:48
 */
@RequestMapping("/region")
@RestController
@Slf4j
public class RegionController {

    @Autowired
    RegionFeignService regionFeignService;

    /**
     * 地区数据字典
     *
     * @param regionParentCode 地区父ID（不传默认查询所有:省、自治区、直辖市）
     * @return
     */
    @GetMapping("/dictionary")
    @TrackLog
    public Mono<List<chain.fxgj.server.payroll.dto.region.RegionDictionaryDTO>> dictionary(@RequestParam(value = "regionParentCode", required = false) String regionParentCode) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            log.info("=====> /region/dictionary 地区数据字典 regionParentCode：{} ", regionParentCode);

//            return regionFeignService.dictionaryRegion(regionParentCode);
            List<chain.fxgj.server.payroll.dto.region.RegionDictionaryDTO> list = new ArrayList<>();
            list.add(chain.fxgj.server.payroll.dto.region.RegionDictionaryDTO.builder()
                    .latitude("N30°33′18.32″")
                    .provinceCityCode("420106")
                    .regionCode("430061")
                    .longitude("E114°20′8.37")
                    .regionLevel(RegionLevelEnum.SECOND.getCode())
                    .regionLevelVal(RegionLevelEnum.SECOND.getDesc())
                    .regionName("武昌区")
                    .regionParentCode("420100000000")
                    .regionParentName("武汉市")
                    .build());
            return list;
        }).subscribeOn(Schedulers.elastic());
    }
}

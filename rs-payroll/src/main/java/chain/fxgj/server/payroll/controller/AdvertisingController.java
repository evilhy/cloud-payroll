package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import chain.fxgj.feign.client.AdvertisingFeignService;
import chain.fxgj.feign.dto.advertising.WageAdvertisingRotationDTO;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.advertising.AdvertisingRotationDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@Validated
@RequestMapping(value = "/advertising")
@Slf4j
public class AdvertisingController {

    @Autowired
    AdvertisingFeignService advertisingFeignService;

    /**
     * 轮播图查询
     */
    @GetMapping("/rotation")
    @TrackLog
    @PermitAll
    public Mono<List<AdvertisingRotationDTO>> rotation(@RequestParam("channelId") Integer channelId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        //根据合作方获取一行渠道
        String apppartner = MDC.get(PayrollConstants.APPPARTNER);
        AppPartnerEnum appPartner = AppPartnerEnum.values()[Integer.valueOf(apppartner)];
        FundLiquidationEnum liquidation = appPartner.getLiquidation();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("channelId:[{}](0放薪管家web,1放薪经理,2微信工资条,3放薪虎符)", channelId);
            log.info("fundLiquidationEnum:[{}]", liquidation.toString());
            List<AdvertisingRotationDTO> advertisingRotationDTOS = new ArrayList<>();
            List<WageAdvertisingRotationDTO> rotation = advertisingFeignService.rotation(channelId, liquidation);
            if (null != rotation && rotation.size() > 0) {
                for (WageAdvertisingRotationDTO wageAdvertisingRotationDTO : rotation) {
                    AdvertisingRotationDTO advertisingRotationDTO = new AdvertisingRotationDTO();
                    BeanUtils.copyProperties(wageAdvertisingRotationDTO, advertisingRotationDTO);
                    advertisingRotationDTOS.add(advertisingRotationDTO);
                }
            }
            return advertisingRotationDTOS;
        }).subscribeOn(Schedulers.elastic());
    }
}

package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.feign.client.AdvertisingFeignService;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.advertising.AdvertisingRotationDTO;
import chain.news.dto.adv.AdsAdvInfoRes;
import chain.news.dto.adv.QueryAdsReq;
import chain.news.server.service.AdvServiceFegin;
import chain.utils.fxgj.constant.DictEnums.AppPartnerEnum;
import chain.utils.fxgj.constant.DictEnums.FundLiquidationEnum;
import chain.utils.fxgj.constant.DictEnums.VersionsEnum;
import chain.utils.fxgj.constant.DictEnums.VersionsTypeEnum;
import chain.utils.news.constant.DictEnums.SysMsgTypeEnum;
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

    @Autowired
    AdvServiceFegin advServiceFegin;

//    /**
//     * 轮播图查询
//     */
//    @GetMapping("/rotation")
//    @TrackLog
//    @PermitAll
//    public Mono<List<AdvertisingRotationDTO>> rotation(@RequestParam("channelId") Integer channelId) {
//        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        //根据合作方获取一行渠道
//        String apppartner = MDC.get(PayrollConstants.APPPARTNER);
//        log.info("rotation.apppartner:[{}]", apppartner);
//        AppPartnerEnum appPartner = AppPartnerEnum.values()[Integer.valueOf(apppartner)];
//        FundLiquidationEnum liquidation = appPartner.getLiquidation();
//
//        return Mono.fromCallable(() -> {
//            MDC.setContextMap(mdcContext);
//            log.info("channelId:[{}](0放薪管家web,1放薪经理,2微信工资条,3放薪虎符)", channelId);
//            log.info("fundLiquidationEnum:[{}]", liquidation.toString());
//            List<AdvertisingRotationDTO> advertisingRotationDTOS = new ArrayList<>();
//            List<WageAdvertisingRotationDTO> rotation = advertisingFeignService.rotation(channelId, liquidation);
//            if (null != rotation && rotation.size() > 0) {
//                for (WageAdvertisingRotationDTO wageAdvertisingRotationDTO : rotation) {
//                    AdvertisingRotationDTO advertisingRotationDTO = new AdvertisingRotationDTO();
//                    BeanUtils.copyProperties(wageAdvertisingRotationDTO, advertisingRotationDTO);
//                    advertisingRotationDTOS.add(advertisingRotationDTO);
//                }
//            }
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
        //根据合作方获取一行渠道
        String apppartner = MDC.get(PayrollConstants.APPPARTNER);
        log.info("rotation.apppartner:[{}]", apppartner);
        AppPartnerEnum appPartner = AppPartnerEnum.values()[Integer.valueOf(apppartner)];
        FundLiquidationEnum liquidation = appPartner.getLiquidation();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("channelId:[{}](0放薪管家web,1放薪经理,2微信工资条,3放薪虎符)", channelId);
            log.info("fundLiquidationEnum:[{}]", liquidation.toString());
            List<AdvertisingRotationDTO> advertisingRotationDTOS = new ArrayList<>();

            /** 目前仅支持：普通版——普通版 **/
            VersionsTypeEnum versionsTypeEnum = VersionsTypeEnum.NORMAL;
            VersionsEnum versionsEnum = VersionsEnum.NORMAL;

            log.info("fundLiquidationEnum:[{}]", liquidation.getDesc());
            log.info("versionsTypeEnum:[{}]", versionsTypeEnum.getDesc());
            log.info("versionsEnum:[{}]", versionsEnum.getDesc());

            //渠道
            String[] channels = new String[1];
            channels[0] = String.valueOf(channelId);
            //银行
            String[] liquidations = new String[1];
            liquidations[0] = String.valueOf(liquidation.getNum().toString());
            //父版本
            String[] versions = new String[1];
            versions[0] = String.valueOf(versionsTypeEnum.getNum().toString());
            //子版本
            String[] subVersions = new String[1];
            subVersions[0] = String.valueOf(versionsEnum.getNum().toString());

            QueryAdsReq queryAdsReq = QueryAdsReq.builder()
                    .liquidation(liquidations)
                    .versions(versions)
                    .subVersion(subVersions)
                    .channels(channels)
                    .type(SysMsgTypeEnum.rot)
                    .status(0)
                    .build();
            List<AdsAdvInfoRes> advInfoRes = advServiceFegin.query(queryAdsReq);
            if (null != advInfoRes && advInfoRes.size() > 0) {
                for (AdsAdvInfoRes adsAdvInfoRes : advInfoRes) {

                    String link = null;
                    String url = null;
                    if (null != adsAdvInfoRes.getAccessUrls() && adsAdvInfoRes.getAccessUrls().size() > 0) {
                        for (AdsAdvInfoRes.AccessUrl accessUrl : adsAdvInfoRes.getAccessUrls()
                        ) {
                            link = accessUrl.getLink();
                            url = accessUrl.getUrl();
                        }
                    }

                    AdvertisingRotationDTO advertisingRotationDTO = AdvertisingRotationDTO.builder()
                            .link(link)
                            .releaseStatus(adsAdvInfoRes.getStatus())
                            .releaseStatusDesc(0 == adsAdvInfoRes.getStatus() ? "已发布" : (1 == adsAdvInfoRes.getStatus() ? "未发布" : "已下架"))
                            .sortNo(adsAdvInfoRes.getSortNo())
                            .url(url)
                            .build();
                    advertisingRotationDTOS.add(advertisingRotationDTO);
                }
            }
            return advertisingRotationDTOS;
        }).subscribeOn(Schedulers.elastic());
    }
}

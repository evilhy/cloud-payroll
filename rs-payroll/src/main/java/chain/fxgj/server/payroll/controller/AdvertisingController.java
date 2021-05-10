package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.feign.hxinside.ent.service.EntErpriseInfoServiceFeign;
import chain.fxgj.ent.core.dto.request.EntErpriseQueryRequest;
import chain.fxgj.ent.core.dto.response.EntErpriseInfoRes;
import chain.fxgj.server.payroll.dto.advertising.AdvertisingRotationDTO;
import chain.news.dto.adv.AdsAdvInfoRes;
import chain.news.dto.adv.QueryAdsReq;
import chain.news.server.service.AdvServiceFegin;
import chain.utils.commons.JacksonUtil;
import chain.utils.fxgj.constant.DictEnums.EnterpriseStatusEnum;
import chain.utils.fxgj.constant.DictEnums.FundLiquidationEnum;
import chain.utils.fxgj.constant.DictEnums.VersionsEnum;
import chain.utils.fxgj.constant.DictEnums.VersionsTypeEnum;
import chain.utils.news.constant.DictEnums.SysMsgTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.util.*;


@RestController
@Validated
@RequestMapping(value = "/advertising")
@Slf4j
public class AdvertisingController {

    @Autowired
    AdvServiceFegin advServiceFegin;

    @Autowired
    EntErpriseInfoServiceFeign entErpriseInfoServiceFeign;

    /**
     * 轮播图查询
     */
    @GetMapping("/rotation")
    @TrackLog
    @PermitAll
    public Mono<List<AdvertisingRotationDTO>> rotation(@RequestHeader("ent-id") String entId,
                                                       @RequestParam("channelId") Integer channelId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        //根据合作方获取一行渠道
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<AdvertisingRotationDTO> advertisingRotationDTOS = new ArrayList<>();

            log.info("=====> /advertising/rotation 轮播图查询  entId:[{}]", entId);
            //查询企业信息
            EntErpriseQueryRequest erpriseQueryRequest = EntErpriseQueryRequest.builder()
                    .entId(entId)
                    .entStatus(new LinkedList(Arrays.asList(EnterpriseStatusEnum.NORMAL)))
                    .build();
            EntErpriseInfoRes erpriseInfoRes = entErpriseInfoServiceFeign.find(erpriseQueryRequest);
            if (null == erpriseInfoRes) {
                return advertisingRotationDTOS;
            }

            log.info("channelId:[{}](0放薪管家web,1放薪经理,2微信工资条,3放薪虎符)", channelId);

            /** 目前仅支持：普通版——普通版 **/
            FundLiquidationEnum liquidation = erpriseInfoRes.getLiquidation();
            VersionsTypeEnum versionsTypeEnum = erpriseInfoRes.getVersion();
            VersionsEnum versionsEnum = erpriseInfoRes.getSubVersion();

            log.info("fundLiquidationEnum:[{}]", liquidation.getDesc());
            log.info("versionsTypeEnum:[{}]", versionsTypeEnum.getDesc());
            log.info("versionsEnum:[{}]", versionsEnum.getDesc());

            //宁夏银行-新增校验:[仅华夏银行继续查询，其他银行统一返回空数组]
            if (!FundLiquidationEnum.HXB.getFundCode().equals(liquidation)) {
                return advertisingRotationDTOS;
            }

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
            log.info("============================================================> 企业ID：{}，企业名称：{}，企业渠道：{}，企业版本：{}，企业子版本：{}", erpriseInfoRes.getEntId(), erpriseInfoRes.getEntName(), liquidation.getDesc(), versionsTypeEnum.getDesc(), versionsEnum.getDesc());
            log.info("============================================================> advertisingRotationDTOS：{}", JacksonUtil.objectToJson(advertisingRotationDTOS));
            return advertisingRotationDTOS;
        }).subscribeOn(Schedulers.elastic());
    }
}

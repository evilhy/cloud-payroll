package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.constant.DictEnums.ChannelIdEnum;
import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.ReleaseStatusEnum;
import chain.fxgj.core.common.service.AdvertisementService;
import chain.fxgj.core.jpa.dao.AdvertisingInfoDao;
import chain.fxgj.core.jpa.model.AdvertisingInfo;
import chain.fxgj.core.jpa.model.QAdvertisingInfo;
import chain.fxgj.server.payroll.dto.advertising.AdvertisingRotationDTO;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    @Autowired
    AdvertisingInfoDao advertisingInfoDao;

    /**
     * 轮播图查询</p>
     * 根据渠道id、删除状态(正常)、发布状态(已发布)</p>
     *
     * @param channelId
     * @return
     */
    @Override
    public List<AdvertisingRotationDTO> rotation(int channelId) {
        QAdvertisingInfo qAdvertisingInfo = QAdvertisingInfo.advertisingInfo;
        BooleanExpression booleanExpression = qAdvertisingInfo.delStatus.eq(DelStatusEnum.normal)
                .and(qAdvertisingInfo.releaseStatus.eq(ReleaseStatusEnum.PUBLISHED))
                .and(qAdvertisingInfo.channelId.eq(ChannelIdEnum.values()[channelId]));
        log.info("channelId:[{}]",channelId);
        List<AdvertisingInfo> fetch = advertisingInfoDao.select(qAdvertisingInfo)
                .from(qAdvertisingInfo)
                .where(booleanExpression)
                .orderBy(qAdvertisingInfo.sortNo.asc())
                .fetch();
        log.info("fetch.size():[{}]",fetch.size());
        List<AdvertisingRotationDTO> advertisingRotationDTOS = new ArrayList<>();
        for (AdvertisingInfo advertisingInfo : fetch) {
            AdvertisingRotationDTO advertisingRotationDTO = AdvertisingRotationDTO.builder()
                    .link(advertisingInfo.getLink())
                    .releaseStatus(advertisingInfo.getReleaseStatus().getCode())
                    .releaseStatusDesc(advertisingInfo.getReleaseStatus().getDesc())
                    .sortNo(advertisingInfo.getSortNo())
                    .url(advertisingInfo.getUrl())
                    .build();

            advertisingRotationDTOS.add(advertisingRotationDTO);
        }
        log.info("advertisingRotationDTOS.size()[{}]",advertisingRotationDTOS.size());
        return advertisingRotationDTOS;
    }
}

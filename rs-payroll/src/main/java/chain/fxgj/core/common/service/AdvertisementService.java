package chain.fxgj.core.common.service;

import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import chain.fxgj.core.common.dto.advertising.AdvertisingRotationDTO;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface AdvertisementService {

    /**
     * 轮播图查询
     *
     * @param channelId
     * @return
     */
    @Cacheable(value = "advertisement", key = "'payroll:'.concat(#channelId).concat(#fundLiquidationEnum)")
    List<AdvertisingRotationDTO> rotation(int channelId, FundLiquidationEnum fundLiquidationEnum);
}

package chain.fxgj.core.common.service;

import chain.fxgj.server.payroll.dto.advertising.AdvertisingRotationDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface AdvertisementService {

    /**
     * 轮播图查询
     *
     * @param channelId
     * @return
     */
    @Cacheable(value = "advertisement", key = "'payroll:'.concat(#channelId)")
    List<AdvertisingRotationDTO> rotation(int channelId);

    /**
     * 轮播图查询
     *
     * @param channelId
     * @return
     */
    //@Cacheable(value = "advertisement", key = "'payroll:'.concat(#channelId).concat(#entId)")
    List<AdvertisingRotationDTO> rotation(int channelId, String entId);
}

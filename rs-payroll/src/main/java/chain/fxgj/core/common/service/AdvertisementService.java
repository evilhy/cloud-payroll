package chain.fxgj.core.common.service;


import chain.fxgj.server.payroll.dto.advertising.AdvertisingRotationDTO;
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
    List<AdvertisingRotationDTO> rotation(int channelId);
}

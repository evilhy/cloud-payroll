package chain.fxgj.server.payroll.service;

import chain.wisales.core.dto.promise.FundAppointmentInfoDTO;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

public interface FundService {

    /**
     * 查询客户基金预约信息
     *
     * @return
     * @CachePut 负责增加缓存
     * @Cacheable 负责查询缓存，如果没查到，则将执行方法，并将方法的结果增加到缓存
     */
    @Cacheable(cacheNames = "fund", key = "'jsessionId:'.concat(#jsessionId)")
    FundAppointmentInfoDTO qryFundAppointmentInfo(String jsessionId, String openId);

    /**
     * 预约信息入库
     *
     * @param jsessionId
     * @return
     */
    @CachePut(cacheNames = "fund", key = "'jsessionId:'.concat(#jsessionId)")
    FundAppointmentInfoDTO fundAppointmentInfoSave(String jsessionId, FundAppointmentInfoDTO fundSaveDTO);

    /**
     * 查询客户基金预约信息
     *
     * @return
     * @CachePut 负责增加缓存
     * @Cacheable 负责查询缓存，如果没查到，则将执行方法，并将方法的结果增加到缓存
     */
    @Cacheable(cacheNames = "fund", key = "'jsessionId:'.concat(#jsessionId)")
    FundAppointmentInfoDTO qryFunInfo(String jsessionId);
}

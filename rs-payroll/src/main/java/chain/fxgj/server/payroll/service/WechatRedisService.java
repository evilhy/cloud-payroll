package chain.fxgj.server.payroll.service;

import chain.fxgj.feign.dto.response.WageDetailInfoDTO;
import chain.fxgj.feign.dto.response.WageRes100703;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.payroll.dto.response.PayrollRes100703DTO;
import chain.payroll.dto.response.PayrollWageDetailDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface WechatRedisService {
    /**
     * 薪资列表 - mongo
     *
     * @param idNumber
     * @param groupId
     * @param year
     * @return
     */
    @Cacheable(cacheNames = "wechat", key = "'wageListMongo:'.concat(#idNumber).concat(#groupId).concat(#year).concat(#type)")
    PayrollRes100703DTO wageListByMongo(String idNumber, String groupId, String year, String type);

    /**
     * 薪资列表 - mysql
     *
     * @param idNumber
     * @param groupId
     * @param year
     * @return
     */
    @Cacheable(cacheNames = "wechat", key = "'wageListMysql:'.concat(#idNumber).concat(#groupId).concat(#year).concat(#type)")
    WageRes100703 wageListByMysql(String idNumber, String groupId, String year, String type);

    /**
     * 工资详情 - mongo
     *
     * @return
     */
    @Cacheable(cacheNames = "wechat", key = "'wageDetailMongo:'.concat(#idNumber).concat(#groupId).concat(#wageSheetId)")
    List<PayrollWageDetailDTO> getWageDetailByMongo(String idNumber, String groupId, String wageSheetId);

    /**
     * 工资详情 - mysql
     *
     * @return
     */
    @Cacheable(cacheNames = "wechat", key = "'wageDetailMysql:'.concat(#idNumber).concat(#groupId).concat(#wageSheetId)")
    List<WageDetailInfoDTO> getWageDetailByMysql(String idNumber, String groupId, String wageSheetId);



}

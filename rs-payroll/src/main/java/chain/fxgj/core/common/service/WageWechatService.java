package chain.fxgj.core.common.service;

import chain.fxgj.server.payroll.dto.response.IndexDTO;
import chain.fxgj.server.payroll.dto.response.Res100703;
import chain.fxgj.server.payroll.dto.response.Res100712;
import chain.fxgj.server.payroll.dto.response.WageDetailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface WageWechatService {
    Logger log = LoggerFactory.getLogger(WageWechatService.class);

    /**
     * 最新一条推送信息企业机构
     *
     * @return
     */
    IndexDTO.DataListBean newGroupPushInfo(String idNumber);


    /**
     * 企业机构
     *
     * @return
     */
    List<IndexDTO.DataListBean> groupList(String idNumber);


    /**
     * 工资详情
     *
     * @return
     */
    @Cacheable(cacheNames = "wechat", key = "'wageDetail:'.concat(#idNumber).concat(#groupId).concat(#wageSheetId)")
    List<WageDetailDTO> getWageDetail(String idNumber, String groupId, String wageSheetId);

    /**
     * 走势
     *
     * @param openId
     * @param idNumber
     * @return
     */
    Res100712 wageTrend(String openId, String idNumber);

    /**
     * 薪资列表
     *
     * @param idNumber
     * @param groupId
     * @param year
     * @return
     */
    Res100703 wageList(String idNumber, String groupId, String year, String type);

    @Cacheable(cacheNames = "wechat", key = "'wageList:'.concat(#idNumber).concat(#groupId).concat(#year).concat(#type)")
    Res100703 wageHistroyList(String idNumber, String groupId, String year, String type);

    /**
     * 薪资年份
     */
    List<Integer> years(String employeeSid, String type);

}

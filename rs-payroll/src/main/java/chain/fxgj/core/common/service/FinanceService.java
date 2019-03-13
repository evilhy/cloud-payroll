package chain.fxgj.core.common.service;

import chain.fxgj.core.jpa.model.EmployeeInfo;
import chain.fxgj.server.payroll.dto.tfinance.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FinanceService {
    Logger log = LoggerFactory.getLogger(FinanceService.class);

    /**
     * 获取产品信息
     *
     * @param productId
     * @return
     */
    ProductInfoDTO getProductInfo(String productId, String imgUrl);

    /**
     * 根据身份证号查询预约企业
     *
     * @return
     */
    String getIdNumberIntent(String productId, String idNumber);

    /**
     * 企业预约数
     *
     * @return
     */
    Long getIntentNum(String productId, String entId);

    /**
     * 根据身份证号查询用户预约信息
     *
     * @return
     */
    IntentInfoDTO getIntetByIdNumber(String productId, String idNumber);

    /**
     * 活动预约信息
     *
     * @param productId
     * @return
     */
    IntentListDTO getIntentList(String productId);

    /**
     * 用户操作记录
     *
     * @param productId
     * @param entId
     * @return
     */
    Page<OperateDTO> getOperate(String productId, String entId, Integer operate, Pageable page, String openId);

    /**
     * 查询用户信息
     *
     * @param idNumber
     * @param entId
     * @return
     */
    UserInfoDTO getUserInfo(String idNumber, String entId);

    /**
     * 是否已关注
     *
     * @param openId
     * @return
     */
    boolean isFollowWechat(String openId);

    /**
     * 查询员工信息
     *
     * @param idNumber
     * @param entId
     * @return
     */
    EmployeeInfo getEmpByIdNumberEnt(String idNumber, String entId);

    String getBankProductInfo();


}

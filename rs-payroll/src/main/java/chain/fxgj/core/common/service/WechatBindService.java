package chain.fxgj.core.common.service;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.EmployeeStatusEnum;
import chain.fxgj.core.jpa.model.EmployeeCardLog;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.dto.response.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface WechatBindService {

    /**
     * 获取员工企业
     *
     * @param idNumber
     * @return
     */
    Res100701 getEntList(String idNumber,AppPartnerEnum appPartner);

    List<EmployeeListBean> getEntPhone(String idNumber,AppPartnerEnum appPartner);


    /**
     * 员工个人信息
     *
     * @param idNumber
     * @return
     */
    List<Res100708> empList(String idNumber);

    /**
     * 机构发票
     *
     * @param idNumber
     * @return
     */
    List<GroupInvoiceDTO> invoiceList(String idNumber);


    /**
     * 通过身份证 ，获取（正常）企业列表
     *
     * @param idNumber
     * @return
     */
    @Cacheable(cacheNames = "empInfos", key = "'getEntInfos:'.concat(#idNumber)")
    List<EntInfoDTO> getEntInfos(String idNumber);


    /**
     * 通过身份证 ，获取（正常）企业列表
     *
     * @param idNumber
     * @param employeeStatusEnum
     * @return
     */
    List<EntInfoDTO> getEntInfos(String idNumber, EmployeeStatusEnum[] employeeStatusEnum);


    /**
     * 验证银行卡后六位
     */
    int checkCardNo(String idNumber, String cardNo);

    /**
     * 员工企业
     *
     * @param idNumber
     * @return
     */
    List<EmpEntDTO> empEntList(String idNumber);

    /**
     * 银行卡操作记录
     *
     * @param ids
     * @return
     */
    List<EmpCardLogDTO> empCardLog(String[] ids);

    /**
     * 企业超管
     *
     * @return
     */
    List<EntUserDTO> entUser(String entId);

    /**
     * 验证手机号
     *
     * @param idNumber
     * @param phone
     */
    void checkPhone(String idNumber, String phone);

    Integer getCardUpdIsNew(String idNumber);

    /**
     * 根据openid 查询  查询密码
     *
     * @param openId
     */
    String getQueryPwd(String openId);

    /**
     * 根据id 查询  查询密码
     *
     * @param id
     */
    String getQueryPwdById(String id);

    /**
     * 根据银行卡id获取银行卡修改信息
     *
     * @param bankCardId
     * @return
     */
    List<EmployeeCardLog> getEmployeeCardLogs(String bankCardId);

}

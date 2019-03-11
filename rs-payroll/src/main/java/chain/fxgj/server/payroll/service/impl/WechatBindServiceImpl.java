package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.core.common.constant.DictEnums.EmployeeStatusEnum;
import chain.fxgj.core.jpa.model.EmployeeCardLog;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.dto.response.*;
import chain.fxgj.server.payroll.service.WechatBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class WechatBindServiceImpl implements WechatBindService {


    /**
     * 获取员工企业
     *
     * @param idNumber
     * @return
     */
    @Override
    public Res100701 getEntList(String idNumber) {
        return null;
    }

    @Override
    public List<Res100701.EmployeeListBean> getEntPhone(String idNumber) {
        return null;
    }

    /**
     * 员工个人信息
     *
     * @param idNumber
     * @return
     */
    @Override
    public List<Res100708> empList(String idNumber) {
        return null;
    }

    /**
     * 机构发票
     *
     * @param idNumber
     * @return
     */
    @Override
    public List<GroupInvoiceDTO> invoiceList(String idNumber) {
        return null;
    }

    /**
     * 通过身份证 ，获取（正常）企业列表
     *
     * @param idNumber
     * @return
     */
    @Override
    public List<EntInfoDTO> getEntInfos(String idNumber) {
        return null;
    }

    /**
     * 通过身份证 ，获取（正常）企业列表
     *
     * @param idNumber
     * @param employeeStatusEnum
     * @return
     */
    @Override
    public List<EntInfoDTO> getEntInfos(String idNumber, EmployeeStatusEnum[] employeeStatusEnum) {
        return null;
    }

    /**
     * 验证银行卡后六位
     *
     * @param idNumber
     * @param cardNo
     */
    @Override
    public int checkCardNo(String idNumber, String cardNo) {
        return 0;
    }

    /**
     * 员工企业
     *
     * @param idNumber
     * @return
     */
    @Override
    public List<EmpEntDTO> empEntList(String idNumber) {
        return null;
    }

    /**
     * 银行卡操作记录
     *
     * @param ids
     * @return
     */
    @Override
    public List<EmpCardLogDTO> empCardLog(String[] ids) {
        return null;
    }

    /**
     * 企业超管
     *
     * @param entId
     * @return
     */
    @Override
    public List<EntUserDTO> entUser(String entId) {
        return null;
    }

    /**
     * 验证手机号
     *
     * @param idNumber
     * @param phone
     */
    @Override
    public void checkPhone(String idNumber, String phone) {

    }

    @Override
    public Integer getCardUpdIsNew(String idNumber) {
        return null;
    }

    @Override
    public String getQueryPwd(String openId) {
        return null;
    }

    /**
     * 根据银行卡id获取银行卡修改信息
     *
     * @param bankCardId
     * @return
     */
    @Override
    public List<EmployeeCardLog> getEmployeeCardLogs(String bankCardId) {
        return null;
    }
}

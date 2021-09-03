package chain.fxgj.server.payroll.service;

import chain.fxgj.server.payroll.dto.PageDTO;
import chain.fxgj.server.payroll.dto.wallet.*;
import core.dto.wechat.EmployeeWechatDTO;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/7/16 13:45
 */
public interface WalletService {

    /**
     * 查询当前用户钱包余额
     *
     * @param entId
     * @param dto
     * @return
     */
    WalletBalanceDTO balance(String entId, EmployeeWechatDTO dto, String salt, String passwd);

    /**
     * 查询当前企业下的钱包余额、银行卡数(去重)
     *
     * @param entId
     * @param salt
     * @param passwd
     * @param dto
     * @return
     */
    EmpCardAndBalanceResDTO empCardAdnBalance(String entId, String salt, String passwd, EmployeeWechatDTO dto);

    /**
     * 提现台账分页列表
     *
     * @param entId
     * @param dto
     * @param req
     * @param salt
     * @param passwd
     * @param pageRequest
     * @return
     */
    PageDTO<WithdrawalLedgerPageRes> withdrawalLedgerPage(String entId, EmployeeWechatDTO dto, WithdrawalLedgerPageReq req, String salt, String passwd, PageRequest pageRequest);

    /**
     * 提现台账详情
     *
     * @param withdrawalLedgerId
     * @param entId
     * @param dto
     * @param salt
     * @param passwd
     * @return
     */
    WithdrawalLedgerDetailRes withdrawalLedgerDetail(String withdrawalLedgerId, String entId, EmployeeWechatDTO dto, String salt, String passwd);

    /**
     * 提现进度详情
     *
     * @param withdrawalLedgerId
     * @param entId
     * @param dto
     * @param salt
     * @param passwd
     * @return
     */
    WithdrawalRecordDetailRes withdrawalRecordDetail(String withdrawalLedgerId, String entId, EmployeeWechatDTO dto, String salt, String passwd);

    /**
     * 收款账户列表
     *
     * @param entId
     * @param dto
     * @param salt
     * @param passwd
     * @return
     */
    List<EmployeeCardDTO> employeeCardList(String entId, EmployeeWechatDTO dto, String salt, String passwd);

    /**
     * 确认提现
     *
     * @param entId
     * @param dto
     * @param req
     */
    void withdraw(String entId, EmployeeWechatDTO dto, WithdrawalReq req);
}

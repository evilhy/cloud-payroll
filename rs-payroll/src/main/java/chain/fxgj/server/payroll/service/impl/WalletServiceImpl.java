package chain.fxgj.server.payroll.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.feign.client.*;
import chain.fxgj.server.payroll.dto.PageDTO;
import chain.fxgj.server.payroll.dto.wallet.*;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.WalletService;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.payroll.client.feign.*;
import chain.utils.commons.JsonUtil;
import chain.utils.fxgj.constant.DictEnums.*;
import chain.wage.manager.core.dto.response.entAccount.EntAccountDTO;
import chain.wage.manager.core.dto.response.enterprise.EntErpriseInfoDTO;
import core.dto.ErrorConstant;
import core.dto.request.BaseReqDTO;
import core.dto.request.empCard.EmployeeCardQueryReq;
import core.dto.request.employee.EmployeeQueryReq;
import core.dto.request.employeeWallet.EmployeeWalletQueryReq;
import core.dto.request.employeeWallet.EmployeeWalletSaveReq;
import core.dto.request.withdrawalLedger.WithdrawalLedgerQueryReq;
import core.dto.request.withdrawalLedger.WithdrawalLedgerSaveReq;
import core.dto.request.withdrawalRecordLog.WithdrawalRecordLogQueryReq;
import core.dto.request.withdrawalRecordLog.WithdrawalRecordLogSaveReq;
import core.dto.response.employee.EmployeeDTO;
import core.dto.response.employeeWallet.EmployeeWalletDTO;
import core.dto.response.groupAttach.GroupAttachInfoDTO;
import core.dto.response.wagesheet.WageSheetDTO;
import core.dto.response.withdrawalLedger.WithdrawalLedgerDTO;
import core.dto.response.withdrawalRecordLog.WithdrawalRecordLogDTO;
import core.dto.wechat.CacheUserPrincipal;
import core.dto.wechat.EmployeeWechatDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @Description:钱包
 * @Author: du
 * @Date: 2021/7/16 13:45
 */
@Service
@Slf4j
public class WalletServiceImpl implements WalletService {

    @Autowired
    WalletFeignController walletFeignController;
    @Autowired
    EmployeeFeignController employeeFeignController;
    @Autowired
    EnterpriseFeignService enterpriseFeignService;
    @Autowired
    EnterpriseAttachFeignService enterpriseAttachFeignService;
    @Autowired
    GroupFeignController groupFeignController;
    @Autowired
    GroupAttachInfoServiceFeign groupAttachInfoServiceFeign;
    @Autowired
    EmployeeWalletInfoServiceFeign employeeWalletInfoServiceFeign;
    @Autowired
    WithdrawalLedgerInfoServiceFeign withdrawalLedgerInfoServiceFeign;
    @Autowired
    WithdrawalRecordLogServiceFeign withdrawalRecordLogServiceFeign;
    @Autowired
    EntAccountInfoFeignService entAccountInfoFeignService;
    @Autowired
    WageSheetFeignController wageSheetFeignController;
    @Autowired
    WageDetailFeignController wageDetailFeignController;
    @Autowired
    EntGroupAccountFeignService entGroupAccountFeignService;
    @Autowired
    VirtualAccountFeignService virtualAccountFeignService;
    @Autowired
    VirtualUnitAccountFeignService virtualUnitAccountFeignService;
    @Autowired
    VirtualAccountConnectInfoFeignService virtualAccountConnectInfoFeignService;
    @Autowired
    EmployeeCardFeignService employeeCardFeignService;

    @Autowired
    EmpWechatService empWechatService;


    @Override
    public WalletBalanceDTO balance(String entId, EmployeeWechatDTO dto, String salt, String passwd) {
        //查询钱包
        EmployeeWalletDTO employeeWalletDTO = getEmployeeWallet(entId, dto.getIdNumber(), dto.getName());

        BigDecimal balance = null == employeeWalletDTO.getTotalAmount() ? BigDecimal.ZERO : employeeWalletDTO.getTotalAmount();
        BigDecimal availableAmount = null == employeeWalletDTO.getAvailableAmount() ? BigDecimal.ZERO : employeeWalletDTO.getAvailableAmount();
        BigDecimal frozenAmount = null == employeeWalletDTO.getFrozenAmount() ? BigDecimal.ZERO : employeeWalletDTO.getFrozenAmount();

        return WalletBalanceDTO.builder()
                .totalAmount(EncrytorUtils.encryptField(balance.toString(), salt, passwd))
                .availableAmount(EncrytorUtils.encryptField(availableAmount.toString(), salt, passwd))
                .frozenAmount(EncrytorUtils.encryptField(frozenAmount.toString(), salt, passwd))
                .salt(salt)
                .passwd(passwd)
                .build();
    }

    /**
     * 查询员工在当前企业的钱包信息
     *
     * @param entId
     * @param idNumber
     * @param custName
     * @return
     */
    public EmployeeWalletDTO getEmployeeWallet(String entId, String idNumber, String custName) {
        EmployeeWalletQueryReq walletQueryReq = EmployeeWalletQueryReq.builder()
                .entId(entId)
                .idNumber(idNumber)
                .custName(custName)
                .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                .type(EmpVirtualAccountTypeEnum.ElectronicWallet)
                .build();
        List<EmployeeWalletDTO> walletDTOS = employeeWalletInfoServiceFeign.list(walletQueryReq);
        if (null == walletDTOS || walletDTOS.size() <= 0) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("钱包信息未找到！"));
        }
        return walletDTOS.get(0);
    }

    @Override
    public EmpCardAndBalanceResDTO empCardAdnBalance(String entId, String salt, String passwd, EmployeeWechatDTO dto) {

        //查询银行卡信息
        BaseReqDTO baseReqDTO = BaseReqDTO.builder()
                .idNumber(dto.getIdNumber())
                .entId(entId)
                .build();
        core.dto.response.wallet.EmpCardAndBalanceResDTO empCardResDTO = walletFeignController.empCardAndBalance(baseReqDTO);
        empCardResDTO.setBalance(EncrytorUtils.encryptField(empCardResDTO.getBalance(), salt, passwd));
        empCardResDTO.setPasswd(passwd);
        empCardResDTO.setSalt(salt);

        //查询完成之后,更新缓存中的entId
        String jsessionId = dto.getJsessionId();
        if (StringUtils.isNotBlank(jsessionId)) {
            CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
            empWechatService.upWechatInfoDetail(jsessionId, entId, wechatInfoDetail);
        } else {
            log.info("empCardAndBalance未更新缓存中的entId");
        }

        //查询钱包
        EmployeeWalletDTO employeeWalletDTO = getEmployeeWallet(entId, dto.getIdNumber(), dto.getName());

        String walletNumber = StringUtils.isBlank(employeeWalletDTO.getWalletNumber()) ? "" : employeeWalletDTO.getWalletNumber();
        BigDecimal balance = null == employeeWalletDTO.getTotalAmount() ? BigDecimal.ZERO : employeeWalletDTO.getTotalAmount();
        BigDecimal availableAmount = null == employeeWalletDTO.getAvailableAmount() ? BigDecimal.ZERO : employeeWalletDTO.getAvailableAmount();
        BigDecimal frozenAmount = null == employeeWalletDTO.getFrozenAmount() ? BigDecimal.ZERO : employeeWalletDTO.getFrozenAmount();
        return EmpCardAndBalanceResDTO.builder()
                .employeeWalletId(employeeWalletDTO.getEmployeeWalletId())
                .walletNumber(EncrytorUtils.encryptField(walletNumber, salt, passwd))
                .balance(EncrytorUtils.encryptField(balance.toString(), salt, passwd))
                .availableAmount(EncrytorUtils.encryptField(availableAmount.toString(), salt, passwd))
                .frozenAmount(EncrytorUtils.encryptField(frozenAmount.toString(), salt, passwd))
                .cardNum(empCardResDTO.getCardNum())
                .withdrawStatus(null == employeeWalletDTO.getDelStatusEnum() ? null : employeeWalletDTO.getDelStatusEnum().getCode())
                .withdrawStatusVal(null == employeeWalletDTO.getDelStatusEnum() ? null : employeeWalletDTO.getDelStatusEnum().getDesc())
                .salt(salt)
                .passwd(passwd)
                .build();
    }

    @Override
    public PageDTO<WithdrawalLedgerPageRes> withdrawalLedgerPage(String entId, EmployeeWechatDTO dto, WithdrawalLedgerPageReq req, String salt, String passwd, PageRequest pageRequest) {
        List<WithdrawalLedgerPageRes> list = new ArrayList<>();

        String name = dto.getName();
        String idNumber = dto.getIdNumber();
        //查询钱包
        EmployeeWalletQueryReq walletQueryReq = EmployeeWalletQueryReq.builder()
                .entId(entId)
                .idNumber(idNumber)
                .custName(name)
                .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                .type(EmpVirtualAccountTypeEnum.ElectronicWallet)
                .build();
        List<EmployeeWalletDTO> walletDTOS = employeeWalletInfoServiceFeign.list(walletQueryReq);

        if (null == walletDTOS || walletDTOS.size() <= 0) {
            log.info("=====> 当前用户没有钱包记录 entId:{}, dto:{}", entId, JsonUtil.objectToJson(dto));
            return new PageDTO<>(pageRequest.getPageNumber(), pageRequest.getPageSize(), 0, 0, true, true, list);
        }
        EmployeeWalletDTO employeeWalletDTO = walletDTOS.get(0);
        String walletNumber = StringUtils.isBlank(employeeWalletDTO.getWalletNumber()) ? "" : employeeWalletDTO.getWalletNumber();
        String encryptWalletNumber = EncrytorUtils.encryptField(walletNumber, salt, passwd);

        //员工信息
        EmployeeQueryReq employeeQueryReq = EmployeeQueryReq.builder()
                .entId(entId)
                .idNumber(idNumber)
                .employeeName(name)
                .build();
        List<EmployeeDTO> employeeDTOList = employeeFeignController.query(employeeQueryReq);
        if (null == employeeDTOList || employeeDTOList.size() <= 0) {
            log.info("=====> 当前用户信息异常【员工信息不存在】 entId:{}, idNumber:{}, employeeName:{}", entId, idNumber, name);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("当前用户信息异常"));
        }
        List<String> employeeIds = new ArrayList<>();
        for (EmployeeDTO employeeDTO : employeeDTOList) {
            employeeIds.add(employeeDTO.getEmployeeId());
        }

        //查询台账分页列表
        List<WithdrawalStatusEnum> withdrawalStatus = new ArrayList<>();
        if (null != req.getWithdrawalStatus() && req.getWithdrawalStatus().size() > 0) {
            for (Integer status : req.getWithdrawalStatus()) {
                withdrawalStatus.add(WithdrawalStatusEnum.values()[status]);
            }
        }
        WithdrawalLedgerQueryReq ledgerQueryReq = WithdrawalLedgerQueryReq.builder()
                .entId(entId)
                .employeeWalletId(employeeWalletDTO.getEmployeeWalletId())
                .year(req.getYear())
                .month(req.getMonth())
                .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                .withdrawalStatus(withdrawalStatus)
                .build();
        core.dto.PageDTO<WithdrawalLedgerDTO> page = withdrawalLedgerInfoServiceFeign.page(ledgerQueryReq);
        if (null == page.getContent() || page.getContent().size() <= 0) {
            for (WithdrawalLedgerDTO res : page.getContent()
            ) {
                //收款卡
                String employeeCardNo = res.getEmployeeCardNo();
                EmployeeCardQueryReq cardQueryReq = EmployeeCardQueryReq.builder()
                        .employeeIds(employeeIds)
                        .cardNo(employeeCardNo)
                        .build();
                List<core.dto.response.empCard.EmployeeCardDTO> cardDTOS = employeeCardFeignService.query(cardQueryReq);
                core.dto.response.empCard.EmployeeCardDTO employeeCardDTO = null;
                if (null != cardDTOS && cardDTOS.size() > 0) {
                    employeeCardDTO = cardDTOS.get(0);
                }

                //方案信息
                String wageSheetId = res.getWageSheetId();
                WageSheetDTO wageSheetDTO = wageSheetFeignController.findById(wageSheetId);

                //账户信息
                String accountId = res.getAccountId();
                EntAccountDTO entAccountDTO = entAccountInfoFeignService.findById(accountId);
                //账户状态
                Integer accountStatus = 0;
                String accountStatusVal = "正常";
                if (null != entAccountDTO && AccountStatusEnum.FROZENFROZEN.getCode().equals(entAccountDTO.getAccountStatus())) {
                    accountStatus = 1;
                    accountStatusVal = "异常";
                }

                WithdrawalLedgerPageRes pageRes = WithdrawalLedgerPageRes.builder()
                        .year(res.getYear())
                        .withdrawalStatusVal(null == res.getWithdrawalStatus() ? null : res.getWithdrawalStatus().getDesc())
                        .withdrawalStatus(null == res.getWithdrawalStatus() ? null : res.getWithdrawalStatus().getCode())
                        .withdrawalLedgerId(res.getWithdrawalLedgerId())
                        .walletNumber(encryptWalletNumber)
                        .updDateTime(null == res.getUpdDateTime() ? null : res.getUpdDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .transAmount(EncrytorUtils.encryptField(res.getTransAmount().toString(), salt, passwd))
                        .remark(res.getRemark())
                        .payDateTime(null == res.getPayDateTime() ? null : res.getPayDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .openBank(null == employeeCardDTO ? null : employeeCardDTO.getIssuerName())
                        .month(res.getMonth())
                        .issueTime(null == res.getIssueTime() ? null : res.getIssueTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .idNumber(res.getIdNumber())
                        .fundTypeVal(null == wageSheetDTO ? null : wageSheetDTO.getFundTypeDesc())
                        .fundType(null == wageSheetDTO ? null : wageSheetDTO.getFundType())
                        .fundDateVal(null == wageSheetDTO ? null : wageSheetDTO.getFundDate().getDesc())
                        .fundDate(null == wageSheetDTO ? null : wageSheetDTO.getFundDate().getCode())
                        .wageSheetName(null == wageSheetDTO ? null : wageSheetDTO.getWageName())
                        .entId(res.getEntId())
                        .employeeCardNo(employeeCardNo)
                        .custName(EncrytorUtils.encryptField(res.getCustName(), salt, passwd))
                        .crtDateTime(null == res.getCrtDateTime() ? null : res.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .accountStatus(accountStatus)
                        .accountStatusVal(accountStatusVal)
                        .accountOpenBank(null == entAccountDTO ? null : entAccountDTO.getAccountOpenBank())
                        .accountStar(null == entAccountDTO ? null : entAccountDTO.getAccountStar())
                        .accountName(null == entAccountDTO ? null : entAccountDTO.getAccountName())
                        .account(null == entAccountDTO ? null : entAccountDTO.getAccount())
                        .accountId(null == entAccountDTO ? null : entAccountDTO.getId())
                        .bankClose(isCloseBank())
                        .salt(salt)
                        .passwd(passwd)
                        .build();
                list.add(pageRes);
            }
        }
        return new PageDTO<>(page.getPageNum(), page.getPageSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isFirst(), list);
    }

    @Override
    public WithdrawalLedgerDetailRes withdrawalLedgerDetail(String withdrawalLedgerId, String entId, EmployeeWechatDTO dto, String salt, String passwd) {
        Optional.ofNullable(withdrawalLedgerId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("提现台账ID不能为空")));

        //台账
        WithdrawalLedgerDTO ledgerDTO = withdrawalLedgerInfoServiceFeign.findById(withdrawalLedgerId);
        if (null == ledgerDTO) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("提现台账信息未找到"));
        }

        //钱包
        String employeeWalletId = ledgerDTO.getEmployeeWalletId();
        EmployeeWalletDTO employeeWalletDTO = employeeWalletInfoServiceFeign.findById(employeeWalletId);
        if (null == employeeWalletDTO) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("钱包信息未找到"));
        }

        //员工信息
        EmployeeQueryReq employeeQueryReq = EmployeeQueryReq.builder()
                .entId(entId)
                .idNumber(ledgerDTO.getIdNumber())
                .employeeName(ledgerDTO.getCustName())
                .build();
        List<EmployeeDTO> employeeDTOList = employeeFeignController.query(employeeQueryReq);

        core.dto.response.empCard.EmployeeCardDTO employeeCardDTO = null;
        String employeeCardNo = ledgerDTO.getEmployeeCardNo();
        if (null != employeeDTOList && employeeDTOList.size() > 0) {
            List<String> employeeIds = new ArrayList<>();
            for (EmployeeDTO employeeDTO : employeeDTOList) {
                employeeIds.add(employeeDTO.getEmployeeId());
            }

            //收款卡
            EmployeeCardQueryReq cardQueryReq = EmployeeCardQueryReq.builder()
                    .employeeIds(employeeIds)
                    .cardNo(employeeCardNo)
                    .build();
            List<core.dto.response.empCard.EmployeeCardDTO> cardDTOS = employeeCardFeignService.query(cardQueryReq);
            if (null != cardDTOS && cardDTOS.size() > 0) {
                employeeCardDTO = cardDTOS.get(0);
            }
        }

        //方案信息
        String wageSheetId = ledgerDTO.getWageSheetId();
        WageSheetDTO wageSheetDTO = wageSheetFeignController.findById(wageSheetId);

        //账户信息
        String accountId = ledgerDTO.getAccountId();
        EntAccountDTO entAccountDTO = entAccountInfoFeignService.findById(accountId);
        //账户状态
        Integer accountStatus = 0;
        String accountStatusVal = "正常";
        if (null != entAccountDTO && AccountStatusEnum.FROZENFROZEN.getCode().equals(entAccountDTO.getAccountStatus())) {
            accountStatus = 1;
            accountStatusVal = "异常";
        }

        //获取最后一次的提现记录
        WithdrawalRecordLogQueryReq logQueryReq = WithdrawalRecordLogQueryReq.builder()
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
                .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                .build();
        List<WithdrawalRecordLogDTO> logDTOS = withdrawalRecordLogServiceFeign.list(logQueryReq);
        WithdrawalRecordLogDTO logDTO = null;
        if (null != logDTOS && logDTOS.size() > 0) {
            logDTO = logDTOS.get(0);
        }

        //企业
        EntErpriseInfoDTO erpriseInfoDTO = enterpriseFeignService.findById(entId);

        return WithdrawalLedgerDetailRes.builder()
                .walletNumber(employeeWalletDTO.getWalletNumber())
                .withdrawStatus(null == employeeWalletDTO.getDelStatusEnum() ? null : employeeWalletDTO.getDelStatusEnum().getCode())
                .withdrawStatusVal(null == employeeWalletDTO.getDelStatusEnum() ? null : employeeWalletDTO.getDelStatusEnum().getDesc())
                .year(ledgerDTO.getYear())
                .withdrawalStatusVal(null == ledgerDTO.getWithdrawalStatus() ? null : ledgerDTO.getWithdrawalStatus().getDesc())
                .withdrawalStatus(null == ledgerDTO.getWithdrawalStatus() ? null : ledgerDTO.getWithdrawalStatus().getCode())
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
                .updDateTime(null == ledgerDTO.getUpdDateTime() ? null : ledgerDTO.getUpdDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .transAmount(EncrytorUtils.encryptField(ledgerDTO.getTransAmount().toString(), salt, passwd))
                .remark(ledgerDTO.getRemark())
                .payDateTime(null == ledgerDTO.getPayDateTime() ? null : ledgerDTO.getPayDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .month(ledgerDTO.getMonth())
                .issueTime(null == ledgerDTO.getIssueTime() ? null : ledgerDTO.getIssueTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .idNumber(ledgerDTO.getIdNumber())
                .fundTypeVal(null == wageSheetDTO ? null : wageSheetDTO.getFundTypeDesc())
                .fundType(null == wageSheetDTO ? null : wageSheetDTO.getFundType())
                .fundDateVal(null == wageSheetDTO ? null : wageSheetDTO.getFundDate().getDesc())
                .fundDate(null == wageSheetDTO ? null : wageSheetDTO.getFundDate().getCode())
                .entId(ledgerDTO.getEntId())
                .employeeCardNo(EncrytorUtils.encryptField(employeeCardNo, salt, passwd))
                .employeeCardStar(employeeCardNo.substring(0, 4) + "****" + employeeCardNo.substring(employeeCardNo.length() - 4, employeeCardNo.length()))
                .custName(EncrytorUtils.encryptField(ledgerDTO.getCustName(), salt, passwd))
                .crtDateTime(null == ledgerDTO.getCrtDateTime() ? null : ledgerDTO.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .accountOpenBank(null == entAccountDTO ? null : entAccountDTO.getAccountOpenBank())
                .accountStar(null == entAccountDTO ? null : entAccountDTO.getAccountStar())
                .accountName(null == entAccountDTO ? null : entAccountDTO.getAccountName())
                .account(null == entAccountDTO ? null : entAccountDTO.getAccount())
                .accountId(null == entAccountDTO ? null : entAccountDTO.getId())
                .accountStatus(accountStatus)
                .accountStatusVal(accountStatusVal)
                .salt(salt)
                .passwd(passwd)
                .withdrawalRecordLogId(ledgerDTO.getWithdrawalRecordLogId())
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
                .wageSheetName(null == wageSheetDTO ? null : wageSheetDTO.getWageName())
                .transNo(null == logDTO ? null : logDTO.getTransNo())
                .openBank(null == employeeCardDTO ? null : employeeCardDTO.getIssuerName())
                .entName(null == erpriseInfoDTO ? null : erpriseInfoDTO.getEntName())
                .entId(entId)
                .build();
    }

    @Override
    public WithdrawalRecordDetailRes withdrawalRecordDetail(String withdrawalLedgerId, String entId, EmployeeWechatDTO dto, String salt, String passwd) {
        WithdrawalLedgerDetailRes ledgerDetail = withdrawalLedgerDetail(withdrawalLedgerId, entId, dto, salt, passwd);
        chain.fxgj.server.payroll.dto.wallet.WithdrawalRecordLogDTO withdrawalRecordLogDTO = null;
        if (null != ledgerDetail && StringUtils.isNotBlank(ledgerDetail.getWithdrawalRecordLogId())) {
            WithdrawalRecordLogDTO logDTO = withdrawalRecordLogServiceFeign.findById(ledgerDetail.getWithdrawalRecordLogId());
            if (null != logDTO) {
                withdrawalRecordLogDTO = chain.fxgj.server.payroll.dto.wallet.WithdrawalRecordLogDTO.builder()
                        .updDateTime(null == logDTO.getUpdDateTime() ? null : logDTO.getUpdDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .withdrawalRecordLogId(logDTO.getWithdrawalRecordLogId())
                        .withdrawalLedgerId(logDTO.getWithdrawalLedgerId())
                        .transStatusVal(logDTO.getTransNo())
                        .transStatus(logDTO.getTransStatus().getCode())
                        .transStatusVal(logDTO.getTransStatus().getDesc())
                        .transNo(logDTO.getTransNo())
                        .custName(ledgerDetail.getCustName())
                        .transAmount(EncrytorUtils.encryptField(logDTO.getTransAmount().toString(), salt, passwd))
                        .remark(logDTO.getRemark())
                        .predictDateTime(null == logDTO.getPredictDateTime() ? null : logDTO.getPredictDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .payDateTime(null == logDTO.getPayDateTime() ? null : logDTO.getPayDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .openBank(logDTO.getOpenBank())
                        .failDesc(logDTO.getFailDesc())
                        .employeeCardNo(EncrytorUtils.encryptField(logDTO.getEmployeeCardNo().toString(), salt, passwd))
                        .employeeCardStar(logDTO.getEmployeeCardNo().substring(0, 4) + "****" + logDTO.getEmployeeCardNo().substring(logDTO.getEmployeeCardNo().length() - 4, logDTO.getEmployeeCardNo().length()))
                        .delStatus(logDTO.getTransStatus().getCode())
                        .crtDateTime(null == logDTO.getCrtDateTime() ? null : logDTO.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .applyDateTime(null == logDTO.getApplyDateTime() ? null : logDTO.getApplyDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .delStatusVal(logDTO.getEmployeeCardNo())
                        .build();
            }
        }
        return WithdrawalRecordDetailRes.builder()
                .withdrawalRecordLog(withdrawalRecordLogDTO)
                .withdrawalLedgerDetail(ledgerDetail)
                .build();
    }

    @Override
    public List<EmployeeCardDTO> employeeCardList(String entId, EmployeeWechatDTO dto, String salt, String passwd) {
        List<EmployeeCardDTO> list = new ArrayList<>();
        //员工信息
        EmployeeQueryReq employeeQueryReq = EmployeeQueryReq.builder()
                .entId(entId)
                .idNumber(dto.getIdNumber())
                .employeeName(dto.getName())
                .build();
        List<EmployeeDTO> employeeDTOList = employeeFeignController.query(employeeQueryReq);

        if (null != employeeDTOList && employeeDTOList.size() > 0) {
            List<String> employeeIds = new ArrayList<>();
            for (EmployeeDTO employeeDTO : employeeDTOList) {
                employeeIds.add(employeeDTO.getEmployeeId());
            }

            //收款卡
            EmployeeCardQueryReq cardQueryReq = EmployeeCardQueryReq.builder()
                    .employeeIds(employeeIds)
                    .build();
            List<core.dto.response.empCard.EmployeeCardDTO> cardDTOS = employeeCardFeignService.query(cardQueryReq);
            if (null != cardDTOS && cardDTOS.size() > 0) {
                for (core.dto.response.empCard.EmployeeCardDTO cardDTO : cardDTOS
                ) {
                    EmployeeCardDTO employeeCardDTO = EmployeeCardDTO.builder()
                            .issuerName(cardDTO.getIssuerName())
                            .employeeCardId(cardDTO.getEmployeeId())
                            .issuerBankId(cardDTO.getIssuerBankId())
                            .cardNo(cardDTO.getCardNo())
                            .build();
                    list.add(employeeCardDTO);
                }
            }
        }
        return list;
    }

    @Override
    public void withdraw(String entId, EmployeeWechatDTO dto, WithdrawalReq req) {
        String cardNo = req.getCardNo();
        String withdrawalLedgerId = req.getWithdrawalLedgerId();
        String issuerName = req.getIssuerName();

        //查询台账
        WithdrawalLedgerDTO ledgerDTO = withdrawalLedgerInfoServiceFeign.findById(withdrawalLedgerId);
        if (null == ledgerDTO) {
            log.info("=====> 未找到台账信息，提现失败, withdrawalLedgerId:{}", withdrawalLedgerId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到台账信息，提现失败"));
        }
        switch (ledgerDTO.getWithdrawalStatus()) {
            case Ing:
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("提现进行中，请勿重复提交"));
            case Success:
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("提现已完成，请勿重复提交"));
            default:
                log.info("=====> 开始提现 req：{}, ledgerDTO:{}", JsonUtil.objectToJson(req), JsonUtil.objectToJson(ledgerDTO));
                break;
        }
        BigDecimal transAmount = ledgerDTO.getTransAmount();

        //单位是否开启提现功能
        String groupId = ledgerDTO.getGroupId();
        GroupAttachInfoDTO groupAttachInfoDTO = groupAttachInfoServiceFeign.findGroupAttachById(groupId);
        if (null == groupAttachInfoDTO || ModelStatusEnum.ENSABLE != groupAttachInfoDTO.getWalletStatus()) {
            log.info("=====> 当前用工单位提现功能已关闭 groupId:{}, groupAttachInfoDTO:{}", groupId, JsonUtil.objectToJson(groupAttachInfoDTO));
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("当前用工单位提现功能已关闭"));
        }

        //查询钱包
        String employeeWalletId = ledgerDTO.getEmployeeWalletId();
        EmployeeWalletDTO employeeWalletDTO = employeeWalletInfoServiceFeign.findById(employeeWalletId);
        if (null == employeeWalletDTO) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("钱包信息不存在"));
        }

        //员工信息
        EmployeeQueryReq employeeQueryReq = EmployeeQueryReq.builder()
                .entId(entId)
                .idNumber(ledgerDTO.getIdNumber())
                .employeeName(ledgerDTO.getCustName())
                .build();
        List<EmployeeDTO> employeeDTOList = employeeFeignController.query(employeeQueryReq);

        core.dto.response.empCard.EmployeeCardDTO employeeCardDTO = null;
        if (null != employeeDTOList && employeeDTOList.size() > 0) {
            List<String> employeeIds = new ArrayList<>();
            for (EmployeeDTO employeeDTO : employeeDTOList) {
                employeeIds.add(employeeDTO.getEmployeeId());
            }

            //收款卡
            EmployeeCardQueryReq cardQueryReq = EmployeeCardQueryReq.builder()
                    .employeeIds(employeeIds)
                    .cardNo(cardNo)
                    .build();
            List<core.dto.response.empCard.EmployeeCardDTO> cardDTOS = employeeCardFeignService.query(cardQueryReq);
            if (null != cardDTOS && cardDTOS.size() > 0) {
                employeeCardDTO = cardDTOS.get(0);
            }
        }

        //生成提现记录
        WithdrawalRecordLogSaveReq build = WithdrawalRecordLogSaveReq.builder()
                .applyDateTime(LocalDateTime.now())
                .delStatusEnum(DelStatusEnum.normal)
                .employeeCardNo(employeeCardDTO.getCardNo())
                .openBank(employeeCardDTO.getCardNo())
                .transAmount(transAmount)
                .transStatus(TransDealStatusEnum.ING)
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
//                .withdrawalRecordLogId()
//                .failDesc()
//                .payDateTime()
//                .predictDateTime()
//                .remark()
//                .transNo()
                .build();
        WithdrawalRecordLogDTO logDTO = withdrawalRecordLogServiceFeign.save(build);

        //更新提现台账
        WithdrawalLedgerSaveReq ledgerSaveReq = WithdrawalLedgerSaveReq.builder()
                .employeeCardNo(employeeCardDTO.getCardNo())
                .openBank(employeeCardDTO.getIssuerName())
                .build();
        WithdrawalLedgerDTO ledgerDTO1 = withdrawalLedgerInfoServiceFeign.save(ledgerSaveReq);

        //查询当前钱包余额
        core.dto.response.employeeWallet.WalletBalanceDTO balance = withdrawalLedgerInfoServiceFeign.balance(employeeWalletId);

        //修改钱包余额
        EmployeeWalletSaveReq walletSaveReq = EmployeeWalletSaveReq.builder()
                .totalAmount(balance.getTotalAmount())
                .frozenAmount(balance.getFrozenAmount())
                .availableAmount(balance.getAvailableAmount())
                .employeeWalletId(employeeWalletId)
                .build();
        EmployeeWalletDTO walletDTO = employeeWalletInfoServiceFeign.save(walletSaveReq);

        //TODO 调用接口发送到银行
        //预计到帐时间
        LocalDateTime predictDateTime = null;
        //流水号
        String transNo = null;



        //更新提现记录
        WithdrawalRecordLogSaveReq logSaveReq = WithdrawalRecordLogSaveReq.builder()
                .withdrawalRecordLogId(logDTO.getWithdrawalRecordLogId())
                .predictDateTime(predictDateTime)
                .transNo(transNo)
                .build();
        withdrawalRecordLogServiceFeign.save(logSaveReq);
    }

    /**
     * 银行是否收市
     *
     * @return
     */
    public static boolean isCloseBank() {
        String date = DateFormatUtils.format(new Date(), "yyyyMMdd");
        Long now = Long.parseLong(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        Long start = Long.parseLong(date + "215900");
        Long end = Long.parseLong(date + "235959");
        if (now >= start && now <= end) {
            return true;
        }
        return false;
    }
}

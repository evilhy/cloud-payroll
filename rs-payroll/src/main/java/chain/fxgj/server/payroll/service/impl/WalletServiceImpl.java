package chain.fxgj.server.payroll.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.feign.client.*;
import chain.fxgj.server.payroll.dto.PageDTO;
import chain.fxgj.server.payroll.dto.wallet.*;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.WalletService;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.payroll.client.feign.*;
import chain.utils.commons.JacksonUtil;
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
import core.dto.request.wageDetail.WageDetailQueryReq;
import core.dto.request.withdrawalLedger.WithdrawalLedgerQueryReq;
import core.dto.request.withdrawalLedger.WithdrawalLedgerSaveReq;
import core.dto.request.withdrawalRecordLog.WithdrawalRecordLogQueryReq;
import core.dto.request.withdrawalRecordLog.WithdrawalRecordLogSaveReq;
import core.dto.request.withdrawalSchedule.WithdrawalScheduleQueryReq;
import core.dto.request.withdrawalSchedule.WithdrawalScheduleSaveReq;
import core.dto.response.employee.EmployeeDTO;
import core.dto.response.employeeWallet.EmployeeWalletDTO;
import core.dto.response.entAttach.EnterpriseAttachRes;
import core.dto.response.group.GroupDTO;
import core.dto.response.groupAttach.GroupAttachInfoDTO;
import core.dto.response.wageDetail.WageDetailDTO;
import core.dto.response.wagesheet.WageSheetDTO;
import core.dto.response.withdrawalLedger.WithdrawalLedgerDTO;
import core.dto.response.withdrawalRecordLog.WithdrawalRecordLogDTO;
import core.dto.response.withdrawalSchedule.WithdrawalScheduleDTO;
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
 * @Description:??????
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
    EmployeeCardFeignService employeeCardFeignService;
    @Autowired
    WithdrawalScheduleFeignService withdrawalScheduleFeignService;
    @Autowired
    EmpWechatService empWechatService;


    @Override
    public WalletBalanceDTO balance(String entId, EmployeeWechatDTO dto, String salt, String passwd) {
        //????????????
        EmployeeWalletDTO employeeWalletDTO = getEmployeeWallet(entId, dto.getIdNumber(), dto.getName());

        BigDecimal balance = null == employeeWalletDTO || null == employeeWalletDTO.getTotalAmount() ? BigDecimal.ZERO : employeeWalletDTO.getTotalAmount();
        BigDecimal availableAmount = null == employeeWalletDTO || null == employeeWalletDTO.getAvailableAmount() ? BigDecimal.ZERO : employeeWalletDTO.getAvailableAmount();
        BigDecimal frozenAmount = null == employeeWalletDTO || null == employeeWalletDTO.getFrozenAmount() ? BigDecimal.ZERO : employeeWalletDTO.getFrozenAmount();

        return WalletBalanceDTO.builder()
                .totalAmount(EncrytorUtils.encryptField(balance.toString(), salt, passwd))
                .availableAmount(EncrytorUtils.encryptField(availableAmount.toString(), salt, passwd))
                .frozenAmount(EncrytorUtils.encryptField(frozenAmount.toString(), salt, passwd))
                .salt(salt)
                .passwd(passwd)
                .build();
    }

    /**
     * ??????????????????????????????????????????
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
            log.info("=====> ????????????????????????walletQueryReq:{}???walletDTOS:{}", JsonUtil.objectToJson(walletQueryReq), JsonUtil.objectToJson(walletDTOS));
//            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????"));
            return null;
        }
        return walletDTOS.get(0);
    }

    @Override
    public EmpCardAndBalanceResDTO empCardAdnBalance(String entId, String salt, String passwd, EmployeeWechatDTO dto) {

        //?????????????????????
        BaseReqDTO baseReqDTO = BaseReqDTO.builder()
                .idNumber(dto.getIdNumber())
                .entId(entId)
                .build();
        core.dto.response.wallet.EmpCardAndBalanceResDTO empCardResDTO = walletFeignController.empCardAndBalance(baseReqDTO);
        empCardResDTO.setBalance(EncrytorUtils.encryptField(empCardResDTO.getBalance(), salt, passwd));
        empCardResDTO.setPasswd(passwd);
        empCardResDTO.setSalt(salt);

        //??????????????????,??????????????????entId
        String jsessionId = dto.getJsessionId();
        if (StringUtils.isNotBlank(jsessionId)) {
            CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
            empWechatService.upWechatInfoDetail(jsessionId, entId, wechatInfoDetail);
        } else {
            log.info("empCardAndBalance?????????????????????entId");
        }

        //????????????
        EmployeeWalletDTO employeeWalletDTO = getEmployeeWallet(entId, dto.getIdNumber(), dto.getName());

        //??????????????????
        EnterpriseAttachRes enterpriseAttachRes = enterpriseAttachFeignService.attachInfo(entId);
        ModelStatusEnum withdrawStatus = ModelStatusEnum.DISABLE;
        if (null != enterpriseAttachRes && null != enterpriseAttachRes.getWithdrawStatus()) {
            withdrawStatus = enterpriseAttachRes.getWithdrawStatus();
        }

        // ??????????????????
        BigDecimal bigDecimal = recentlyIssued(entId, dto.getName(), dto.getIdNumber(), employeeWalletDTO);

        String walletNumber = null == employeeWalletDTO || StringUtils.isBlank(employeeWalletDTO.getWalletNumber()) ? "" : employeeWalletDTO.getWalletNumber();
        BigDecimal balance = null == employeeWalletDTO || null == employeeWalletDTO.getTotalAmount() ? BigDecimal.ZERO : employeeWalletDTO.getTotalAmount();
        BigDecimal availableAmount = null == employeeWalletDTO || null == employeeWalletDTO.getAvailableAmount() ? BigDecimal.ZERO : employeeWalletDTO.getAvailableAmount();
        BigDecimal frozenAmount = null == employeeWalletDTO || null == employeeWalletDTO.getFrozenAmount() ? BigDecimal.ZERO : employeeWalletDTO.getFrozenAmount();
        return EmpCardAndBalanceResDTO.builder()
                .employeeWalletId(null == employeeWalletDTO ? null : employeeWalletDTO.getEmployeeWalletId())
                .walletNumber(StringUtils.isBlank(walletNumber) ? null : EncrytorUtils.encryptField(walletNumber, salt, passwd))
                .balance(null == balance ? null : EncrytorUtils.encryptField(balance.toString(), salt, passwd))
                .availableAmount(null == availableAmount ? null : EncrytorUtils.encryptField(availableAmount.toString(), salt, passwd))
                .frozenAmount(null == frozenAmount ? null : EncrytorUtils.encryptField(frozenAmount.toString(), salt, passwd))
                .cardNum(empCardResDTO.getCardNum())
                .withdrawStatus(withdrawStatus.getCode())
                .withdrawStatusVal(withdrawStatus.getDesc())
                .recentlyIssuedAmt(null == bigDecimal ? EncrytorUtils.encryptField("0.00", salt, passwd) : EncrytorUtils.encryptField(bigDecimal.toString(), salt, passwd))
                .salt(salt)
                .passwd(passwd)
                .build();
    }

    /**
     * ??????????????????
     * <p>
     * 1???????????????????????????       isCountStatus=YES???payStatus=SUCCESS
     * 2??????????????????????????????      isCountStatus=YES
     * 3??????????????????
     *
     * @param entId
     * @param custName
     * @param idNumber
     * @return
     */
    public BigDecimal recentlyIssued(String entId, String custName, String idNumber, EmployeeWalletDTO employeeWalletDTO) {
        //??????????????????
        WageDetailDTO wageDetailDTO = null;
        WageDetailQueryReq detailQueryReq = WageDetailQueryReq.builder()
                .entId(entId)
                .idNumber(idNumber)
                .custName(custName)
                .isCountStatus(IsStatusEnum.YES)
                .payStatus(Arrays.asList(PayStatusEnum.values()))
                .build();
        core.dto.PageDTO<WageDetailDTO> wageDetailPage = wageDetailFeignController.page(detailQueryReq);
        if (null != wageDetailPage && null != wageDetailPage.getContent() && wageDetailPage.getContent().size() > 0) {
            wageDetailDTO = wageDetailPage.getContent().get(0);
        }

        WithdrawalLedgerDTO withdrawalLedgerDTO = null;
        if (null != employeeWalletDTO) {
            //????????????
            WithdrawalLedgerQueryReq ledgerQueryReq = WithdrawalLedgerQueryReq.builder()
                    .entId(entId)
                    .employeeWalletId(employeeWalletDTO.getEmployeeWalletId())
                    .withdrawalStatus(Arrays.asList(WithdrawalStatusEnum.Await, WithdrawalStatusEnum.Ing, WithdrawalStatusEnum.Success, WithdrawalStatusEnum.Fail))
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            core.dto.PageDTO<WithdrawalLedgerDTO> ledgerDTOPage = withdrawalLedgerInfoServiceFeign.page(ledgerQueryReq);
            if (null != ledgerDTOPage && null != ledgerDTOPage.getContent() && ledgerDTOPage.getContent().size() > 0) {
                withdrawalLedgerDTO = ledgerDTOPage.getContent().get(0);
            }
        }

        if (null == wageDetailDTO) {
            if (null == withdrawalLedgerDTO) {
                return BigDecimal.ZERO;
            } else {
                return withdrawalLedgerDTO.getTransAmount();
            }
        } else {
            if (null != withdrawalLedgerDTO) {
                return withdrawalLedgerDTO.getCrtDateTime().isAfter(wageDetailDTO.getCrtDateTime()) ? withdrawalLedgerDTO.getTransAmount() : wageDetailDTO.getRealTotalAmt();
            } else {
                return wageDetailDTO.getRealTotalAmt();
            }
        }
    }

    @Override
    public PageDTO<WithdrawalLedgerPageRes> withdrawalLedgerPage(String entId, EmployeeWechatDTO dto, WithdrawalLedgerPageReq req, String salt, String passwd, PageRequest pageRequest) {
        List<WithdrawalLedgerPageRes> list = new ArrayList<>();

        String name = dto.getName();
        String idNumber = dto.getIdNumber();
        //????????????
        EmployeeWalletQueryReq walletQueryReq = EmployeeWalletQueryReq.builder()
                .entId(entId)
                .idNumber(idNumber)
                .custName(name)
                .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                .type(EmpVirtualAccountTypeEnum.ElectronicWallet)
                .build();
        List<EmployeeWalletDTO> walletDTOS = employeeWalletInfoServiceFeign.list(walletQueryReq);

        if (null == walletDTOS || walletDTOS.size() <= 0) {
            log.info("=====> ?????????????????????????????? entId:{}, dto:{}", entId, JsonUtil.objectToJson(dto));
            return new PageDTO<>(pageRequest.getPageNumber(), pageRequest.getPageSize(), 0, 0, true, true, list);
        }
        EmployeeWalletDTO employeeWalletDTO = walletDTOS.get(0);
        String walletNumber = StringUtils.isBlank(employeeWalletDTO.getWalletNumber()) ? "" : employeeWalletDTO.getWalletNumber();
        String encryptWalletNumber = EncrytorUtils.encryptField(walletNumber, salt, passwd);

        //????????????
        EmployeeQueryReq employeeQueryReq = EmployeeQueryReq.builder()
                .entId(entId)
                .idNumber(idNumber)
                .employeeName(name)
                .build();
        List<EmployeeDTO> employeeDTOList = employeeFeignController.query(employeeQueryReq);
        if (null == employeeDTOList || employeeDTOList.size() <= 0) {
            log.info("=====> ??????????????????????????????????????????????????? entId:{}, idNumber:{}, employeeName:{}", entId, idNumber, name);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????"));
        }
        List<String> employeeIds = new ArrayList<>();
        for (EmployeeDTO employeeDTO : employeeDTOList) {
            employeeIds.add(employeeDTO.getEmployeeId());
        }

        //????????????????????????
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
        if (null != page.getContent() && page.getContent().size() > 0) {
            for (WithdrawalLedgerDTO res : page.getContent()
            ) {
                //?????????
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

                //????????????
                String wageSheetId = res.getWageSheetId();
                WageSheetDTO wageSheetDTO = wageSheetFeignController.findById(wageSheetId);

                //????????????
                String accountId = res.getAccountId();
                EntAccountDTO entAccountDTO = entAccountInfoFeignService.findById(accountId);

                //????????????
                EntErpriseInfoDTO infoDTO = enterpriseFeignService.findById(entId);
                String liquidationDesc = null;
                if (null != infoDTO.getLiquidation()) {
                    liquidationDesc = infoDTO.getLiquidation().getDesc();
                }

                //????????????
                Integer accountStatus = 0;
                String accountStatusVal = "??????";
                if (null != entAccountDTO && AccountStatusEnum.FROZENFROZEN.getCode().equals(entAccountDTO.getAccountStatus())) {
                    accountStatus = 1;
                    accountStatusVal = "??????";
                }

                //????????????
                String groupId = res.getGroupId();
                GroupDTO groupDTO = groupFeignController.findById(groupId);

                //????????????
                GroupAttachInfoDTO groupAttachInfoDTO = groupAttachInfoServiceFeign.findGroupAttachById(groupId);
                ModelStatusEnum withdrawStatus = ModelStatusEnum.DISABLE;
                if (null != groupAttachInfoDTO) {
                    withdrawStatus = groupAttachInfoDTO.getWithdrawStatus();
                }

                WithdrawalLedgerPageRes pageRes = WithdrawalLedgerPageRes.builder()
                        .dealType(null == res.getDealType() ? null : res.getDealType().getCode())
                        .dealTypeVal(null == res.getDealType() ? null : res.getDealType().getDesc())
                        .cutoffTime(null == res.getCutoffTime() ? null : res.getCutoffTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .systemTime(System.currentTimeMillis())
                        .year(res.getYear())
                        .withdrawalStatusVal(null == res.getWithdrawalStatus() ? null : res.getWithdrawalStatus().getDesc())
                        .withdrawalStatus(null == res.getWithdrawalStatus() ? null : res.getWithdrawalStatus().getCode())
                        .withdrawalLedgerId(res.getWithdrawalLedgerId())
                        .walletNumber(encryptWalletNumber)
                        .withdrawStatus(withdrawStatus.getCode())
                        .withdrawStatusVal(withdrawStatus.getDesc())
                        .updDateTime(null == res.getUpdDateTime() ? null : res.getUpdDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .transAmount(EncrytorUtils.encryptField(res.getTransAmount().toString(), salt, passwd))
                        .remark(res.getRemark())
                        .payDateTime(null == res.getPayDateTime() ? null : res.getPayDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .openBank(null == employeeCardDTO ? null : employeeCardDTO.getIssuerName())
                        .month(res.getMonth())
                        .issueTime(null == res.getIssueTime() ? null : res.getIssueTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .idNumber(EncrytorUtils.encryptField(res.getIdNumber(), salt, passwd))
                        .fundTypeVal(null == wageSheetDTO ? null : wageSheetDTO.getFundTypeDesc())
                        .fundType(null == wageSheetDTO ? null : wageSheetDTO.getFundType())
                        .fundDateVal(null == wageSheetDTO ? null : wageSheetDTO.getFundDate().getDesc())
                        .fundDate(null == wageSheetDTO ? null : wageSheetDTO.getFundDate().getCode())
                        .wageSheetName(null == wageSheetDTO ? null : wageSheetDTO.getWageName())
                        .entId(res.getEntId())
                        .employeeCardNo(EncrytorUtils.encryptField(employeeCardNo, salt, passwd))
                        .custName(EncrytorUtils.encryptField(res.getCustName(), salt, passwd))
                        .crtDateTime(null == res.getCrtDateTime() ? null : res.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .accountStatus(accountStatus)
                        .accountStatusVal(accountStatusVal)
                        .accountOpenBank(liquidationDesc)
                        .accountStar(null == entAccountDTO ? null : entAccountDTO.getAccountStar())
                        .accountName(null == entAccountDTO ? null : entAccountDTO.getAccountName())
                        .account(null == entAccountDTO ? null : EncrytorUtils.encryptField(entAccountDTO.getAccount(), salt, passwd))
                        .accountId(null == entAccountDTO ? null : entAccountDTO.getId())
                        .bankClose(isCloseBank())
                        .groupName(null == groupDTO ? null : groupDTO.getGroupName())
                        .shortGroupName(null == groupDTO ? null : groupDTO.getShortGroupName())
                        .salt(salt)
                        .passwd(passwd)
                        .build();
                list.add(pageRes);
            }
        }
        return new PageDTO<>(page.getPageNum(), page.getPageSize(), page.getTotalElements(), page.getTotalPages(), page.isLast(), page.isFirst(), list);
    }

    @Override
    public WithdrawalLedgerDetailRes withdrawalLedgerDetail(String withdrawalLedgerId, String entId, EmployeeWechatDTO dto, String salt, String passwd) {
        Optional.ofNullable(withdrawalLedgerId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????ID????????????")));

        //??????
        WithdrawalLedgerDTO ledgerDTO = withdrawalLedgerInfoServiceFeign.findById(withdrawalLedgerId);
        if (null == ledgerDTO) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("???????????????????????????"));
        }

        //??????
        String employeeWalletId = ledgerDTO.getEmployeeWalletId();
        EmployeeWalletDTO employeeWalletDTO = employeeWalletInfoServiceFeign.findById(employeeWalletId);
        if (null == employeeWalletDTO) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????"));
        }

        //????????????
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

            //?????????
            EmployeeCardQueryReq cardQueryReq = EmployeeCardQueryReq.builder()
                    .employeeIds(employeeIds)
                    .cardNo(employeeCardNo)
                    .build();
            List<core.dto.response.empCard.EmployeeCardDTO> cardDTOS = employeeCardFeignService.query(cardQueryReq);
            if (null != cardDTOS && cardDTOS.size() > 0) {
                employeeCardDTO = cardDTOS.get(0);
            }
        }

        //????????????
        String wageSheetId = ledgerDTO.getWageSheetId();
        WageSheetDTO wageSheetDTO = wageSheetFeignController.findById(wageSheetId);

        //????????????
        String accountId = ledgerDTO.getAccountId();
        EntAccountDTO entAccountDTO = entAccountInfoFeignService.findById(accountId);
        //????????????
        Integer accountStatus = 0;
        String accountStatusVal = "??????";
        if (null != entAccountDTO && AccountStatusEnum.FROZENFROZEN.getCode().equals(entAccountDTO.getAccountStatus())) {
            accountStatus = 1;
            accountStatusVal = "??????";
        }

        //?????????????????????????????????
        WithdrawalRecordLogQueryReq logQueryReq = WithdrawalRecordLogQueryReq.builder()
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
                .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                .build();
        List<WithdrawalRecordLogDTO> logDTOS = withdrawalRecordLogServiceFeign.list(logQueryReq);
        WithdrawalRecordLogDTO logDTO = null;
        if (null != logDTOS && logDTOS.size() > 0) {
            logDTO = logDTOS.get(0);
        }

        //??????
        EntErpriseInfoDTO erpriseInfoDTO = enterpriseFeignService.findById(entId);

        //????????????
        String groupName = null;
        ModelStatusEnum withdrawStatus = ModelStatusEnum.DISABLE;
        String groupId = ledgerDTO.getGroupId();
        GroupDTO groupDTO = groupFeignController.findById(groupId);
        groupName = groupDTO.getGroupName();

        GroupAttachInfoDTO groupAttachInfoDTO = groupAttachInfoServiceFeign.findGroupAttachById(groupId);
        if (null != groupAttachInfoDTO) {
            withdrawStatus = groupAttachInfoDTO.getWithdrawStatus();
        }

        return WithdrawalLedgerDetailRes.builder()
                .groupName(groupName)
                .walletNumber(EncrytorUtils.encryptField(employeeWalletDTO.getWalletNumber(), salt, passwd))
                .withdrawStatus(withdrawStatus.getCode())
                .withdrawStatusVal(withdrawStatus.getDesc())
                .year(ledgerDTO.getYear())
                .accountOpenBank(null == erpriseInfoDTO || null == erpriseInfoDTO.getLiquidation() ? null : erpriseInfoDTO.getLiquidation().getDesc())
                .withdrawalStatusVal(null == ledgerDTO.getWithdrawalStatus() ? null : ledgerDTO.getWithdrawalStatus().getDesc())
                .withdrawalStatus(null == ledgerDTO.getWithdrawalStatus() ? null : ledgerDTO.getWithdrawalStatus().getCode())
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
                .updDateTime(null == ledgerDTO.getUpdDateTime() ? null : ledgerDTO.getUpdDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .transAmount(EncrytorUtils.encryptField(ledgerDTO.getTransAmount().toString(), salt, passwd))
                .remark(ledgerDTO.getRemark())
                .payDateTime(null == ledgerDTO.getPayDateTime() ? null : ledgerDTO.getPayDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .month(ledgerDTO.getMonth())
                .issueTime(null == ledgerDTO.getIssueTime() ? null : ledgerDTO.getIssueTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .idNumber(EncrytorUtils.encryptField(ledgerDTO.getIdNumber(), salt, passwd))
                .fundTypeVal(null == wageSheetDTO ? null : wageSheetDTO.getFundTypeDesc())
                .fundType(null == wageSheetDTO ? null : wageSheetDTO.getFundType())
                .fundDateVal(null == wageSheetDTO ? null : wageSheetDTO.getFundDate().getDesc())
                .fundDate(null == wageSheetDTO ? null : wageSheetDTO.getFundDate().getCode())
                .entId(ledgerDTO.getEntId())
                .employeeCardNo(EncrytorUtils.encryptField(employeeCardNo, salt, passwd))
                .employeeCardStar(employeeCardNo.substring(0, 4) + "****" + employeeCardNo.substring(employeeCardNo.length() - 4, employeeCardNo.length()))
                .custName(EncrytorUtils.encryptField(ledgerDTO.getCustName(), salt, passwd))
                .crtDateTime(null == ledgerDTO.getCrtDateTime() ? null : ledgerDTO.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .accountStar(null == entAccountDTO ? null : entAccountDTO.getAccountStar())
                .accountName(null == entAccountDTO ? null : entAccountDTO.getAccountName())
                .account(null == entAccountDTO ? null : EncrytorUtils.encryptField(entAccountDTO.getAccount(), salt, passwd))
                .accountId(null == entAccountDTO ? null : entAccountDTO.getId())
                .accountStatus(accountStatus)
                .accountStatusVal(accountStatusVal)
                .salt(salt)
                .passwd(passwd)
                .withdrawalRecordLogId(ledgerDTO.getWithdrawalRecordLogId())
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
                .wageSheetName(null == wageSheetDTO ? null : wageSheetDTO.getWageName())
                .transNo(null == logDTO ? null : logDTO.getTransNo())
                .entName(null == erpriseInfoDTO ? null : erpriseInfoDTO.getEntName())
                .openBank(null == erpriseInfoDTO || null == erpriseInfoDTO.getLiquidation() ? null : erpriseInfoDTO.getLiquidation().getDesc())
                .entId(entId)
                .build();
    }

    @Override
    public WithdrawalRecordDetailRes withdrawalRecordDetail(String withdrawalLedgerId, String entId, EmployeeWechatDTO dto, String salt, String passwd) {
        //????????????
        WithdrawalLedgerDetailRes ledgerDetail = withdrawalLedgerDetail(withdrawalLedgerId, entId, dto, salt, passwd);

        //????????????
        chain.fxgj.server.payroll.dto.wallet.WithdrawalRecordLogDTO withdrawalRecordLogDTO = null;
        WithdrawalStatusEnum withdrawalStatusEnum = WithdrawalStatusEnum.values()[ledgerDetail.getWithdrawalStatus()];
        if (null != withdrawalStatusEnum && WithdrawalStatusEnum.Await != withdrawalStatusEnum && WithdrawalStatusEnum.TimeOut != withdrawalStatusEnum) {
            TransDealStatusEnum transDealStatusEnum = WithdrawalStatusEnum.Success == withdrawalStatusEnum ? TransDealStatusEnum.SUCCESS
                    : (WithdrawalStatusEnum.Fail == withdrawalStatusEnum ? TransDealStatusEnum.FAIL
                    : TransDealStatusEnum.ING);
            WithdrawalRecordLogQueryReq recordLogQueryReq = WithdrawalRecordLogQueryReq.builder()
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .withdrawalLedgerId(ledgerDetail.getWithdrawalLedgerId())
                    .transStatus(transDealStatusEnum)
                    .build();
            List<WithdrawalRecordLogDTO> list = withdrawalRecordLogServiceFeign.list(recordLogQueryReq);
            if (null != list && list.size() > 0) {
                WithdrawalRecordLogDTO logDTO = list.get(0);

                //????????????
                withdrawalRecordLogDTO = chain.fxgj.server.payroll.dto.wallet.WithdrawalRecordLogDTO.builder()
                        .updDateTime(null == logDTO.getUpdDateTime() ? null : logDTO.getUpdDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .withdrawalRecordLogId(logDTO.getWithdrawalRecordLogId())
                        .withdrawalLedgerId(logDTO.getWithdrawalLedgerId())
                        .transNo(logDTO.getTransNo())
                        .transStatus(logDTO.getTransStatus().getCode())
                        .transStatusVal(TransDealStatusEnum.SUCCESS == logDTO.getTransStatus() ? "????????????"
                                : (TransDealStatusEnum.FAIL == logDTO.getTransStatus() ? "????????????"
                                : (TransDealStatusEnum.ING == logDTO.getTransStatus() ? "???????????????" : "??????")))
                        .custName(ledgerDetail.getCustName())
                        .transAmount(null == logDTO.getTransAmount() ? null : EncrytorUtils.encryptField(logDTO.getTransAmount().toString(), salt, passwd))
                        .remark(logDTO.getRemark())
                        .predictDateTime(null == logDTO.getPredictDateTime() ? null : logDTO.getPredictDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .payDateTime(null == logDTO.getPayDateTime() ? null : logDTO.getPayDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .openBank(ledgerDetail.getOpenBank())
                        .failDesc(logDTO.getFailDesc())
                        .employeeCardNo(StringUtils.isBlank(logDTO.getEmployeeCardNo()) ? null : EncrytorUtils.encryptField(logDTO.getEmployeeCardNo().toString(), salt, passwd))
                        .employeeCardStar(StringUtils.isBlank(logDTO.getEmployeeCardNo()) ? null : logDTO.getEmployeeCardNo().substring(0, 4) + "****" + logDTO.getEmployeeCardNo().substring(logDTO.getEmployeeCardNo().length() - 4, logDTO.getEmployeeCardNo().length()))
                        .delStatus(logDTO.getTransStatus().getCode())
                        .crtDateTime(null == logDTO.getCrtDateTime() ? null : logDTO.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .applyDateTime(null == logDTO.getApplyDateTime() ? null : logDTO.getApplyDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .delStatusVal(logDTO.getEmployeeCardNo())
                        .salt(salt)
                        .passwd(passwd)
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
        //????????????
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

            //?????????
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
                            .cardNo(EncrytorUtils.encryptField(cardDTO.getCardNo(), salt, passwd))
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

        //????????????
        WithdrawalLedgerDTO ledgerDTO = withdrawalLedgerInfoServiceFeign.findById(withdrawalLedgerId);
        if (null == ledgerDTO) {
            log.info("=====> ????????????????????????????????????, withdrawalLedgerId:{}", withdrawalLedgerId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
        }
        switch (ledgerDTO.getWithdrawalStatus()) {
            case Ing:
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
            case Success:
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????????????????"));
            default:
                log.info("=====> ???????????? req???{}, ledgerDTO:{}", JsonUtil.objectToJson(req), JsonUtil.objectToJson(ledgerDTO));
                break;
        }

        //????????????
        LocalDateTime cutoffTime = ledgerDTO.getCutoffTime();
        LocalDateTime localDateTime = LocalDateTime.now();
        if (localDateTime.isAfter(cutoffTime)) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????????????????????????????"));
        }

        //??????????????????????????????????????????????????????????????????
        WithdrawalScheduleQueryReq scheduleQueryReq = WithdrawalScheduleQueryReq.builder()
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
                .withdrawalStatus(Arrays.asList(WithdrawalStatusEnum.Ing, WithdrawalStatusEnum.Success))
                .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                .build();
        List<WithdrawalScheduleDTO> scheduleDTOS = withdrawalScheduleFeignService.list(scheduleQueryReq);
        if (null != scheduleDTOS && scheduleDTOS.size() > 0) {
            log.info("====> ???????????????   scheduleDTOS:{}", JacksonUtil.objectToJson(scheduleDTOS));
            throw new ParamsIllegalException(chain.wage.core.constant.ErrorConstant.SYS_ERROR.format("???????????????"));
        }

        BigDecimal transAmount = ledgerDTO.getTransAmount();

        //??????????????????????????????
        String groupId = ledgerDTO.getGroupId();
        GroupAttachInfoDTO groupAttachInfoDTO = groupAttachInfoServiceFeign.findGroupAttachById(groupId);
        if (null == groupAttachInfoDTO || ModelStatusEnum.ENSABLE != groupAttachInfoDTO.getWalletStatus()) {
            log.info("=====> ??????????????????????????????????????? groupId:{}, groupAttachInfoDTO:{}", groupId, JsonUtil.objectToJson(groupAttachInfoDTO));
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("???????????????????????????????????????"));
        }

        //????????????
        String employeeWalletId = ledgerDTO.getEmployeeWalletId();
        EmployeeWalletDTO employeeWalletDTO = employeeWalletInfoServiceFeign.findById(employeeWalletId);
        if (null == employeeWalletDTO) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????"));
        }

        //????????????
        EmployeeQueryReq employeeQueryReq = EmployeeQueryReq.builder()
                .entId(entId)
                .idNumber(ledgerDTO.getIdNumber())
//                .employeeName(ledgerDTO.getCustName())
                .build();
        List<EmployeeDTO> employeeDTOList = employeeFeignController.query(employeeQueryReq);

        core.dto.response.empCard.EmployeeCardDTO employeeCardDTO = null;
        if (null != employeeDTOList && employeeDTOList.size() > 0) {
            List<String> employeeIds = new ArrayList<>();
            for (EmployeeDTO employeeDTO : employeeDTOList) {
                employeeIds.add(employeeDTO.getEmployeeId());
            }

            //?????????
            EmployeeCardQueryReq cardQueryReq = EmployeeCardQueryReq.builder()
                    .employeeIds(employeeIds)
                    .cardNo(cardNo)
                    .build();
            List<core.dto.response.empCard.EmployeeCardDTO> cardDTOS = employeeCardFeignService.query(cardQueryReq);
            if (null != cardDTOS && cardDTOS.size() > 0) {
                employeeCardDTO = cardDTOS.get(0);
            }
        }

        //??????????????????
        WithdrawalRecordLogSaveReq build = WithdrawalRecordLogSaveReq.builder()
                .applyDateTime(LocalDateTime.now())
                .delStatusEnum(DelStatusEnum.normal)
                .employeeCardNo(null == employeeCardDTO ? null : employeeCardDTO.getCardNo())
                .openBank(null == employeeCardDTO ? null : employeeCardDTO.getIssuerName())
                .transAmount(transAmount)
                .transStatus(TransDealStatusEnum.ING)
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
                .withdrawalMethod(WithdrawalMethodEnum.Manual)
//                .withdrawalRecordLogId()
//                .failDesc()
//                .payDateTime()
//                .predictDateTime()
//                .remark()
//                .transNo()
                .build();
        WithdrawalRecordLogDTO logDTO = withdrawalRecordLogServiceFeign.save(build);

        //??????????????????
        WithdrawalLedgerSaveReq ledgerSaveReq = WithdrawalLedgerSaveReq.builder()
                .withdrawalLedgerId(ledgerDTO.getWithdrawalLedgerId())
                .withdrawalStatus(WithdrawalStatusEnum.Ing)
                .build();
        if (!ledgerDTO.getEmployeeCardNo().equals(cardNo)) {
            ledgerSaveReq.setEmployeeCardNo(employeeCardDTO.getCardNo());
            ledgerSaveReq.setOpenBank(employeeCardDTO.getIssuerName());
        }
        WithdrawalLedgerDTO ledgerDTO1 = withdrawalLedgerInfoServiceFeign.save(ledgerSaveReq);

        //????????????????????????
        core.dto.response.employeeWallet.WalletBalanceDTO balance = withdrawalLedgerInfoServiceFeign.balance(employeeWalletId);

        //??????????????????
        EmployeeWalletSaveReq walletSaveReq = EmployeeWalletSaveReq.builder()
                .totalAmount(balance.getTotalAmount())
                .frozenAmount(balance.getFrozenAmount())
                .availableAmount(balance.getAvailableAmount())
                .employeeWalletId(employeeWalletId)
                .build();
        EmployeeWalletDTO walletDTO = employeeWalletInfoServiceFeign.save(walletSaveReq);

        //???????????????????????????????????????
        WithdrawalScheduleSaveReq saveReq = WithdrawalScheduleSaveReq.builder()
                .delStatusEnum(DelStatusEnum.normal)
                .withdrawalLedgerId(ledgerDTO1.getWithdrawalLedgerId())
                .withdrawalRecordLogId(logDTO.getWithdrawalRecordLogId())
                .withdrawalStatus(WithdrawalStatusEnum.Ing)
                .wageSheetId(ledgerDTO1.getWageSheetId())
                .build();
        withdrawalScheduleFeignService.save(saveReq);
    }

    /**
     * ??????????????????
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

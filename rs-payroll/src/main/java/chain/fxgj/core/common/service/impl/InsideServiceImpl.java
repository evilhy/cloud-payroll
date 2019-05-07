package chain.fxgj.core.common.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.constant.DictEnums.CardUpdStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.ReceiptsStatusEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.InsideService;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.dto.request.ReadWageDTO;
import chain.fxgj.server.payroll.dto.request.ResReceiptDTO;
import chain.fxgj.server.payroll.dto.request.UpdBankCardDTO;
import chain.fxgj.server.payroll.dto.response.BankCardGroup;
import chain.utils.commons.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class InsideServiceImpl implements InsideService {

    @Autowired
    WageDetailInfoDao wageDetailInfoDao;
    @Autowired
    WageSheetInfoDao wageSheetInfoDao;
    @Autowired
    WageReceiptMsgDao wageReceiptMsgDao;
    @Autowired
    EmployeeWechatInfoDao employeeWechatInfoDao;
    @Autowired
    EmployeeInfoDao employeeInfoDao;
    @Autowired
    EmployeeCardInfoDao employeeCardInfoDao;
    @Autowired
    EmployeeCardLogDao employeeCardLogDao;


    @Autowired
    EmployeeEncrytorService employeeEncrytorService;

    @Override
    public void recepitConfirm(ResReceiptDTO resReceiptDTO) {
        //判断工资状态
        WageDetailInfo wageDetailInfo = wageDetailInfoDao.findById(resReceiptDTO.getWageDetailId())
                .orElseThrow(() -> new ParamsIllegalException(ErrorConstant.Error0001.format("工资")));

        //回执状态 0确认无误,1有反馈信息,2已沟通 3未确认
        if (wageDetailInfo.getReceiptsStatus().equals(ReceiptsStatusEnum.CONFIRMED)) {
            throw new ParamsIllegalException(ErrorConstant.RECEPIT_001.getErrorMsg());
        }

        //修改回执状态
        QWageDetailInfo qWageDetailInfo = QWageDetailInfo.wageDetailInfo;
        wageDetailInfoDao.update(qWageDetailInfo).set(qWageDetailInfo.receiptsStatus, ReceiptsStatusEnum.values()[resReceiptDTO.getReceiptsStatus()])
                .where(qWageDetailInfo.id.eq(resReceiptDTO.getWageDetailId())).execute();

        //确认无误
        if (resReceiptDTO.getReceiptsStatus().equals(ReceiptsStatusEnum.CONFIRMED.getCode())) {
            //方案添加回执数
            wageSheetInfoDao.updateRecepitCnt(wageDetailInfo.getWageSheetId());
        }

        if (null != resReceiptDTO.getMsg() && !"".equals(resReceiptDTO.getMsg())) {
            //添加回执记录
            WageReceiptMsg wageReceiptMsg = new WageReceiptMsg();
            wageReceiptMsg.setEmployeeSid(wageDetailInfo.getEmployeeSid());
            wageReceiptMsg.setWageSheetId(wageDetailInfo.getWageSheetId());
            wageReceiptMsg.setWageDetailId(wageDetailInfo.getId());
            wageReceiptMsg.setGroupId(wageDetailInfo.getGroupId());
            wageReceiptMsg.setEntId(wageDetailInfo.getEntId());
            wageReceiptMsg.setMsg(resReceiptDTO.getMsg());
            wageReceiptMsg.setCrtDateTime(LocalDateTime.now());
            wageReceiptMsgDao.saveAndFlush(wageReceiptMsg);
        }
    }

    @Override
    public void bandWechat(String openId, String idNumber, String phone) {
        log.info("绑定信息openId:{},idNumber:{},phone:{}", openId, idNumber, phone);
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;

        EmployeeWechatInfo employeeWechatInfo = employeeWechatInfoDao.selectFrom(qEmployeeWechatInfo)
                .where(qEmployeeWechatInfo.idNumber.equalsIgnoreCase(idNumber)
                        .and(qEmployeeWechatInfo.openId.eq(openId))
                )
                .fetchFirst();
        if (employeeWechatInfo == null) {
            employeeWechatInfo = EmployeeWechatInfo.builder().build();
            employeeWechatInfo.setCrtDateTime(LocalDateTime.now());
        }

        //添加绑定数据
        employeeWechatInfo.setOpenId(openId);
        employeeWechatInfo.setIdNumber(idNumber);
        employeeWechatInfo.setPhone(employeeEncrytorService.encryptPhone(phone));
        employeeWechatInfo.setDelStatusEnum(DelStatusEnum.normal);
        employeeWechatInfo.setUpdDateTime(LocalDateTime.now());
        employeeWechatInfo.setQueryPwd(null);
        log.info("开始插入！");
        employeeWechatInfoDao.save(employeeWechatInfo);
        log.info("插入完成");
    }

    @Override
    public void bandWechatAndPhone(String openId, String idNumber, String phone, String pwd) {
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
        log.info("绑定信息openId:{},idNumber:{},phone:{}", openId, idNumber, phone);
        EmployeeWechatInfo employeeWechatInfo = employeeWechatInfoDao.selectFrom(qEmployeeWechatInfo)
                .where(qEmployeeWechatInfo.idNumber.equalsIgnoreCase(idNumber).and(qEmployeeWechatInfo.openId.eq(openId)))
                .fetchFirst();
        if (employeeWechatInfo == null) {
            employeeWechatInfo = EmployeeWechatInfo.builder().build();
            employeeWechatInfo.setCrtDateTime(LocalDateTime.now());
        }

        //添加绑定数据
        employeeWechatInfo.setOpenId(openId);
        employeeWechatInfo.setIdNumber(employeeEncrytorService.encryptIdNumber(idNumber));
        employeeWechatInfo.setPhone(employeeEncrytorService.encryptPhone(phone));
        employeeWechatInfo.setDelStatusEnum(DelStatusEnum.normal);
        employeeWechatInfo.setUpdDateTime(LocalDateTime.now());
        employeeWechatInfo.setQueryPwd(employeeEncrytorService.encryptPwd(pwd));
        employeeWechatInfoDao.save(employeeWechatInfo);

        //更新手机号
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;
        List<EmployeeInfo> fetch = employeeInfoDao.selectFrom(qEmployeeInfo)
                .where(qEmployeeInfo.idNumber.eq(idNumber)
                        .and(qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal)))
                .fetch();
        log.info("员工的size:{}", fetch.size());
        for (EmployeeInfo employeeInfo : fetch) {
            employeeInfo.setPhone(phone);
        }
    }

    @Override
    public void readWage(ReadWageDTO readWageDTO) {
        QWageDetailInfo qWageDetailInfo = QWageDetailInfo.wageDetailInfo;
        List<WageDetailInfo> fetch = wageDetailInfoDao.selectFrom(qWageDetailInfo)
                .where(qWageDetailInfo.wageSheetId.eq(readWageDTO.getWageSheetId())
                        .and(qWageDetailInfo.idNumber.eq(readWageDTO.getIdNumber()))
                        .and(qWageDetailInfo.isRead.eq(IsStatusEnum.NO)))
                .fetch();
        if (fetch.size() > 0) {
            for (WageDetailInfo wageDetailInfo : fetch) {
                wageDetailInfo.setIsRead(IsStatusEnum.YES);
            }
        }
    }

    @Override
    public void login(String openId, String jsessionId, String nickname, String headimgurl) {
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
        employeeWechatInfoDao.update(qEmployeeWechatInfo)
                .set(qEmployeeWechatInfo.nickname, nickname)
                .set(qEmployeeWechatInfo.headimgurl, headimgurl)
                .set(qEmployeeWechatInfo.jsessionId, jsessionId)
                .set(qEmployeeWechatInfo.updDateTime, LocalDateTime.now())
                .where(qEmployeeWechatInfo.openId.eq(openId)).execute();
    }

    @Override
    public void setPwd(String wechatId, String pwd) {
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
        employeeWechatInfoDao.update(qEmployeeWechatInfo).set(qEmployeeWechatInfo.queryPwd, employeeEncrytorService.encryptPwd(pwd))
                .where(qEmployeeWechatInfo.id.eq(wechatId)).execute();
    }

    @Override
    public void updPhone(String wechatId, String idNumber, String phone) {
        //更新手机号
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
        employeeWechatInfoDao.update(qEmployeeWechatInfo).set(qEmployeeWechatInfo.phone, employeeEncrytorService.encryptPhone(phone))
                .where(qEmployeeWechatInfo.id.eq(wechatId)).execute();

        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;
        employeeInfoDao.update(qEmployeeInfo).set(qEmployeeInfo.phone, phone)
                .where(qEmployeeInfo.idNumber.eq(idNumber).and(qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal))).execute();
    }

    @Override
    public void updBankCard(UpdBankCardDTO updBankCardDTO) {

        //创建联合id
        String unionId = UUIDUtil.createUUID32();
        List<EmployeeCardLog> employeeCardLogs = new ArrayList<>();
        for (BankCardGroup item : updBankCardDTO.getBankCardGroups()) {
            EmployeeCardInfo employeeCardInfo = employeeCardInfoDao.findById(item.getId())
                    .orElseThrow(() -> new ParamsIllegalException(ErrorConstant.Error0001.format("银行卡")));

            //银行卡修改记录
            EmployeeCardLog employeeCardLog = new EmployeeCardLog();
            employeeCardLog.setUnionId(unionId);
            employeeCardLog.setCardNo(updBankCardDTO.getCardNo());
            employeeCardLog.setIssuerBankId(updBankCardDTO.getIssuerBankId());
            employeeCardLog.setIssuerName(updBankCardDTO.getIssuerName());
            employeeCardLog.setCardNoOld(employeeCardInfo.getCardNo());
            employeeCardLog.setIssuerNameOld(employeeCardInfo.getIssuerName());
            employeeCardLog.setCrtDateTime(LocalDateTime.now());
            employeeCardLog.setUpdStatus(CardUpdStatusEnum.UNKOWN);
            employeeCardLog.setGroupId(item.getGroupId());
            employeeCardLog.setIsNew(IsStatusEnum.NO);
            employeeCardLog.setBankCardId(item.getId());
            employeeCardLog.setDelStatus(DelStatusEnum.normal);
            employeeCardLogs.add(employeeCardLog);

            //修改银行卡
//            employeeCardInfo.setCardNo(updBankCardDTO.getCardNo());
//            employeeCardInfo.setIssuerBankId(updBankCardDTO.getIssuerBankId());
//            employeeCardInfo.setIssuerName(updBankCardDTO.getIssuerName());
//            employeeCardInfo.setUpdDateTime(LocalDateTime.now());
        }
        employeeCardLogDao.saveAll(employeeCardLogs);

    }

    @Override
    public void bankCardIsNew(List<String> logIds) {
        List<EmployeeCardLog> allById = employeeCardLogDao.findAllById(logIds);
        for (EmployeeCardLog employeeCardLog : allById) {
            employeeCardLog.setIsNew(IsStatusEnum.NO);
        }
    }




}

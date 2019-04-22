package chain.fxgj.core.common.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.PayStatusEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.WageWechatService;
import chain.fxgj.core.common.util.TransUtil;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.dto.EmployeeDTO;
import chain.fxgj.server.payroll.dto.response.*;
import chain.utils.commons.JacksonUtil;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@SuppressWarnings("unchecked")
public class WageWechatServiceImpl implements WageWechatService {

    @Autowired
    WageDetailInfoDao wageDetailInfoDao;
    @Autowired
    WageSheetInfoDao wageSheetInfoDao;
    @Autowired
    EntErpriseInfoDao entErpriseInfoDao;
    @Autowired
    EntGroupInfoDao entGroupInfoDao;
    @Autowired
    EmployeeInfoDao employeeInfoDao;

    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    WageFundTypeInfoDao wageFundTypeInfoDao;


    @Override
    public NewestWageLogDTO newGroupPushInfo(String idNumber) {
        NewestWageLogDTO bean = new NewestWageLogDTO();
        //用户最新一条推送记录
        List<NewestWageLogDTO> list = this.groupList(idNumber);
        if (list != null && list.size() > 0) {
            bean = list.get(0);
        }

        return bean;
    }

    @Override
    public List<NewestWageLogDTO> groupList(String idNumber) {
        List<EmployeeDTO> employeeDTOS = empWechatService.getEmpList(idNumber);
        log.info("employeeDTOS:[{}]",JacksonUtil.objectToJson(employeeDTOS));
        QWageDetailInfo qWageDetailInfo = QWageDetailInfo.wageDetailInfo;
        List<NewestWageLogDTO> list = new ArrayList<>();
        for (EmployeeDTO employeeDTO : employeeDTOS) {
            //根据最新的代发记录
            WageDetailInfo wageDetailInfo = wageDetailInfoDao.selectFrom(qWageDetailInfo)
                    .where(qWageDetailInfo.employeeSid.eq(employeeEncrytorService.encryptEmployeeId(employeeDTO.getEmployeeId()))
                            .and(qWageDetailInfo.isCountStatus.eq(IsStatusEnum.YES)))
                    .orderBy(qWageDetailInfo.cntDateTime.desc()).fetchFirst();

            if (wageDetailInfo != null) {
                NewestWageLogDTO bean = new NewestWageLogDTO(employeeDTO);
                bean.setCreateDate(wageDetailInfo.getCntDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                bean.setIsRead(wageDetailInfo.getIsRead().getCode() + "");

                list.add(bean);
            }
        }

        Collections.sort(list, new Comparator<NewestWageLogDTO>() {
            @Override
            public int compare(NewestWageLogDTO o1, NewestWageLogDTO o2) {
                return o2.getCreateDate().compareTo(o1.getCreateDate());
            }
        });

        return list;
    }

    @Override
    public List<WageDetailDTO> getWageDetail(String idNumber, String groupId, String wageSheetId) {
        //查询员工id
        EmployeeDTO employee = null;
        List<EmployeeDTO> employeeDTOList = empWechatService.getEmpList(idNumber);
        for (EmployeeDTO employeeDTO : employeeDTOList) {
            if (employeeDTO.getGroupId().equals(groupId)) {
                employee = employeeDTO;
            }
        }
        if (employee == null) {
            log.info("员工信息为空");
            throw new ParamsIllegalException(ErrorConstant.Error0001.format("员工机构"));
        }
        String employeeSid = employeeEncrytorService.encryptEmployeeId(employee.getEmployeeId());
        log.info("employeeSid={}", employeeSid);
        QWageDetailInfo qWageDetailInfo = QWageDetailInfo.wageDetailInfo;
        BooleanExpression booleanExpression = qWageDetailInfo.employeeSid.eq(employeeSid)
                .and(qWageDetailInfo.isCountStatus.eq(IsStatusEnum.YES));
        //员工方案对应的代发明细
        List<WageDetailInfo> wageDetailInfos = wageDetailInfoDao.selectFrom(qWageDetailInfo)
                .where(booleanExpression.and(qWageDetailInfo.wageSheetId.eq(wageSheetId))).fetch();
        log.info("wageDetailInfos.size()={}", wageDetailInfos.size());
        List<WageDetailDTO> list = new ArrayList<>();
        if (wageDetailInfos != null && wageDetailInfos.size() > 0) {
            WageSheetInfo wageSheetInfo = wageDetailInfos.get(0).getWageSheetInfo();

            //机构
            String groupName = employee.getGroupName();
            //企业
            String entName = employee.getEntName();

            //查询上次方案
            String last_sheet_id = wageDetailInfoDao.select(qWageDetailInfo.wageSheetId).from(qWageDetailInfo)
                    .where(booleanExpression.and(qWageDetailInfo.cntDateTime.before(wageDetailInfos.get(0).getCntDateTime()))
                            .and(qWageDetailInfo.wageSheetId.ne(wageSheetId))).orderBy(qWageDetailInfo.crtDateTime.desc()).fetchFirst();
            //查询上次方案金额
            BigDecimal lastAmt = new BigDecimal(0);
            if (last_sheet_id != null && !last_sheet_id.equals("")) {
                lastAmt = wageDetailInfoDao.select(qWageDetailInfo.realTotalAmt.sum()).from(qWageDetailInfo)
                        .where(booleanExpression.and(qWageDetailInfo.wageSheetId.eq(last_sheet_id))).fetchFirst();
            }
            //当前方案金额
            BigDecimal amt = BigDecimal.ZERO;
            for (WageDetailInfo wageDetailInfo : wageDetailInfos) {
                amt = amt.add(wageDetailInfo.getRealTotalAmt());
            }

            BigDecimal differRealAmt = amt.subtract(lastAmt);

            //方案展示
            WageShowInfo wageShowInfo = wageSheetInfo.getWageShowInfo();

            for (WageDetailInfo wageDetailInfo : wageDetailInfos) {
                if (wageDetailInfo.getIsCountStatus().equals(IsStatusEnum.YES)) {
                    WageDetailDTO wageDetailDTO = new WageDetailDTO(wageDetailInfo);
                    wageDetailDTO.setWageName(wageSheetInfo.getWageName());
                    wageDetailDTO.setGroupName(groupName);
                    wageDetailDTO.setEntName(entName);
                    wageDetailDTO.setCardNo(TransUtil.bankCardStar(employeeEncrytorService.decryptCardNo(wageDetailInfo.getBankCard())));
                    List<WageDetailDTO.Content> contents = null;
                    try {
                        contents = JacksonUtil.jsonToList(employeeEncrytorService.decryptContent(wageDetailInfo.getContent()), WageDetailDTO.Content.class);
                    } catch (Exception e) {
                        contents = JacksonUtil.jsonToList(wageDetailInfo.getContent(), WageDetailDTO.Content.class);
                    }
                    wageDetailDTO.setContent(contents);

                    wageDetailDTO.setWageHeadDTO(JacksonUtil.jsonToBean(wageShowInfo.getHeads(), WageHeadDTO.class));
                    wageDetailDTO.setWageShowDTO(new WageDetailDTO.WageShowDTO(wageShowInfo));

                    wageDetailDTO.setDifferRealAmt(differRealAmt);

                    wageDetailDTO.setPayStatus(wageDetailInfo.getPayStatus().equals(PayStatusEnum.SUCCESS) ? "1" : "0");

                    list.add(wageDetailDTO);
                }
            }
        }
        log.info("list.size()={}", list.size());
        return list;
    }

    @Override
    public Res100712 wageTrend(String openId, String idNumber) {
        return null;
    }

    @Override
    public Res100703 wageList(String idNumber, String groupId, String year, String type) {
        //查询员工id
        EmployeeDTO employee = null;
        List<EmployeeDTO> employeeDTOList = empWechatService.getEmpList(idNumber);
        for (EmployeeDTO employeeDTO : employeeDTOList) {
            if (employeeDTO.getGroupId().equals(groupId)) {
                employee = employeeDTO;
            }
        }
        if (employee == null) {
            throw new ParamsIllegalException(ErrorConstant.Error0001.format("员工机构"));
        }

        //查询发放记录
        QWageDetailInfo qWageDetailInfo = QWageDetailInfo.wageDetailInfo;
        String employeeSid=employeeEncrytorService.encryptEmployeeId(employee.getEmployeeId());
        BooleanExpression booleanExpression = qWageDetailInfo.employeeSid.eq(employeeSid)
                .and(qWageDetailInfo.isCountStatus.eq(IsStatusEnum.YES));
        if (null != type && type.equals("0")) {
            booleanExpression = booleanExpression.and(qWageDetailInfo.payStatus.eq(PayStatusEnum.SUCCESS));
        }

        List<Tuple> tuples = wageDetailInfoDao.select(qWageDetailInfo.wageSheetId,
                qWageDetailInfo.realTotalAmt.sum(), qWageDetailInfo.deductTotalAmt.sum(), qWageDetailInfo.shouldTotalAmt.sum()).from(qWageDetailInfo)
                .where(booleanExpression.and(qWageDetailInfo.cntDateTime.year().eq(Integer.parseInt(year))))
                .groupBy(qWageDetailInfo.wageSheetId).fetch();

        BigDecimal shouldTotalAmt = new BigDecimal(0);
        BigDecimal deductTotalAmt = new BigDecimal(0);
        BigDecimal realTotalAmt = new BigDecimal(0);
        List<PlanListBean> planList = new ArrayList<>();
        for (Tuple tuple : tuples) {
            PlanListBean bean = new PlanListBean(tuple.get(qWageDetailInfo.wageSheetId), tuple.get(qWageDetailInfo.realTotalAmt.sum()));
            //方案信息
            WageSheetInfo wageSheetInfo = wageSheetInfoDao.findById(bean.getWageSheetId()).get();
            bean.setSpName(wageSheetInfo.getWageName());
            Integer fundType = wageSheetInfo.getFundType();
            WageFundTypeInfo wageFundTypeInfo = wageFundTypeInfoDao.findById(fundType).get();
            bean.setFundType(wageFundTypeInfo.getFundTypeVal());
            if (wageFundTypeInfo.getId() == 1 || wageFundTypeInfo.getId() == 2 || wageFundTypeInfo.getId() == 3 || wageFundTypeInfo.getId() == 15) {
                bean.setSpTypeIcon(0);
            }
            else if (wageFundTypeInfo.getId() == 10 || wageFundTypeInfo.getId() == 11 || wageFundTypeInfo.getId() == 12 || wageFundTypeInfo.getId() == 13) {
                bean.setSpTypeIcon(2);
            }
            else{
                bean.setSpTypeIcon(1);
            }

            //查询工资明细
            List<WageDetailInfo> wageDetailInfos = wageDetailInfoDao.select(qWageDetailInfo).from(qWageDetailInfo)
                    .where(booleanExpression.and(qWageDetailInfo.wageSheetId.eq(bean.getWageSheetId()))).fetch();
            int payCnt = 0;
            for (WageDetailInfo wageDetailInfo : wageDetailInfos) {
                if (wageDetailInfo.getPayStatus().equals(PayStatusEnum.SUCCESS)) {
                    payCnt = payCnt + 1;
                }
            }
            if (payCnt == 0)
                bean.setPayStatus("0");
            else if (payCnt == wageDetailInfos.size())
                bean.setPayStatus("1");
            else
                bean.setPayStatus("2");
            bean.setPayCnt(payCnt);

            long date = wageSheetInfo.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (wageDetailInfos.size() > 0) {
                date = wageDetailInfos.get(0).getCntDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            bean.setCreateDateTime(date);


            bean.setEmployeeId(employee.getEmployeeId());
            planList.add(bean);
            shouldTotalAmt = shouldTotalAmt.add(tuple.get(qWageDetailInfo.shouldTotalAmt.sum())==null?BigDecimal.ZERO:tuple.get(qWageDetailInfo.shouldTotalAmt.sum()));
            deductTotalAmt = deductTotalAmt.add(tuple.get(qWageDetailInfo.deductTotalAmt.sum())==null?BigDecimal.ZERO:tuple.get(qWageDetailInfo.deductTotalAmt.sum()));
            realTotalAmt = realTotalAmt.add(tuple.get(qWageDetailInfo.realTotalAmt.sum())==null?BigDecimal.ZERO:tuple.get(qWageDetailInfo.realTotalAmt.sum()));
        }

        Collections.sort(planList, new Comparator<PlanListBean>() {
            @Override
            public int compare(PlanListBean o1, PlanListBean o2) {
                return o2.getCreateDateTime().compareTo(o1.getCreateDateTime());
            }
        });



        Res100703 res100703 = new Res100703();
        res100703.setPlanList(planList);
        res100703.setRealTotalAmt(realTotalAmt);
        res100703.setDeductTotalAmt(deductTotalAmt);
        if (deductTotalAmt.compareTo(BigDecimal.ZERO) == 1) { //扣除金额大于0
            res100703.setShouldTotalAmt(realTotalAmt.add(deductTotalAmt));
        } else {
            res100703.setShouldTotalAmt(realTotalAmt.subtract(deductTotalAmt));
        }
        res100703.setEmployeeSid(employeeSid);

        return res100703;
    }

    @Override
    public List<Integer> years(String employeeSid, String type) {
        QWageDetailInfo qWageDetailInfo = QWageDetailInfo.wageDetailInfo;
        BooleanExpression booleanExpression = qWageDetailInfo.employeeSid.eq(employeeSid)
                .and(qWageDetailInfo.isCountStatus.eq(IsStatusEnum.YES));
        if (null != type && type.equals("0")) {
            booleanExpression = booleanExpression.and(qWageDetailInfo.payStatus.eq(PayStatusEnum.SUCCESS));
        }
        //查询员工已推送的年份
        List<Integer> years = wageDetailInfoDao.select(qWageDetailInfo.crtDateTime.year()).from(qWageDetailInfo)
                .where(booleanExpression).groupBy(qWageDetailInfo.crtDateTime.year()).fetch();

        return years;
    }

    @Override
    public Res100703 wageHistroyList(String idNumber, String groupId, String year, String type) {
        return this.wageList(idNumber, groupId, year, type);
    }

}

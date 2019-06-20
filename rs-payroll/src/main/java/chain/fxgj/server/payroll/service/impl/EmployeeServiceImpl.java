package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import chain.fxgj.core.jpa.dao.EmployeeInfoDao;
import chain.fxgj.core.jpa.model.EmployeeInfo;
import chain.fxgj.core.jpa.model.QEmployeeInfo;
import chain.fxgj.server.payroll.service.EmployeeService;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Future;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    EmployeeInfoDao employeeInfoDao;


    /**
     * 根据证件号码
     * 不区分 删除状态
     *
     * @param idNumber
     * @return
     */
    @Override
    public Future<EmployeeInfo> getEmployeeInfoOne(String idNumber,List<FundLiquidationEnum> dataAuths) {
        List<EmployeeInfo> employeeInfoList = this.queryEmployeeInfos(idNumber, null, Long.valueOf(0), Long.valueOf(1),dataAuths);
        if (employeeInfoList != null && employeeInfoList.size() > 0) {
            return new AsyncResult<>(employeeInfoList.get(0));
        }
        return new AsyncResult<>(null);
    }


    /**
     * 根据证件号码  和  删除状态 查询
     *
     * @param idNumber
     * @param delStatusEnum
     * @return
     */
    @Override
    public Future<List<EmployeeInfo>> getEmployeeInfos(String idNumber, DelStatusEnum[] delStatusEnum, Long offset, Long limit,List<FundLiquidationEnum> dataAuths) {
        List<EmployeeInfo> employeeInfoList = this.queryEmployeeInfos(idNumber, delStatusEnum, offset, limit,dataAuths);
        return new AsyncResult<>(employeeInfoList);
    }


    private List<EmployeeInfo> queryEmployeeInfos(String idNumber, DelStatusEnum[] delStatusEnum, Long offset, Long limit,List<FundLiquidationEnum> dataAuths) {
        //查询员工信息
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;
        //查询条件
        //[1] 身份证
        Predicate predicate = qEmployeeInfo.idNumber.eq(idNumber);

        //[2] 用户 删除 状态
        if (delStatusEnum != null && delStatusEnum.length > 0) {
            if (delStatusEnum.length == 1) {
                predicate = ExpressionUtils.and(predicate, qEmployeeInfo.delStatusEnum.eq(delStatusEnum[0]));
            } else {
                predicate = ExpressionUtils.and(predicate, qEmployeeInfo.delStatusEnum.in(delStatusEnum));
            }
        }

        //[3] 用户查询权限

        //删除状态
        OrderSpecifier orderDel = qEmployeeInfo.delStatusEnum.asc();
        //创建日期升序
        OrderSpecifier orderCrtDateTime = qEmployeeInfo.crtDateTime.asc();
        List<EmployeeInfo> employeeInfoList = null;
        if (offset != null && limit != null) {
            log.info("====>分页查询，{}，{}", offset, limit);
            employeeInfoList = employeeInfoDao.selectFrom(qEmployeeInfo)
                    .where(predicate)
                    .offset(offset.longValue())
                    .limit(limit.longValue())
                    .orderBy(orderDel, orderCrtDateTime)
                    .fetch();
        } else {
            employeeInfoDao.selectFrom(qEmployeeInfo)
                    .where(predicate)
                    .orderBy(orderDel, orderCrtDateTime)
                    .fetch();
        }

        log.info("====>根据证件号码，查询存在员工数据量={}", employeeInfoList.size());
        return employeeInfoList;
    }
}

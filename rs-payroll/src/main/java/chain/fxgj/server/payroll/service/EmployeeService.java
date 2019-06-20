package chain.fxgj.server.payroll.service;

import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import chain.fxgj.core.jpa.model.EmployeeInfo;

import java.util.List;
import java.util.concurrent.Future;

public interface EmployeeService {

    /**
     * 根据证件号码
     * 不区分 删除状态
     *
     * @param idNumber
     * @return
     */
    Future<EmployeeInfo> getEmployeeInfoOne(String idNumber,List<FundLiquidationEnum> dataAuths);


    /**
     * 根据证件号码  和  删除状态 查询
     *
     * @param idNumber
     * @param delStatusEnum
     * @param offset        起始页
     * @param limit         每页数量
     * @return
     */
    Future<List<EmployeeInfo>> getEmployeeInfos(String idNumber, DelStatusEnum[] delStatusEnum, Long offset, Long limit,List<FundLiquidationEnum> dataAuths);

}

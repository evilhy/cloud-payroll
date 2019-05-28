package chain.fxgj.server.payroll.service;

import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.jpa.model.EmployeeInfo;

import java.util.List;
import java.util.concurrent.Future;

public interface EmployeeService {


    /**
     * 根据证件号码
     *
     * @param idNumber
     * @param delStatusEnum
     * @return
     */
    Future<List<EmployeeInfo>> getEmployeeInfos(String idNumber, DelStatusEnum[] delStatusEnum);

}

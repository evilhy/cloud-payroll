package chain.fxgj.core.common.service;

import chain.fxgj.server.payroll.dto.response.ManagerInfoDTO;

public interface ManagerService {
    /**
     * 根据员工身份证号查询员工所在企业的客户经理信息
     *
     * @param idNumber 身份证号
     * @return
     */
    ManagerInfoDTO managerInfoByIdNumber(String idNumber);

    /**
     * 通知企业分配客户经理
     *
     * @param entId
     */
    void noticEnterprise(String entId);
}

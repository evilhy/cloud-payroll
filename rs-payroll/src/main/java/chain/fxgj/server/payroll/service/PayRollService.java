package chain.fxgj.server.payroll.service;

import chain.fxgj.server.payroll.dto.response.NewestWageLogDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;

import java.util.List;

/**
 * @Description:
 * @Author: du
 * @Date: 2020/8/12 21:19
 */
public interface PayRollService {

    List<NewestWageLogDTO> groupList(String entId, String groupId, String idNumber, UserPrincipal userPrincipal);
}

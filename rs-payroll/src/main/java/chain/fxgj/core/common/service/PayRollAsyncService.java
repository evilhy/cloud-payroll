package chain.fxgj.core.common.service;

import chain.fxgj.server.payroll.dto.EventDTO;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.dto.request.ReadWageDTO;
import chain.fxgj.server.payroll.dto.request.WechatLoginDTO;
import chain.fxgj.server.payroll.dto.response.EntInfoRes;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by syd on 2018/9/13.
 */
@Transactional
public interface PayRollAsyncService {

    /**
     * 修改绑定信息
     *
     * @param wechatLoginDTO
     * @return
     */
    Future<Response> updBindingInfo(WechatLoginDTO wechatLoginDTO);

    /**
     * 最新的企业信息
     *
     * @param idNumber
     * @return
     */
    Future<EntInfoRes> getNewestEntInfo(String idNumber);

    Future<Response> eventHandle(EventDTO eventDTO);

    /**
     * 已读工资条
     *
     * @param readWageDTO
     * @return
     */
    Future<Response> readWage(ReadWageDTO readWageDTO);

    /**
     * 员工机构
     */
    Future<List<EntInfoDTO>> getGroups(String idNumber);
}

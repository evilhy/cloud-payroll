package chain.fxgj.server.payroll.service;

import chain.fxgj.server.payroll.dto.tax.*;

import java.util.List;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/23 11:46
 */
public interface TaxService {

    void file() throws Exception;

    /**
     * 用户签约h5
     *
     * @param req
     */
    WalletH5Res walletH5(WalletH5Req req) throws Exception;

    /**
     * 用户签约记录
     *
     * @param transUserId
     */
    String sealH5(String transUserId) throws Exception;

    /**
     * 推送用户实名认证信息至零工平台
     *
     * @param req
     * @return
     */
    SealUserRes user(SealUserReq req) throws Exception;

    /**
     * 获取服务机构用工单位可签约的协议模板
     *
     * @param req
     * @return
     * @throws Exception
     */
    List<TempListRes> tempList(TempListReq req) throws Exception;
}

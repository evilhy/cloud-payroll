package chain.fxgj.server.payroll.service;

import chain.fxgj.server.payroll.dto.tax.SealUserReq;
import chain.fxgj.server.payroll.dto.tax.SealUserRes;
import chain.fxgj.server.payroll.dto.tax.WalletH5Req;
import chain.fxgj.server.payroll.dto.tax.WalletH5Res;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/23 11:46
 */
public interface TaxService {

    void file();

    /**
     * 用户签约h5
     *
     * @param req
     */
    WalletH5Res walletH5(WalletH5Req req);

    /**
     * 用户签约记录
     *
     * @param transUserId
     */
    String sealH5(String transUserId);

    /**
     * 推送用户实名认证信息至零工平台
     *
     * @param req
     * @return
     */
    SealUserRes user(SealUserReq req);
}

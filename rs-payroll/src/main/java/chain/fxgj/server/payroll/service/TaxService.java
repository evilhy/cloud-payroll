package chain.fxgj.server.payroll.service;

import java.util.List;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/23 11:46
 */
public interface TaxService {

    /**
     * 用户签约h5
     *
     * @param req
     */
    chain.cloud.tax.dto.fxgj.WalletH5Res walletH5(chain.cloud.tax.dto.fxgj.WalletH5Req req) throws Exception;

    /**
     * 用户签约记录
     *
     * @param req
     * @return
     */
    chain.cloud.tax.dto.fxgj.SealH5Res sealH5(chain.cloud.tax.dto.fxgj.SealH5Req req) throws Exception;

    /**
     * 推送用户实名认证信息至零工平台
     *
     * @param req
     * @return
     */
    chain.cloud.tax.dto.fxgj.SealUserRes user(chain.cloud.tax.dto.fxgj.SealUserReq req) throws Exception;

    /**
     * 获取服务机构用工单位可签约的协议模板
     *
     * @param req
     * @return
     * @throws Exception
     */
    List<chain.cloud.tax.dto.fxgj.TempListRes> tempList(chain.cloud.tax.dto.fxgj.TempListReq req) throws Exception;
}

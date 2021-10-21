package chain.fxgj.server.payroll.service.impl;

import chain.cloud.tax.client.feign.FxgjSignFeignService;
import chain.cloud.tax.dto.fxgj.SealH5Req;
import chain.cloud.tax.dto.fxgj.SealH5Res;
import chain.fxgj.server.payroll.service.TaxHttpClientService;
import chain.fxgj.server.payroll.service.TaxService;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chenm
 * @date 2020/11/5
 */
@Slf4j
@Service
public class TaxServiceImpl implements TaxService {

    @Autowired
    TaxHttpClientService taxHttpClientService;
    @Autowired
    FxgjSignFeignService fxgjSignFeignService;

    @Override
    public chain.cloud.tax.dto.fxgj.WalletH5Res walletH5(chain.cloud.tax.dto.fxgj.WalletH5Req req) {
        log.info("=====> 用户签约h5 req:{}", JacksonUtil.objectToJson(req));
        chain.cloud.tax.dto.fxgj.WalletH5Res walletH5Res = fxgjSignFeignService.walletH5(req);
        return walletH5Res;
    }

    @Override
    public chain.cloud.tax.dto.fxgj.SealH5Res sealH5(chain.cloud.tax.dto.fxgj.SealH5Req req) {
        log.info("=====> 用户签约记录 req:{}", JacksonUtil.objectToJson(req));
        SealH5Res sealH5Res = fxgjSignFeignService.sealH5(req);
        return sealH5Res;
    }

    @Override
    public chain.cloud.tax.dto.fxgj.SealUserRes user(chain.cloud.tax.dto.fxgj.SealUserReq req) {
        log.info("=====> 推送用户实名认证信息至零工平台 req:{}", JacksonUtil.objectToJson(req));
        chain.cloud.tax.dto.fxgj.SealUserRes user = fxgjSignFeignService.user(req);
        return user;
    }

    @Override
    public List<chain.cloud.tax.dto.fxgj.TempListRes> tempList(chain.cloud.tax.dto.fxgj.TempListReq req) {
        log.info("=====> 获取服务机构用工单位可签约的协议模板 req:{}", JacksonUtil.objectToJson(req));
        List<chain.cloud.tax.dto.fxgj.TempListRes> tempListRes = fxgjSignFeignService.tempList(req);
        return tempListRes;
    }
}

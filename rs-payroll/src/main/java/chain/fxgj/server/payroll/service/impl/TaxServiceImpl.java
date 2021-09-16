package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.server.payroll.dto.tax.*;
import chain.fxgj.server.payroll.service.TaxHttpClientService;
import chain.fxgj.server.payroll.service.TaxService;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenm
 * @date 2020/11/5
 */
@Slf4j
@Service
public class TaxServiceImpl implements TaxService {

    @Autowired
    TaxHttpClientService taxHttpClientService;

    @Override
    public void file() throws Exception {
        String url = "/sys/unAuth/file";
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("date", "20210714");
        //h5
        taxHttpClientService.send(paramMap, url);
    }

    @Override
    public WalletH5Res walletH5(WalletH5Req req) throws Exception {
        log.info("=====> 用户签约h5 req:{}", JacksonUtil.objectToJson(req));
        String url = "/sys/walletH5";
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("transUserId", req.getTransUserId());
        paramMap.put("userName", req.getUserName());
        paramMap.put("phone", req.getPhone());
        paramMap.put("idType", req.getIdType());
        paramMap.put("idCardNo", req.getIdCardNo());
        paramMap.put("fwOrg", req.getFwOrg());
        paramMap.put("fwOrgId", req.getFwOrgId());
        paramMap.put("ygOrg", req.getYgOrg());
        paramMap.put("ygOrgId", req.getYgOrgId());
        paramMap.put("templateId", req.getTemplateId());
        //h5
        String result = taxHttpClientService.send(paramMap, url);
        if (StringUtils.isNotBlank(result)) {
            return JacksonUtil.jsonToBean(result, WalletH5Res.class);
        }
        return null;
    }

    @Override
    public String sealH5(String transUserId) throws Exception {
        log.info("=====> 用户签约记录 transUserId:{}", transUserId);
        String url = "/sys/sealH5";
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("transUserId", transUserId);
        //h5
        String result = taxHttpClientService.send(paramMap, url);
        if (StringUtils.isNotBlank(result)) {
            Map<String, String> map = (Map<String, String>) JacksonUtil.jsonToMap(result);
            return map.get("url");
        }
        return null;
    }

    @Override
    public SealUserRes user(SealUserReq req) throws Exception {
        log.info("=====> 推送用户实名认证信息至零工平台 req:{}", JacksonUtil.objectToJson(req));
        String url = "/sys/user";
        log.info("=====> idCardImg1 length:{}", req.getIdCardImg1().length() / 1024);
        log.info("=====> idCardImg2 length:{}", req.getIdCardImg2().length() / 1024);
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("transUserId", req.getTransUserId());
        paramMap.put("userName", req.getUserName());
        paramMap.put("phone", req.getPhone());
        paramMap.put("idType", req.getIdType());
        paramMap.put("idCardNo", req.getIdCardNo());
        paramMap.put("idCardImg1", req.getIdCardImg1());
        paramMap.put("idCardImg2", req.getIdCardImg2());
        paramMap.put("address", req.getAddress());

        log.info("=====> paramMap length:{}", paramMap.toString().length() / 1024);
        //h5
        String result = taxHttpClientService.send(paramMap, url);
        if (StringUtils.isNotBlank(result)) {
            return JacksonUtil.jsonToBean(result, SealUserRes.class);
        }
        return null;
    }

    @Override
    public List<TempListRes> tempList(TempListReq req) throws Exception {
        log.info("=====> 获取服务机构用工单位可签约的协议模板 req:{}", JacksonUtil.objectToJson(req));
        String url = "/sys/tempList";
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("fwOrgId", req.getFwOrgId());
        paramMap.put("fwOrg", req.getFwOrg());
        paramMap.put("ygOrgId", req.getYgOrgId());
        paramMap.put("ygOrg", req.getYgOrg());

        String result = taxHttpClientService.send(paramMap, url);
        if (StringUtils.isNotBlank(result)) {
            List<TempListRes> list = JacksonUtil.jsonToList(result, TempListRes.class);
            return list;
        }
        return null;
    }
}

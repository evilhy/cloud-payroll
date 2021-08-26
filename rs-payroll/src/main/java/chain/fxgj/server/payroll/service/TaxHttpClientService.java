package chain.fxgj.server.payroll.service;

import java.util.Map;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/24 15:22
 */
public interface TaxHttpClientService {

    /**
     * 发送请求
     *
     * @param paramMap 请求参数
     * @param url      请求接口路径
     * @return
     * @throws Exception
     */
    public String send(Map<String, Object> paramMap, String url) throws Exception;

}

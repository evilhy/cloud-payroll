package chain.fxgj.server.payroll.config.feign;

import chain.utils.commons.JacksonUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

/**
 * @program: cloud-account
 * @description: Feign请求拦截器（设置请求头，传递登录信息）
 * @author: lius
 * @create: 2019/10/19 23:56
 */
@Slf4j
public class FeignBasicAuthRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        log.info("=======>mdcContext:[{}]", JacksonUtil.objectToJson(mdcContext));
        requestTemplate.header("req-id", mdcContext.get("log_token"));
        requestTemplate.header("log-token", mdcContext.get("log_token"));
        requestTemplate.header("page-num", mdcContext.get("pageNum"));
        requestTemplate.header("limit", mdcContext.get("limit"));
        requestTemplate.header("liquidation", mdcContext.get("liquidation"));
        requestTemplate.header("version", mdcContext.get("version"));
        requestTemplate.header("subVersion", mdcContext.get("subVersion"));
        requestTemplate.header("sort-field", mdcContext.get("sort-field"));
        requestTemplate.header("direction", mdcContext.get("direction"));
        requestTemplate.header("entId", mdcContext.get("entId"));
        requestTemplate.header("jsessionId", mdcContext.get("jsessionId"));
        requestTemplate.header("idNumber", mdcContext.get("idNumber"));
        requestTemplate.header("idNumberEncrytor", mdcContext.get("idNumberEncrytor"));
        requestTemplate.header("apppartner", mdcContext.get("apppartner"));
    }
}
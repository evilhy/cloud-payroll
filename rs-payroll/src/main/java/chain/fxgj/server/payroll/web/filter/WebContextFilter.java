package chain.fxgj.server.payroll.web.filter;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author chain
 * create by chain on 2019-01-26 11:10
 **/
@Slf4j
@Component
public class WebContextFilter implements WebFilter, Ordered {

    private static final String START_TIME = "start-time";

    private static final String REQ = "req-id";

    public WebContextFilter() {
        log.info("Loaded WebFilter [WebContextFilter]");
    }

    @Override
    @TrackLog
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String reqId = exchange.getRequest().getId();
        String logToken = exchange.getRequest().getHeaders().getFirst("log-token");
        if (StringUtils.isBlank(logToken)) {
            log.info("====>logToken={}",logToken);
            reqId = UUIDUtil.createUUID8();
            logToken = UUIDUtil.createUUID8();

        }
        MDC.put(REQ, reqId);
        MDC.put(FxgjDBConstant.LOG_TOKEN, logToken);

        String apppartner = exchange.getRequest().getHeaders().getFirst(PayrollConstants.APPPARTNER);
        if (StringUtils.isNotBlank(apppartner)&& !apppartner.equals("undefined")) {
            AppPartnerEnum appPartner  =  AppPartnerEnum.values()[Integer.valueOf(apppartner)];
            MDC.put(PayrollConstants.APPPARTNER, appPartner.getCode().toString());
            MDC.put(PayrollConstants.APPPARTNER_DESC, appPartner.getDesc());
        }

        StringBuffer logBuffer = new StringBuffer();
        ServerHttpRequest request = exchange.getRequest();
        logBuffer.append(String.format("%s %s Query:%s \n", request.getMethod().name(), request.getURI().getPath(), request.getQueryParams()));
        //getRemoteAddress 可能获取不到，所以判断
        if (null != exchange.getRequest().getRemoteAddress()) {
            logBuffer.append(exchange.getRequest().getRemoteAddress().getHostString());
        }
        exchange.getRequest().getHeaders().forEach((name, values) -> {
            values.forEach(value -> {
                logBuffer.append("> ").append(name).append(": ").append(value).append('\n');
            });
        });
        log.info(logBuffer.toString());
        //log.info("{}", exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR).toString());
        //log.info("{}", exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR).toString());
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());
        exchange.getAttributes().put(REQ, reqId);
        String finalReqId = reqId;
        //将现在的request 变成 change对象
        ServerHttpRequest host = exchange.getRequest().mutate().header(REQ, reqId).build();
        ServerWebExchange build = exchange.mutate().request(host).build();

        return chain.filter(build).then(Mono.fromRunnable(() -> {
            MDC.put(REQ, finalReqId);
            MDC.put(FxgjDBConstant.LOG_TOKEN, finalReqId);
            Long startTime = exchange.getAttribute(START_TIME);
            if (startTime != null) {
                Long executeTime = (System.currentTimeMillis() - startTime);
                log.info("{} {} {} ms", exchange.getRequest().getURI().getRawPath(), exchange.getResponse().getStatusCode(), executeTime);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

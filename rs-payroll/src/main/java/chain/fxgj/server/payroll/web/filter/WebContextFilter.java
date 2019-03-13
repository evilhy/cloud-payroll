package chain.fxgj.server.payroll.web.filter;

import chain.css.log.annotation.TrackLog;
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
//@Component
public class WebContextFilter implements WebFilter, Ordered {

    private static final String START_TIME = "start_time";

    private static final String REQ = "req_id";


    public WebContextFilter() {
        log.info("Loaded WebFilter [WebContextFilter]");
    }

    @Override
    @TrackLog
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String reqId = exchange.getRequest().getId();
        if (StringUtils.isBlank(reqId)) {
            reqId = UUIDUtil.createUUID24();
        }

        MDC.put(REQ, reqId);

        StringBuffer logBuffer = new StringBuffer();
        logBuffer.append(String.format("%s %s %s Query:%s \n",
                exchange.getRequest().getMethod().name(),
                exchange.getRequest().getURI().getPath(),
                exchange.getRequest().getRemoteAddress().getHostString(),
                exchange.getRequest().getQueryParams()));
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
            Long startTime = exchange.getAttribute(START_TIME);
            if (startTime != null) {
                Long executeTime = (System.currentTimeMillis() - startTime);
                log.info("{} {} {}", exchange.getRequest().getURI().getRawPath(),
                        exchange.getResponse().getStatusCode(), executeTime +
                                "ms");
            }
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
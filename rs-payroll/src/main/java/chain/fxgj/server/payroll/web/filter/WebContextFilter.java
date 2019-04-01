package chain.fxgj.server.payroll.web.filter;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author chain
 * create by chain on 2019-01-26 11:10
 **/
@Slf4j
@Component
public class WebContextFilter implements WebFilter, Ordered {

    private static final String START_TIME = "start-time";

    private static final String REQ = "req-id";

    private static final String MAX_AGE = "18000L";

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
        MDC.put(FxgjDBConstant.LOG_TOKEN, reqId);

        StringBuffer logBuffer = new StringBuffer();
        logBuffer.append(String.format("%s %s Query:%s \n",
                exchange.getRequest().getMethod().name(),
                exchange.getRequest().getURI().getPath(),
                exchange.getRequest().getQueryParams()));
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

//- 先判断 OPTIONS
        ServerHttpRequest request = build.getRequest();
        List<String> strings = request.getHeaders().get(HttpHeaders.ORIGIN);
        log.info("request.getMethod():[{}]",request.getMethod());
        log.info(" request.getHeaders().get('Origin'):[{}]", strings);
        if (CorsUtils.isCorsRequest(request)) {
            HttpHeaders requestHeaders = request.getHeaders();
            ServerHttpResponse response = build.getResponse();
            HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();
            HttpHeaders headers = response.getHeaders();
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, requestHeaders.getOrigin());
            headers.setAccessControlAllowOrigin("*");
            headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders
                    .getAccessControlRequestHeaders());
            if (requestMethod != null) {
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod.name());
            }
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
            headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);

            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }

        }
//-

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

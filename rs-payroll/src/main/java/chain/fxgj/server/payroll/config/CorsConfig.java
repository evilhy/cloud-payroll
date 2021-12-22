package chain.fxgj.server.payroll.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 跨域允许
 */
@Configuration
public class CorsConfig {
    private static final String MAX_AGE = "18000L";

    @Bean
    @Order(-1)
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            ServerHttpResponse response = ctx.getResponse();

            String requestPath = ctx.getRequest().getPath().toString();
            if ("/".equals(requestPath) || "/favicon.ico".equals(requestPath)) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            } else if (!CorsUtils.isCorsRequest(request)) {
                return chain.filter(ctx);
            }

            if (CorsUtils.isCorsRequest(request)) {
                HttpHeaders requestHeaders = request.getHeaders();
                HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();
                HttpHeaders headers = response.getHeaders();
                headers.setAccessControlAllowOrigin("*");
                List<String> headerList = new ArrayList<>();
                headerList.add("origin");
                headerList.add("content-type");
                headerList.add("accept");
                headerList.add("totalpages");
                headerList.add("totalelements");
                headerList.add("reqId");
                headerList.add("page");
                headerList.add("entId");
                headerList.add("jsessionId");
                headerList.add("ascriptionType");
                headerList.add("routeName");
                headerList.add("ascriptionChannel");
                headerList.add("liquidation");
                headerList.add("version");
                headerList.add("subVersion");
                headerList.add("sysVersion");
                headerList.add("encode-key");
                headerList.add("timestamp");
                headerList.add("sha256-sign");
                headerList.add("Cache-Control");
                headerList.add("Pragma");
//                headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders
//                        .getAccessControlRequestHeaders());
                headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, headerList);
                if (requestMethod != null) {
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod.name());
                    List<String> methodList = new ArrayList<>();
                    methodList.add("GET");
                    methodList.add("POST");
                    methodList.add("PUT");
                    methodList.add("DELETE");
                    methodList.add("OPTIONS");
                    methodList.add("HEAD");
                    methodList.add("PATCH");
                    headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, methodList);
                }
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
                headers.setAccessControlExposeHeaders(Arrays.asList("*", "Content-Disposition"));

                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }

            }

//            response.getHeaders().add("Access-Control-Allow-Origin", "*");
//            response.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, totalpages, totalelements," +
//                    " size, page,entId,jsessionId,reqId,ascriptionType,routeName,ascriptionChannel,liquidation,version,subVersion,sysVersion,"+
//                    "encode-key,timestamp,sha256-sign");
//
//            response.getHeaders().add("Access-Control-Allow-Credentials", "true");
//            response.getHeaders().add("Access-Control-Allow-Methods",
//                    "GET, POST, PUT, DELETE, OPTIONS, HEAD,PATCH");
//            response.getHeaders().add("Access-Control-Max-Age", "604800");  //一周

            return chain.filter(ctx);
        };
    }

}

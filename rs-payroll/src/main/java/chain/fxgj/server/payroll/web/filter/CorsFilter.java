//package chain.fxgj.server.payroll.web.filter;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.web.cors.reactive.CorsUtils;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//
//@Slf4j
////@Component
//public class CorsFilter implements WebFilter, Ordered {
//    private static final String MAX_AGE = "18000L";
//
//    @Override
//    public int getOrder() {
//        return 0;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        ServerHttpRequest request = exchange.getRequest();
//        List<String> strings = request.getHeaders().get(HttpHeaders.ORIGIN);
//        log.info("request.getMethod():[{}]", request.getMethod());
//        log.info(" request.getHeaders().get('Origin'):[{}]", strings);
//        if (CorsUtils.isCorsRequest(request)) {
//            HttpHeaders requestHeaders = request.getHeaders();
//            ServerHttpResponse response = exchange.getResponse();
//            HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();
//            HttpHeaders headers = response.getHeaders();
//            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, requestHeaders.getOrigin());
//            headers.setAccessControlAllowOrigin("*");
//            headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders
//                    .getAccessControlRequestHeaders());
//            if (requestMethod != null) {
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod.name());
//            }
//            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
//            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
//            headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
//
//            if (request.getMethod() == HttpMethod.OPTIONS) {
//                response.setStatusCode(HttpStatus.OK);
//                return Mono.empty();
//            }
//        }
//        return chain.filter(exchange);
//    }
//}

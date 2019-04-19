package chain.fxgj.server.payroll.web.filter;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Slf4j
@Component
public class AuthorizationFilter implements WebFilter, Ordered {

    /**
     * 不需要登录的url
     */
    public static final String[] excludeUrls = new String[]{
            "/roll/sdt",
            "/roll/entEmp",
            "/inside/sendCode",
            "/actuator",
            "/weixin/signature",
            "/weixin/wxCallback",
            "/weixin/getJsapiSignature",
            "/tfinance/intentionList",
            "/inside/rz/",
            "/roll/checkCar",
            "/tfinance/codeUrl",
            "/advertising/rotation",
            "/merchant/getAccess",
            "/merchant/callback"


    };

    @Autowired
    private EmpWechatService empWechatService;

    public AuthorizationFilter(EmpWechatService empWechatService){
        this.empWechatService = empWechatService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String jsessionId = exchange.getRequest().getHeaders().getFirst(PayrollConstants.JSESSIONID);
        String req_id = exchange.getRequest().getHeaders().getFirst("req-id");
        log.info("--------------->req_id:[{}]",req_id);
        log.info("--------------->jsessionId:[{}]",jsessionId);
        ServerHttpRequest serverHttpRequest = exchange.getRequest();

        String requestUrl = serverHttpRequest.getURI().getPath();
        for (String url : excludeUrls) {
            if (StringUtils.indexOf(requestUrl, url) > -1) {
                log.info("[{}]不需要验证jsessionId",requestUrl);
                return chain.filter(exchange);
            }
        }
        log.info("[{}]需要验证jsessionId",requestUrl);
        UserPrincipal principal = empWechatService.getWechatInfo(jsessionId);
        if (principal == null) {
            throw new ParamsIllegalException(ErrorConstant.WECHAT_OUT.getErrorMsg());
        }
        LocalDateTime sessionTimeOut = principal.getSessionTimeOut();
        if (LocalDateTime.now().isAfter(sessionTimeOut)) {
            throw new ParamsIllegalException(ErrorConstant.WECHAT_OUT.getErrorMsg());
        }
        WebContext.setCurrentUser(principal);

        return chain.filter(exchange);
    }


//    public Mono<Void> predicate(ServerWebExchange serverWebExchange,WebFilterChain chain) {
//        log.info("验证访问路径 是否 核验 jsessionId");
//
//        URI uri = serverWebExchange.getRequest().getURI();
//        String requestUri = uri.getPath();
//        log.info("访问路径  URI={}, PATH={}", uri, requestUri);
//        String jsessionId = serverWebExchange.getRequest().getHeaders().getFirst(PayrollConstants.JSESSIONID);
//        log.info("jsessionId = {} ", jsessionId);
//        if (isPermitUrl(requestUri)) {
//            return chain.filter(serverWebExchange);
//        }
//
//    }


    @Override
    public int getOrder() {
        return 0;
    }

}






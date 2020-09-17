package chain.fxgj.server.payroll.web.filter;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.base.HeaderDTO;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.util.TransferUtil;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
//todo 上线记得放开
@Component
public class AuthorizationFilter implements WebFilter, Ordered {

    /**
     * 不需要登录的url
     */
    public static final String[] excludeUrls = new String[]{
            "docs",
            "/roll/sdt",
//            "/roll/entEmp",
            "/inside/sendCode",
            "/actuator",
            "/weixin",
            "/tfinance/intentionList",
//            "/inside/rz/",
//            "/roll/checkCar",
            "/tfinance/codeUrl",
            "/advertising/rotation",
            "/merchant/getAccess",
            "/merchant/callback",
            "/unAuth",
            "/sync",
            "/virus",
            "/mini",
            "/securityes",
            "/wechat/wxCallback"
    };

    @Autowired
    private EmpWechatService empWechatService;

    public AuthorizationFilter(EmpWechatService empWechatService) {
        this.empWechatService = empWechatService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String entId = exchange.getRequest().getHeaders().getFirst("ent-id");
        String jsessionId = exchange.getRequest().getHeaders().getFirst(PayrollConstants.JSESSIONID);
        String req_id = exchange.getRequest().getHeaders().getFirst("req-id");
        log.info("--------------->req_id:[{}]", req_id);
        log.info("--------------->jsessionId:[{}]", jsessionId);
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        //todo 跑数据使用，正式环境必须删除
//        if (true) {
//            return chain.filter(exchange);
//        }
        String requestUrl = serverHttpRequest.getURI().getPath();
        for (String url : excludeUrls) {
            if (StringUtils.indexOf(requestUrl, url) > -1) {
                log.info("[{}]不需要验证jsessionId", requestUrl);
                return chain.filter(exchange);
            }
        }
        log.info("[{}]需要验证jsessionId", requestUrl);
        CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
        UserPrincipal principal = TransferUtil.WageUserPrincipalToUserPrincipal(wechatInfoDetail);
        if (principal == null) {
            throw new ParamsIllegalException(ErrorConstant.WECHAT_OUT.getErrorMsg());
        }
        LocalDateTime sessionTimeOut = principal.getSessionTimeOut();
        if (LocalDateTime.now().isAfter(sessionTimeOut)) {
            throw new ParamsIllegalException(ErrorConstant.WECHAT_OUT.getErrorMsg());
        }

        //请求头中entId不为空的时候设置到缓存中，为空的时候就不用设置
        if (!StringUtils.equals("undefined", entId) && StringUtils.isNotBlank(entId)) {
            principal.setEntId(entId);
        }

        WebContext.setCurrentUser(principal);

        String pageNumStr = exchange.getRequest().getHeaders().getFirst("page-num");
        String limitStr = exchange.getRequest().getHeaders().getFirst("limit");
        String sortField = exchange.getRequest().getHeaders().getFirst("sort-field");
        String direction = exchange.getRequest().getHeaders().getFirst("direction");
        String liquidation = exchange.getRequest().getHeaders().getFirst("liquidation");
        String version = exchange.getRequest().getHeaders().getFirst("version");
        String subVersion = exchange.getRequest().getHeaders().getFirst("subVersion");
        MDC.put("log-token", req_id);
        MDC.put("jsessionId", jsessionId);
        MDC.put("pageNum", pageNumStr);
        MDC.put("limit", limitStr);
        MDC.put("liquidation", liquidation);
        MDC.put("sort-field", sortField);
        MDC.put("direction", direction);
        MDC.put("version", version);
        MDC.put("subVersion", subVersion);
        MDC.put("entId", entId);
        MDC.put("idNumber", wechatInfoDetail.getIdNumber());
        MDC.put("idNumberEncrytor", wechatInfoDetail.getIdNumberEncrytor());

        HeaderDTO headerDTO = HeaderDTO.builder()
                .limit(StringUtils.isBlank(limitStr) ? 30 : Integer.parseInt(limitStr))
                .pageNum(StringUtils.isBlank(pageNumStr) ? 30 : Integer.parseInt(pageNumStr))
                .direction(direction)
                .sortField(sortField)
                .jsessionId(jsessionId)
                .liquidation(StringUtils.isBlank(liquidation) ? null : Integer.parseInt(liquidation))
                .version(StringUtils.isBlank(version) ? null : Integer.parseInt(version))
                .subVersion(StringUtils.isBlank(subVersion) ? null : Integer.parseInt(subVersion))
                .build();
        WebContext.setCurrentHeader(headerDTO);
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






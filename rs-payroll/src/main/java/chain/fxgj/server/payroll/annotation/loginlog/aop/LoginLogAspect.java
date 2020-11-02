package chain.fxgj.server.payroll.annotation.loginlog.aop;

import chain.fxgj.server.payroll.annotation.loginlog.annotation.LoginLog;
import chain.fxgj.server.payroll.service.LoginLogService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.utils.commons.JacksonUtil;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 定义切面，实现通知的具体内容
 */
@Component
@Aspect
@Slf4j
public class LoginLogAspect {
    @Autowired
    WechatRedisService wechatRedisService;
    @Autowired
    LoginLogService loginLogService;


    /**
     * 切入点
     */
    @Pointcut("@annotation(chain.fxgj.server.payroll.annotation.loginlog.annotation.LoginLog)")
    private void pointcut() {

    }

    @Around("pointcut() && @annotation(logger)")
    public Object advice(ProceedingJoinPoint joinPoint, LoginLog logger) throws Throwable {
//        System.out.println("--- Kth日志的内容为[" + logger.value() + "] ---");
//        System.out.println("注解作用的方法名: " + joinPoint.getSignature().getName());
//        System.out.println("所在类的简单类名: " + joinPoint.getSignature().getDeclaringType().getSimpleName());
//        System.out.println("所在类的完整类名: " + joinPoint.getSignature().getDeclaringType());
//        System.out.println("目标方法的声明类型: " + Modifier.toString(joinPoint.getSignature().getModifiers()));

        log.info("注解日志-[{}],[{}],[{}]", joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName(), logger.value());
        boolean bol = true;
        //返回结果封装类
        //1.这里获取到所有的参数值的数组
        Object[] args = joinPoint.getArgs();
//        List<String> strings = new ArrayList<>();
//        for (Object arg : args) {
//            if (arg instanceof ServerHttpRequest) {
//                strings = ((ServerHttpRequest) arg).getHeaders().get("jsession_id");
//            }
//        }

        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        //2.最关键的一步:通过这获取到方法的所有参数名称的字符串数组
        String[] parameterNames = methodSignature.getParameterNames();
        try {
            //3.通过你需要获取的参数名称的下标获取到对应的值
            int jsessionIdIndex = ArrayUtils.indexOf(parameterNames, "jsession_id");
            if (jsessionIdIndex != -1) {
                String jsessionId = (String) args[jsessionIdIndex];
                log.info("header-jsessionId:[{}]", jsessionId);
                CacheUserPrincipal cacheUserPrincipal = wechatRedisService.userPrincipalByJsessionId(jsessionId);
                if (!StringUtils.equals("undefined", jsessionId) && null != cacheUserPrincipal) {
                    String openId = cacheUserPrincipal.getOpenId();
                    if (StringUtils.isNotBlank(openId)) {
                        loginLogService.saveLoginLog(openId);
                        bol = false;
                    } else {
                        log.info("根据openId取缓存，但无openId");
                    }
                } else {
                    log.info("根据jsessionId未查询到缓存信息");
                }
            } else {
                log.info("无jsessionId");
            }
        } catch (Throwable throwable) {
            log.error("注解获取jsessionId异常");
        }

        Object proceed = joinPoint.proceed();

        if (bol) {
            log.info("根据返回值操作入库");
            try {
                if (proceed != null) {
                    log.info("{}", proceed.toString());
                }
                String json = JacksonUtil.objectToJson(proceed);
                log.info("返回值:[{}]", json);
                Map<?, ?> map = JacksonUtil.jsonToMap(json);
                if (map.containsKey("jsessionId")) {
                    String jsessionId = map.get("jsessionId").toString();
                    CacheUserPrincipal cacheUserPrincipal = wechatRedisService.userPrincipalByJsessionId(jsessionId);
                    if (null != cacheUserPrincipal) {
                        String openId = cacheUserPrincipal.getOpenId();
                        if (StringUtils.isNotBlank(openId)) {
                            loginLogService.saveLoginLog(openId);
                            bol = false;
                        } else {
                            log.info("2根据openId取缓存，但无openId");
                        }
                    } else {
                        log.info("2根据jsessionId未查询到缓存信息");
                    }
                } else {
                    log.info("返回值无jsessionId");
                }
            } catch (Throwable throwable) {
                log.error("jsessionId日志记录异常");
                throwable.printStackTrace();
            }
        } else {
            log.info("通过jsessionId已完成");
        }
        log.info("end");
        return proceed;
    }

//-----------------------以下为扩展内容
//
//    //配置切入点,该方法无方法体,主要为方便同类中其他方法使用此处配置的切入点
//    @Pointcut("execution(* chain.fxgj.server.payroll.annotation.five.annotation..*(..))")
//    public void aspect() {
//    }
//
//    /*
//     * 配置前置通知,使用在方法aspect()上注册的切入点
//     * 同时接受JoinPoint切入点对象,可以没有该参数
//     */
//    @Before("aspect()")
//    public void before(JoinPoint joinPoint) {
//        log.info("before " + joinPoint);
//    }
//
//    //配置后置通知,使用在方法aspect()上注册的切入点
//    @After("aspect()")
//    public void after(JoinPoint joinPoint) {
//        log.info("after " + joinPoint);
//    }
//
//    //配置环绕通知,使用在方法aspect()上注册的切入点
//    @Around("aspect()")
//    public void around(JoinPoint joinPoint) {
//        long start = System.currentTimeMillis();
//        try {
//            ((ProceedingJoinPoint) joinPoint).proceed();
//            long end = System.currentTimeMillis();
//            log.info("around " + joinPoint + "\tUse time : " + (end - start) + " ms!");
//        } catch (Throwable e) {
//            long end = System.currentTimeMillis();
//            log.info("around " + joinPoint + "\tUse time : " + (end - start) + " ms with exception : " + e.getMessage());
//        }
//    }
//
//    //配置后置返回通知,使用在方法aspect()上注册的切入点
//    @AfterReturning("aspect()")
//    public void afterReturn(JoinPoint joinPoint) {
//        log.info("afterReturn " + joinPoint);
//    }
//
//    //配置抛出异常后通知,使用在方法aspect()上注册的切入点
//    @AfterThrowing(pointcut = "aspect()", throwing = "ex")
//    public void afterThrow(JoinPoint joinPoint, Exception ex) {
//        log.info("afterThrow " + joinPoint + "\t" + ex.getMessage());
//    }
}

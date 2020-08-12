package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.handpassword.HandPasswordDTO;
import chain.fxgj.server.payroll.service.AdminService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.dto.response.wechat.EmployeeWechatDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: du
 * @Date: 2020/8/11 20:47
 */
@RestController
@Validated
@RequestMapping(value = "/admin")
@Slf4j
public class AdminController {

    @Autowired
    AdminService adminService;
    @Resource
    RedisTemplate redisTemplate;

    /**
     * 查询用户是否开启手势密码
     *
     * @return
     */
    @GetMapping("/queryHandPassword")
    @TrackLog
    @PermitAll
    public Mono<HandPasswordDTO> queryHandPassword() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = WebContext.getCurrentUser().getWechatId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /admin/queryHandPassword 查询用户是否开启手势密码 wechatId:{}", wechatId);

            return adminService.queryHandPassword(wechatId);
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 关闭手势密码
     *
     * @return
     */
    @GetMapping("/closeHandPassword")
    @TrackLog
    @PermitAll
    public Mono<Void> closeHandPassword() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = WebContext.getCurrentUser().getWechatId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /admin/closeHandPassword 关闭手势密码 wechatId:{}", wechatId);

            EmployeeWechatDTO dto = adminService.closeHandPassword(wechatId);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 登录密码校验
     *
     * @param password 密码
     * @param type     密码类型 0：数字密码  1：手势密码
     * @return
     */
    @GetMapping("/checkPassword")
    @TrackLog
    public Mono<Void> checkPassword(@RequestParam("password") String password,
                                    @RequestParam("type") String type) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = WebContext.getCurrentUser().getWechatId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /admin/checkPassword 校验密码 wechatId:{}, password:{}，type{}", wechatId, password, type);

            EmployeeWechatDTO dto = adminService.checkPassword(wechatId, password, type);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 校验并保存密码
     *
     * @param oldPassword 原密码
     * @param password    确认密码
     * @param type        密码类型 0：数字密码  1：手势密码
     * @return
     */
    @GetMapping("/savePassword}")
    @TrackLog
    @PermitAll
    public Mono<Void> savePassword(@RequestParam("password") String password,
                                   @RequestParam("oldPassword") String oldPassword,
                                   @RequestParam("type") String type) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = WebContext.getCurrentUser().getWechatId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /admin/savePassword 添加密码 wechatId:{}，oldPassword:{}, password:{}，type{}", wechatId, oldPassword, password, type);

            Optional.ofNullable(oldPassword).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘原密码’参数不能为空")));
            Optional.ofNullable(password).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘确认密码’参数不能为空")));

            if (!oldPassword.equals(password)) {
                log.info("=====> wechatId:{} 用户密码校验失败：oldPassword：{}，password：{}", wechatId, oldPassword, password);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("两次输入密码不一致"));
            }

            EmployeeWechatDTO dto = adminService.savePassword(wechatId, password, type);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 用户登录
     *
     * @param password 密码
     * @param type     密码类型 0：数字密码  1：手势密码
     * @return
     */
    @GetMapping("/login")
    @TrackLog
    @PermitAll
    public Mono<Void> login(@RequestParam("password") String password,
                            @RequestParam("type") String type) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String wechatId = currentUser.getWechatId();
        String idNumberEncrytor = currentUser.getIdNumberEncrytor();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /admin/login 用户登录 wechatId:{}, password:{}，type{}", wechatId, password, type);

            Optional.ofNullable(type).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码类型’参数不能为空")));
            Optional.ofNullable(password).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码’参数不能为空")));

            //密码校验
            EmployeeWechatDTO dto = adminService.checkPassword(wechatId, password, type);

            try {
                //密码校验通过之后，缓存中登记一条记录，之后的几分钟只能不再输入密码，key：sessionId
                String redisKey = FxgjDBConstant.PREFIX + ":checkFreePassword:" + idNumberEncrytor;
                log.info("checkPwd.redisKey:[{}]", redisKey);
                redisTemplate.opsForValue().set(redisKey, true, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("免密入缓存失败:[{}]", e.getMessage());
            }
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }
}

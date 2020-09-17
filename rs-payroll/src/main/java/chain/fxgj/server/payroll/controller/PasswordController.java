package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.PayrollDBConstant;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.handpassword.HandPasswordDTO;
import chain.fxgj.server.payroll.dto.request.PasswordSaveReq;
import chain.fxgj.server.payroll.dto.response.CrateNumericKeypadRes;
import chain.fxgj.server.payroll.dto.response.SecretFreeRes;
import chain.fxgj.server.payroll.service.PaswordService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.ids.core.commons.dto.softkeyboard.KeyboardResponse;
import chain.payroll.client.feign.EmployeeWechatFeignController;
import chain.utils.commons.JacksonUtil;
import com.google.gson.GsonBuilder;
import core.dto.response.wechat.EmployeeWechatDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping(value = "/password")
@Slf4j
public class PasswordController {

    @Autowired
    PaswordService paswordService;
    @Resource
    RedisTemplate redisTemplate;
    @Autowired
    EmployeeWechatFeignController employeeWechatFeignController;



    private static String KEYBOARD_KEY = "keyboard_{KEYBOARDID}";

    /**
     * 查询用户是否开启手势密码(待定使用)
     *
     * @return
     */
    @GetMapping("/queryHandPassword")
    @TrackLog
    @PermitAll
    public Mono<HandPasswordDTO> queryHandPassword() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = String.valueOf(WebContext.getCurrentUser().getWechatId());
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到登录用户可用标识")));
            log.info("=====> /admin/queryHandPassword 查询用户是否开启手势密码 wechatId:{}", wechatId);
            return paswordService.queryHandPassword(wechatId);
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
        String wechatId = String.valueOf(WebContext.getCurrentUser().getWechatId());
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到登录用户可用标识")));
            log.info("=====> /admin/closeHandPassword 关闭手势密码 wechatId:{}", wechatId);

            EmployeeWechatDTO dto = paswordService.closeHandPassword(wechatId);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 数字密码、手势密码校验
     *
     * @param passwordSaveReq
     * @return
     */
    @PostMapping("/checkPassword")
    @TrackLog
    public Mono<Void> checkPassword(@RequestBody PasswordSaveReq passwordSaveReq) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = String.valueOf(WebContext.getCurrentUser().getWechatId());
        return Mono.fromCallable(() -> {
            String password = passwordSaveReq.getPassword();
            String type = passwordSaveReq.getType();
            MDC.setContextMap(mdcContext);
            Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到登录用户可用标识")));
            Optional.ofNullable(password).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码’参数不能为空")));
            Optional.ofNullable(type).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码类型’参数不能为空")));
            log.info("=====> /admin/checkPassword 校验密码 wechatId:{}, password:{}，type{}", wechatId, password, type);

            //密码校验
            if ("1".equals(type)) {
                //手势键盘
                EmployeeWechatDTO dto = paswordService.checkPassword(wechatId, password, type);
            } else if ("0".equals(type)) {
                //数字键盘
                String password1 = paswordService.checkNumberPassword(password, wechatId);
                EmployeeWechatDTO dto = paswordService.checkPassword(wechatId, password1, type);
            } else {
                log.info("=====> 校验失败，密码类型异常(0：数字密码  1：手势密码) type = {}", type);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码类型异常"));
            }

            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }


    /**
     * 数字密码、手势密码保存
     *
     * @return
     */
    @PostMapping("/savePassword")
    @TrackLog
    @PermitAll
    public Mono<Void> savePassword(@RequestBody PasswordSaveReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = String.valueOf(WebContext.getCurrentUser().getWechatId());
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到登录用户可用标识")));
            Optional.ofNullable(req.getFirstPassword()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("第一次密码不能为空")));
            Optional.ofNullable(req.getPassword()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘确认密码’参数不能为空")));
            Optional.ofNullable(req.getType()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码类型’参数不能为空")));

            log.info("=====> /admin/savePassword 添加密码 wechatId:{}，req:{}", wechatId, JacksonUtil.objectToJson(req));

            String password = req.getPassword();
            String type = req.getType();
            String firstPassword = req.getFirstPassword();
            if (!firstPassword.equals(password)) {
                log.info("=====> wechatId:{} 用户密码校验失败：firstPassword：{}，password：{}", wechatId, firstPassword, password);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("两次输入密码不一致"));
            }
            //密码校验
            if ("1".equals(type)) {
                //手势键盘
                EmployeeWechatDTO dto = paswordService.savePassword(wechatId, password, type);
            } else if ("0".equals(type)) {
                //数字键盘
                String password1 = paswordService.checkNumberPassword(password, wechatId);
                EmployeeWechatDTO dto = paswordService.savePassword(wechatId, password1, type);
            } else {
                log.info("=====> 校验失败，密码类型异常(0：数字密码  1：手势密码) type = {}", type);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码类型异常"));
            }

            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 用户登录(校验通过，记录缓存)
     *
     * @param passwordSaveReq
     * @return
     */
    @PostMapping("/login")
    @TrackLog
    @PermitAll
    public Mono<Void> login(@RequestBody PasswordSaveReq passwordSaveReq) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String wechatId = String.valueOf(currentUser.getWechatId());
        String idNumberEncrytor = currentUser.getIdNumberEncrytor();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String password = passwordSaveReq.getPassword();
            String type = passwordSaveReq.getType();
            log.info("=====> /admin/login 用户登录 wechatId:{}, password:{}，type{}", wechatId, password, type);

            Optional.ofNullable(type).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码类型’参数不能为空")));
            Optional.ofNullable(password).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码’参数不能为空")));

            //密码校验
            if ("1".equals(type)) {
                //手势键盘
                EmployeeWechatDTO dto = paswordService.checkPassword(wechatId, password, type);
            } else if ("0".equals(type)) {
                //数字键盘
                String password1 = paswordService.checkNumberPassword(password, wechatId);
                EmployeeWechatDTO dto = paswordService.checkPassword(wechatId, password1, type);
            } else {
                log.info("=====> 校验失败，密码类型异常(0：数字密码  1：手势密码) type = {}", type);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码类型异常"));
            }

            try {
                //密码校验通过之后，缓存中登记一条记录，之后的几分钟只能不再输入密码，key：sessionId
                String redisKey = PayrollDBConstant.PREFIX + ":checkFreePassword:" + idNumberEncrytor;
                log.info("checkPwd.redisKey:[{}]", redisKey);
                redisTemplate.opsForValue().set(redisKey, true, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("免密入缓存失败:[{}]", e.getMessage());
            }
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 数字密码键盘生成(调用多次，每次都不一样)
     *
     * @return
     */
    @GetMapping("/crateNumericKeypad")
    @TrackLog
    @PermitAll
    public Mono<CrateNumericKeypadRes> crateNumericKeypad() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        //wechatId为空，就用openId作为 redisKey 缓存
        String wechatId = StringUtils.isEmpty(WebContext.getCurrentUser().getWechatId())?WebContext.getCurrentUser().getOpenId():WebContext.getCurrentUser().getWechatId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到登录用户可用标识")));

            //生成密码键盘ID
            String keyboardId = PayrollDBConstant.PREFIX + ":numberKeyboard:" + wechatId;
            log.info("=====> /admin/crateNumericKeypad 数字密码键盘生成 wechatId:{}", keyboardId);

            //生成密码键盘
            KeyboardResponse keyboardResponse = paswordService.crateNumericKeypad(keyboardId);
            String result = new GsonBuilder().disableHtmlEscaping().create().toJson(keyboardResponse.getNumber());

            //加入缓存
            try {
                log.info("crateNumericKeypad.redisKey:[{}]", keyboardId);
                redisTemplate.opsForValue().set(keyboardId, result, 1, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("密码键盘入缓存失败:[{}]", e.getMessage());
            }

            return CrateNumericKeypadRes.builder().numberBase(keyboardResponse.getNumberBase64()).build();
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 是否免密查询
     *
     * @return
     */
    @PostMapping("/secretFree")
    @TrackLog
    @PermitAll
    public Mono<SecretFreeRes> secretFree(@RequestBody PasswordSaveReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumberEncrytor = principal.getIdNumberEncrytor();
        String wechatId = String.valueOf(principal.getWechatId());

        return Mono.fromCallable(() -> {

            MDC.setContextMap(mdcContext);
            SecretFreeRes secretFreeRes = new SecretFreeRes();

            boolean security = true;//默认免密

            //一、是否免密
            if (StringUtils.isBlank(idNumberEncrytor)) {
                log.info("idNumberEncrytor空，直接返回false，需要输入密码");
                security = false;
            }
            String redisKey = PayrollDBConstant.PREFIX + ":checkFreePassword:" + idNumberEncrytor;
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value == null) {
                log.info("根据idNumberEncrytor:[{}]未查询到登录记录", idNumberEncrytor);
                security = false;
            }
            log.info("根据idNumberEncrytor:[{}]查询到登录记录,value:[{}]", idNumberEncrytor, value);
            secretFreeRes.setSecretFree(security);

            //二、密码类型
            Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到登录用户可用标识")));
            EmployeeWechatDTO employeeWechatDTO = employeeWechatFeignController.findById(wechatId);
            String queryPwd = employeeWechatDTO.getQueryPwd();
            String handPassword = employeeWechatDTO.getHandPassword();
            Integer pwdTyp = 0;
            if (StringUtils.isEmpty(queryPwd)) {
                // 0 无密码
                pwdTyp = 0;
            } else {
                if (StringUtils.isEmpty(handPassword)) {
                    //1 数字密码
                    pwdTyp = 1;
                } else {
                    //2 手势密码
                    pwdTyp = 2;
                }
            }
            secretFreeRes.setPwdType(pwdTyp);

            return secretFreeRes;
        }).subscribeOn(Schedulers.elastic());
    }


}

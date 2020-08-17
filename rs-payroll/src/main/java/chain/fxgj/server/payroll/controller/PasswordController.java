package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.PayrollDBConstant;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.handpassword.HandPasswordDTO;
import chain.fxgj.server.payroll.dto.request.PasswordSaveReq;
import chain.fxgj.server.payroll.service.PaswordService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.ids.core.commons.dto.softkeyboard.KeyboardResponse;
import chain.utils.commons.JacksonUtil;
import core.dto.response.wechat.EmployeeWechatDTO;
import lombok.extern.slf4j.Slf4j;
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

    private static String KEYBOARD_KEY = "keyboard_{KEYBOARDID}";

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
     * 登录密码校验
     *
     * @param password 密码   （数字密码以“,”分隔）
     * @param type     密码类型 0：数字密码  1：手势密码
     * @return
     */
    @GetMapping("/checkPassword")
    @TrackLog
    public Mono<Void> checkPassword(@RequestParam("password") String password,
                                    @RequestParam("type") String type) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = String.valueOf(WebContext.getCurrentUser().getWechatId());
        return Mono.fromCallable(() -> {
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
                String password1 = checkNumberPassword(password, wechatId);
                EmployeeWechatDTO dto = paswordService.checkPassword(wechatId, password1, type);
            } else {
                log.info("=====> 校验失败，密码类型异常(0：数字密码  1：手势密码) type = {}", type);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码类型异常"));
            }

            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    public String checkNumberPassword(String passsword, String wechatId) {
        //生成密码键盘ID
        String keyboardId = PayrollDBConstant.PREFIX + ":numberKeyboard:" + wechatId;

        //取出缓存
        try {
            log.info("crateNumericKeypad.redisKey:[{}]", keyboardId);
            String redisStr = redisTemplate.opsForValue().get(KEYBOARD_KEY.replace("{KEYBOARDID}", keyboardId)).toString();

            Map<String, Character> number = (Map<String, Character>) JacksonUtil.jsonToMap(redisStr);
            String[] split = passsword.split(",");
            if (null == split || split.length <= 0) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码不能为空"));
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                Character character = number.get(split[i]);
                sb = sb.append(character.charValue());
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("密码键盘读取缓存失败:[{}]", e.getMessage());
        }
        return null;
    }

    /**
     * 校验并保存密码
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
            Optional.ofNullable(req.getOldPassword()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘新密码’参数不能为空")));
            Optional.ofNullable(req.getPassword()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘确认密码’参数不能为空")));
            Optional.ofNullable(req.getType()).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码类型’参数不能为空")));

            log.info("=====> /admin/savePassword 添加密码 wechatId:{}，req:{}", wechatId, JacksonUtil.objectToJson(req));

            String password = req.getPassword();
            String oldPassword = req.getOldPassword();
            String type = req.getType();

            if (!oldPassword.equals(password)) {
                log.info("=====> wechatId:{} 用户密码校验失败：oldPassword：{}，password：{}", wechatId, oldPassword, password);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("两次输入密码不一致"));
            }

            //密码校验
            if ("1".equals(type)) {
                //手势键盘
                EmployeeWechatDTO dto = paswordService.savePassword(wechatId, password, type);
            } else if ("0".equals(type)) {
                //数字键盘
                String password1 = checkNumberPassword(password, wechatId);
                EmployeeWechatDTO dto = paswordService.savePassword(wechatId, password1, type);
            } else {
                log.info("=====> 校验失败，密码类型异常(0：数字密码  1：手势密码) type = {}", type);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码类型异常"));
            }

            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 用户登录
     *
     * @param password 密码   （数字密码以“,”分隔）
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
        String wechatId = String.valueOf(currentUser.getWechatId());
        String idNumberEncrytor = currentUser.getIdNumberEncrytor();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("=====> /admin/login 用户登录 wechatId:{}, password:{}，type{}", wechatId, password, type);

            Optional.ofNullable(type).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码类型’参数不能为空")));
            Optional.ofNullable(password).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码’参数不能为空")));

            //密码校验
            if ("1".equals(type)) {
                //手势键盘
                EmployeeWechatDTO dto = paswordService.checkPassword(wechatId, password, type);
            } else if ("0".equals(type)) {
                //数字键盘
                String password1 = checkNumberPassword(password, wechatId);
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
     * 数字密码键盘生成
     *
     * @return
     */
    @GetMapping("/crateNumericKeypad")
    @TrackLog
    @PermitAll
    public Mono<String> crateNumericKeypad() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = String.valueOf(WebContext.getCurrentUser().getWechatId());
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到登录用户可用标识")));

            //生成密码键盘ID
            String keyboardId = PayrollDBConstant.PREFIX + ":numberKeyboard:" + wechatId;
            log.info("=====> /admin/crateNumericKeypad 数字密码键盘生成 wechatId:{}", keyboardId);

            //生成密码键盘
            KeyboardResponse keyboardResponse = paswordService.crateNumericKeypad(keyboardId);

            //加入缓存
            try {
                log.info("crateNumericKeypad.redisKey:[{}]", keyboardId);
                redisTemplate.opsForValue().set(KEYBOARD_KEY.replace("{KEYBOARDID}", keyboardId), JacksonUtil.objectToJson(keyboardResponse.getNumber()), 1, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("密码键盘入缓存失败:[{}]", e.getMessage());
            }

            return keyboardResponse.getNumberBase64();
        }).subscribeOn(Schedulers.elastic());
    }
}

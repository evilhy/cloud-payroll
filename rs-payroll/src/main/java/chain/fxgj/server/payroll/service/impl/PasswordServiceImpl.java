package chain.fxgj.server.payroll.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.constant.PayrollDBConstant;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.handpassword.HandPasswordDTO;
import chain.fxgj.server.payroll.dto.request.KeyboardReqDTO;
import chain.fxgj.server.payroll.dto.response.KeyboardResDTO;
import chain.fxgj.server.payroll.service.PaswordService;
import chain.ids.client.feign.KeyboardFeign;
import chain.ids.core.commons.enums.EnumKeyboardType;
import chain.payroll.client.feign.EmployeeWechatFeignController;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.kkutils.ImageUtils;
import chain.utils.kkutils.Utils;
import chain.utils.passwrod.image.ImageInfo;
import chain.utils.passwrod.image.maker.AlphabetPasswordImageMaker;
import chain.utils.passwrod.image.maker.NumberPasswordImageMaker;
import core.dto.request.wechat.EmployeeWechatSaveReq;
import core.dto.response.wechat.EmployeeWechatDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: du
 * @Date: 2020/8/11 20:48
 */
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class PasswordServiceImpl implements PaswordService {

    @Autowired
    EmployeeWechatFeignController employeeWechatFeignController;
    @Autowired
    KeyboardFeign keyboardFeign;
    @Resource
    RedisTemplate redisTemplate;
    @Resource
    RedisOperations<String, String> redisOperations;
    /**
     * 有效期60分
     */
    private static final int KEYBOARD_TIME_OUT = 10;

    private static String KEYBOARD_KEY = "keyboard_{KEYBOARDID}";

    /**
     * 密码键盘 宽度
     */
    private final int WIDTH = 750;

    @Override
    public HandPasswordDTO queryHandPassword(String wechatId) {
        Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘用户微信绑定ID’参数不能为空")));

        EmployeeWechatDTO dto = employeeWechatFeignController.findById(wechatId);

        if (null == dto) {
            log.info("=====> wechatId:{} 用户微信绑定信息不存在!", wechatId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户微信绑定信息不存在"));
        }
        HandPasswordDTO handPasswordDTO = HandPasswordDTO.builder()
                .status(0)
                .statusVal("未开启")
                .build();
        if (StringUtils.isNotBlank(dto.getHandPassword())) {
            handPasswordDTO.setStatus(1);
            handPasswordDTO.setStatusVal("已开启");
        }
        return handPasswordDTO;
    }

    @Override
    public EmployeeWechatDTO checkPassword(String wechatId, String password, String type) {
        Optional.ofNullable(password).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘数字密码’参数不能为空")));
        Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘用户微信绑定ID’参数不能为空")));
        Optional.ofNullable(type).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码类型’参数不能为空")));

        //是否已锁定
        checkTimeRedisKey(wechatId, type);

        EmployeeWechatDTO dto = employeeWechatFeignController.findById(wechatId);
        if (null == dto) {
            log.info("=====> wechatId:{} 用户微信绑定信息不存在!", wechatId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户微信绑定信息不存在"));
        }

        if ("0".equals(type)) {
            //数字密码校验
            if (!password.equals(dto.getQueryPwd())) {
                log.info("=====> wechatId:{} 用户数字密码校验失败：queryPwd：{}，password：{}", wechatId, password);
//                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("数字密码校验失败"));
                //缓存错误次数
                this.checkPasswordRedisKey(wechatId, type);
            }
        } else if ("1".equals(type)) {
            //手势密码校验
            if (!password.equals(dto.getHandPassword())) {
                log.info("=====> wechatId:{} 用户手密码校验失败：queryPwd：{}，password：{}", wechatId, password);
//                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("手势密码校验失败"));
                //缓存错误次数
                this.checkPasswordRedisKey(wechatId, type);
            }
        } else {
            log.info("=====> 校验失败，密码类型异常(0：数字密码  1：手势密码) type = {}", type);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码类型异常"));
        }
        return dto;
    }

    @Override
    public EmployeeWechatDTO savePassword(String wechatId, String password, String type) {
        Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘用户微信绑定ID’参数不能为空")));
        Optional.ofNullable(password).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘确认密码’参数不能为空")));

        EmployeeWechatDTO dto = employeeWechatFeignController.findById(wechatId);
        if (null == dto) {
            log.info("=====> wechatId:{} 用户微信绑定信息不存在!", wechatId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户微信绑定信息不存在"));
        }

        EmployeeWechatSaveReq saveReq = EmployeeWechatSaveReq.builder()
                .updDateTime(LocalDateTime.now())
                .wechatId(dto.getWechatId())
                .build();

        if ("0".equals(type)) {
            //数字密码校验
            char[] chars = password.toCharArray();
            //是否同一字符
            boolean same = true;
            for (int i = 0; i < chars.length - 1; i++) {
                if (chars[i] != (chars[i + 1])) {
                    same = false;
                    break;
                }
            }
            if (same) {
                throw new ParamsIllegalException(ErrorConstant.PASSWORDMASE.getErrorMsg());
            }

            //是否正序连续
            same = true;
            for (int i = 0; i < chars.length - 1; i++) {
                int i1 = Integer.parseInt(String.valueOf(chars[i])) + 1;
                int i2 = Integer.parseInt(String.valueOf(chars[i + 1]));
                if (i1 != i2) {
                    same = false;
                    break;
                }
            }
            if (same) {
                throw new ParamsIllegalException(ErrorConstant.PASSWORDMASE.getErrorMsg());
            }
            //是否倒序连续
            same = true;
            for (int i = 0; i < chars.length - 1; i++) {
                int i1 = Integer.parseInt(String.valueOf(chars[i])) - 1;
                int i2 = Integer.parseInt(String.valueOf(chars[i + 1]));
                if (i1 != i2) {
                    same = false;
                    break;
                }
            }
            if (same) {
                throw new ParamsIllegalException(ErrorConstant.PASSWORDMASE.getErrorMsg());
            }
            //新增包含密码校验，手机号、身份证
            String phone = dto.getPhone();
            String idNumber = dto.getIdNumber();
            log.info("密码包含校验phone:[{}], idNumber:[{}], password:[{}]", phone, idNumber, password);
            same = true;
            if (StringUtils.isNotBlank(phone)) {
                if (phone.contains(password)) {
                    throw new ParamsIllegalException(ErrorConstant.PASSWORDCONTAINS.getErrorMsg());
                }
            }
            if (StringUtils.isNotBlank(idNumber)) {
                if (idNumber.contains(password)) {
                    throw new ParamsIllegalException(ErrorConstant.PASSWORDCONTAINS.getErrorMsg());
                }
            }
            saveReq.setQueryPwd(password);
        } else if ("1".equals(type)) {
            //手势密码校验
            saveReq.setHandPassword(password);
        } else {
            log.info("=====> 校验失败，密码类型异常(0：数字密码  1：手势密码) type = {}", type);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码类型异常"));
        }
        return employeeWechatFeignController.save(saveReq);
    }

    @Override
    public EmployeeWechatDTO closeHandPassword(String wechatId) {
        Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘用户微信绑定ID’参数不能为空")));

        EmployeeWechatDTO dto = employeeWechatFeignController.findById(wechatId);
        if (null == dto) {
            log.info("=====> wechatId:{} 用户微信绑定信息不存在!", wechatId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户微信绑定信息不存在"));
        }

        EmployeeWechatSaveReq saveReq = EmployeeWechatSaveReq.builder()
                .updDateTime(LocalDateTime.now())
                .wechatId(dto.getWechatId())
                .handPassword("")
                .build();
        return employeeWechatFeignController.save(saveReq);
    }

    @Override
    public KeyboardResDTO crateNumericKeypad(String keyboardId) {
        KeyboardReqDTO keyboardRequest = KeyboardReqDTO.builder()
                .keyboardType(EnumKeyboardType.number)
                .keyboardId(keyboardId)
                .build();
//        KeyboardResponse keyboard = keyboardFeign.createKeyboard(keyboardRequest); //调用ids项目
        EnumKeyboardType keyboardType = keyboardRequest.getKeyboardType();
        KeyboardResDTO keyboard = null;
        switch (keyboardType) {
            case letter:
                //字母键盘
                log.info("====>字母键盘");
                keyboard = letterCreate(keyboardRequest);
                break;
            case number:
                log.info("====>纯数字键盘");
                //纯数字键盘
                keyboard = numberCreate(keyboardRequest);
                break;
            case idcard:
                //身份证键盘
                log.info("====>身份证键盘");
                keyboard = idcardCreate(keyboardRequest);
                break;
            case money:
                //金额键盘
                log.info("====>金额键盘");
                break;
        }

        if (null == keyboard || null == keyboard.getNumber()) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("密码键盘生成失败"));
        }
        return keyboard;
    }

    @Override
    public String checkNumberPassword(String passsword, String wechatId) {
        //生成密码键盘ID
        String keyboardId = PayrollDBConstant.PREFIX + ":numberKeyboard:" + wechatId;

        //是否已锁定
        checkTimeRedisKey(wechatId, "0");

        //取出缓存
        String redisStr = null;
        try {
            log.info("crateNumericKeypad.redisKey:[{}]", keyboardId);
            redisStr = redisTemplate.opsForValue().get(keyboardId).toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("密码键盘读取缓存国企:[{}]", e.getMessage());
        }

        if (null == redisStr) {
            log.error("密码键盘读取缓存失败 wechatId:{}， redisKey:{} redisValue:{}", wechatId, keyboardId, redisStr);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("页面缓存已过期，请重新刷新"));
        }

        Map<String, Character> number = (Map<String, Character>) JacksonUtil.jsonToMap(redisStr);
        String[] split = passsword.split(",");
        if (null == split || split.length <= 0) {
//                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码不能为空"));
            //缓存错误次数
            this.checkPasswordRedisKey(wechatId, "0");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
//                Character character = number.get(split[i]);
            String s = split[i];
            sb = sb.append(number.get(s));
        }
        return sb.toString();
    }

    /**
     * 字母键盘
     *
     * @param keyboardRequest
     * @return
     */
    private KeyboardResDTO letterCreate(KeyboardReqDTO keyboardRequest) {
        KeyboardResDTO keyboard = null;

        Boolean topLogo = keyboardRequest.getTopLogo();
        Boolean shuffle = keyboardRequest.getShuffle();
        String keyboardId = keyboardRequest.getKeyboardId();

        //[1]产生a ~ z的字符
        char[] alphabetLower = Utils.getChars(shuffle);
        Map<String, Character> lower = Utils.getkeyboard(alphabetLower, "L");
        AlphabetPasswordImageMaker lowercaseMaker = new AlphabetPasswordImageMaker();
        lowercaseMaker.setUppercase(false);
        ImageInfo lowercaseImage = lowercaseMaker.make(WIDTH, topLogo, alphabetLower);

        //[2]产生A ~ Z的字符
        char[] alphabetUpper = Utils.getUpperChars(shuffle);
        Map<String, Character> upper = Utils.getkeyboard(alphabetUpper, "U");
        AlphabetPasswordImageMaker uppercaseMaker = new AlphabetPasswordImageMaker();
        uppercaseMaker.setUppercase(true);
        ImageInfo uppercaseImage = uppercaseMaker.make(WIDTH, topLogo, alphabetUpper);

        //[3]随机 产生 0 ~ 9的数字
        char[] alphabetNumber = Utils.getNumbers(shuffle);
        Map<String, Character> number = Utils.getkeyboard(alphabetNumber, "N");
        NumberPasswordImageMaker maker = new NumberPasswordImageMaker();
        ImageInfo imageNumber = maker.makeNumberCharLeft(WIDTH, topLogo, alphabetNumber);

        try {
            String lowercaseBase64 = Utils.toBase64(ImageUtils.toPNG(lowercaseImage)).replaceAll("\n", "");
            String uppercaseBase64 = Utils.toBase64(ImageUtils.toPNG(uppercaseImage)).replaceAll("\n", "");
            String numberBase64 = Utils.toBase64(ImageUtils.toPNG(imageNumber)).replaceAll("\n", "");
            keyboard = KeyboardResDTO.builder()
                    .keyboardId(keyboardRequest.getKeyboardId())
                    .keyboardType(keyboardRequest.getKeyboardType())
                    .shuffle(keyboardRequest.getShuffle())
                    .lowercaseBase64(lowercaseBase64)
                    .uppercaseBase64(uppercaseBase64)
                    .numberBase64(numberBase64)
                    .build();

            Map<String, Character> alphabetMap = new HashMap<>();
            alphabetMap.putAll(lower);
            alphabetMap.putAll(upper);
            alphabetMap.putAll(number);

            keyboard.setLower(lower);
            keyboard.setUpper(upper);
            keyboard.setNumber(number);
            redisOperations.opsForValue().set(KEYBOARD_KEY.replace("{KEYBOARDID}", keyboardId), JacksonUtil.objectToJson(alphabetMap), KEYBOARD_TIME_OUT, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("====>[字母键盘]失败");
            e.printStackTrace();
        }
        return keyboard;
    }

    /**
     * 纯数字键盘
     *
     * @param keyboardRequest
     * @return
     */
    private KeyboardResDTO numberCreate(KeyboardReqDTO keyboardRequest) {
        KeyboardResDTO keyboard = null;

        Boolean topLogo = keyboardRequest.getTopLogo();
        Boolean shuffle = keyboardRequest.getShuffle();
        String keyboardId = keyboardRequest.getKeyboardId();

        //随机 产生 0 ~ 9的数字
        char[] alphabet = Utils.getNumbers(shuffle);
        Map<String, Character> alphabetMap = Utils.getkeyboard(alphabet, "N");

        //纯数字键盘
        NumberPasswordImageMaker maker = new NumberPasswordImageMaker();
        //准备绘图


        ImageInfo image = null;
        if (keyboardRequest.getConfirm()) {
            image = maker.makeNumberConfirmLeft(WIDTH, topLogo, alphabet);
        } else {
            image = maker.makeNumberConfirmNon(WIDTH, topLogo, alphabet);
        }

        try {
            //转成BASE64
            String numberBase64 = Utils.toBase64(ImageUtils.toPNG(image)).replaceAll("\n", "");

            keyboard = KeyboardResDTO.builder()
                    .keyboardId(keyboardRequest.getKeyboardId())
                    .keyboardType(keyboardRequest.getKeyboardType())
                    .shuffle(keyboardRequest.getShuffle())
                    .numberBase64(numberBase64)
                    .build();

            keyboard.setNumber(alphabetMap);
            redisOperations.opsForValue().set(KEYBOARD_KEY.replace("{KEYBOARDID}", keyboardId), JacksonUtil.objectToJson(alphabetMap), KEYBOARD_TIME_OUT, TimeUnit.MINUTES);

        } catch (Exception e) {
            log.error("====>[纯数字键盘]失败");
            e.printStackTrace();
        }
        return keyboard;
    }

    /**
     * 身份证键盘
     *
     * @param keyboardRequest
     * @return
     */
    private KeyboardResDTO idcardCreate(KeyboardReqDTO keyboardRequest) {
        KeyboardResDTO keyboard = null;

        Boolean topLogo = keyboardRequest.getTopLogo();
        Boolean shuffle = keyboardRequest.getShuffle();
        String keyboardId = keyboardRequest.getKeyboardId();

        //产生身份证数据   0-9 ，X
        char[] alphabet = Utils.getIdCard(shuffle);
        Map<String, Character> alphabetMap = Utils.getkeyboard(alphabet, "N");

        NumberPasswordImageMaker maker = new NumberPasswordImageMaker();
        ImageInfo image = maker.makeIdCardChar(WIDTH, topLogo, alphabet);
        try {
            String numberBase64 = Utils.toBase64(ImageUtils.toPNG(image)).replaceAll("\n", "");

             keyboard = KeyboardResDTO.builder()
                    .keyboardId(keyboardRequest.getKeyboardId())
                    .keyboardType(keyboardRequest.getKeyboardType())
                    .shuffle(keyboardRequest.getShuffle())
                    .numberBase64(numberBase64)
                    .build();

            keyboard.setIdCard(alphabetMap);
            redisOperations.opsForValue().set(KEYBOARD_KEY.replace("{KEYBOARDID}", keyboardId), JacksonUtil.objectToJson(alphabetMap), KEYBOARD_TIME_OUT, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("====>[身份证键盘]失败");
            e.printStackTrace();
        }

        return keyboard;
    }
    /**
     * 金额键盘
     *
     * @param keyboardRequest
     * @return
     */
    private KeyboardResDTO moneyCreate(KeyboardReqDTO keyboardRequest) {
        return null;
    }

    /**
     * 缓存密码错误次数
     *
     * @param wechatId 登录标识
     * @param type     密码类型 0：数字密码  1：手势密码
     */
    public void checkPasswordRedisKey(String wechatId, String type) {
        Integer usedTimes = 0;
        Integer usedDailyTimes = 0;
        String redisKey = null;
        String redisDailyKey = null;
        if ("0".equals(type)) {
            //对同一手机号一小时内进行次数限制。一小时最多10次
            redisKey = "tiger:number:limit:".concat(wechatId);
            usedTimes = this.setRedisKey(redisKey, 1, 10, ErrorConstant.PASSWORDLIMITERR);
            //对同一手机号一天内进行次数限制。一天最多20次
            redisDailyKey = "tiger:number:dailyLimit:".concat(wechatId);
            usedDailyTimes = this.setRedisKey(redisDailyKey, 24, 20, ErrorConstant.PASSWORDCHECKERR);
        } else if ("1".equals(type)) {
            //对同一手机号一小时内进行次数限制。一小时最多10次
            redisKey = "tiger:hand:limit:".concat(wechatId);
            usedTimes = this.setRedisKey(redisKey, 1, 10, ErrorConstant.PASSWORDLIMITERR);
            //对同一手机号一天内进行次数限制。一天最多20次
            redisDailyKey = "tiger:hand:dailyLimit:".concat(wechatId);
            usedDailyTimes = this.setRedisKey(redisDailyKey, 24, 20, ErrorConstant.PASSWORDCHECKERR);
        }

        if (17 == usedDailyTimes || 18 == usedDailyTimes || 19 == usedDailyTimes) {
            throw new ParamsIllegalException(ErrorConstant.PASSWORDCHECKERR.format(20 - usedDailyTimes));
        }
        if (usedDailyTimes == 20) {
            checkRedisTime(redisDailyKey);
        }

        if (7 == usedTimes || 8 == usedTimes || 9 == usedTimes) {
            throw new ParamsIllegalException(ErrorConstant.PASSWORDLIMITERR.format(10 - usedTimes));
        }
        if (usedDailyTimes == 10) {
            checkRedisTime(redisKey);
        }
        throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("密码错误，请核对后重试！"));
    }

    /**
     * 密码错误次数缓存
     *
     * @param redisKey      redis 缓存KEY
     * @param limitTime     控制时间（单位：小时）
     * @param maxTimes      最大错误次数
     * @param errorConstant 错误信息
     */
    private Integer setRedisKey(String redisKey, Integer limitTime, Integer maxTimes, ErrorConstant errorConstant) {
        Integer usedTimes = (Integer) redisTemplate.opsForValue().get(redisKey);
        if (usedTimes == null) {
            redisTemplate.opsForValue().set(redisKey, 1, limitTime, TimeUnit.HOURS);
            return 1;
        } else {
//            if (usedTimes.equals(maxTimes)) {
//                if (1 == limitTime) {
//                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("错误次数过多，请于59分59秒后尝试"));
//                }
//                if (24 == limitTime) {
//                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("错误次数已达上限，请于23时59分59秒后尝试"));
//                }
//            } else {
            usedTimes = usedTimes + 1;
            redisTemplate.opsForValue().set(redisKey, usedTimes, limitTime, TimeUnit.HOURS);
//            }
            return usedTimes;
        }
    }

    /**
     * 是否锁定
     *
     * @param wechatId 登录标识
     * @param type     密码类型 0：数字密码  1：手势密码
     */
    private void checkTimeRedisKey(String wechatId, String type) {
        if ("0".equals(type)) {
            //数字键盘
            //24小时内是否超过20次
            String dailyRedisKey = "tiger:number:dailyLimit:".concat(wechatId);
            Integer usedTimes = (Integer) redisTemplate.opsForValue().get(dailyRedisKey);
            if (null != usedTimes && 20 <= usedTimes) {
                checkRedisTime(dailyRedisKey);
            }
            //1小时内是否有超过10次
            String redisKey = "tiger:number:limit:".concat(wechatId);
            usedTimes = (Integer) redisTemplate.opsForValue().get(redisKey);
            if (null != usedTimes && 10 <= usedTimes) {
                checkRedisTime(redisKey);
            }
        } else if ("1".equals(type)) {
            //手势键盘
            //24小时内是否超过20次
            String dailyRedisKey = "tiger:hand:dailyLimit:".concat(wechatId);
            Integer usedTimes = (Integer) redisTemplate.opsForValue().get(dailyRedisKey);
            if (null != usedTimes && 20 <= usedTimes) {
                checkRedisTime(dailyRedisKey);
            }
            //1小时内是否有超过10次
            String redisKey = "tiger:hand:limit:".concat(wechatId);
            usedTimes = (Integer) redisTemplate.opsForValue().get(redisKey);
            if (null != usedTimes && 10 <= usedTimes) {
                checkRedisTime(redisKey);
            }

        }
    }

    /**
     * 锁定时间
     *
     * @param redisKey
     */
    private void checkRedisTime(String redisKey) {
        Integer usedTimes = (Integer) redisTemplate.opsForValue().get(redisKey);
        if (usedTimes != null) {
            long expire = redisTemplate.getExpire(redisKey);
            log.debug("=================>ipRedisKey.expire:{}", expire);
            long hour = expire / (60 * 60);
            long minute = (expire - hour * 60 * 60) / 60;
            long second = (expire - hour * 60 * 60 - minute * 60);
            StringBuilder sb = new StringBuilder();
            if (hour != 0) {
                sb.append(hour + "时");
            }
            if (minute != 0 || (hour != 0 && minute == 0)) {
                sb.append(minute + "分");
            }
            sb.append(second + "秒");
            throw new ParamsIllegalException(ErrorConstant.PASSWORD.format(sb.toString()));
        }
    }
}

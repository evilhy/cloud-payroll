package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.exception.ServiceHandleException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.feign.client.InsideFeignService;
import chain.fxgj.feign.dto.request.WageReqPhone;
import chain.fxgj.server.payroll.dto.securities.request.ReqRewardDTO;
import chain.fxgj.server.payroll.dto.securities.request.ReqSecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.*;
import chain.fxgj.server.payroll.service.SecuritiesService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.pub.client.feign.CaptchaFeignClient;
import chain.pub.common.dto.captcha.CaptchaDTO;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import chain.wisales.core.constant.dictEnum.SecuritiesPlatformEnum;
import chain.wisales.core.constant.dictEnum.StandardEnum;
import chain.wisales.core.dto.securities.SecuritiesInvitationAwardDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 证券开户活动
 */
@RestController
@Validated
@RequestMapping(value = "/securityes")
@Slf4j
@SuppressWarnings("unchecked")
public class SecuritiesController {

    @Resource
    RedisTemplate redisTemplate;
    @Autowired
    SecuritiesService securitiesService;
    @Autowired
    WechatRedisService wechatRedisService;
    @Autowired
    InsideFeignService insideFeignService;
    @Autowired
    CaptchaFeignClient captchaFeignClient;

    /**
     * 登录校验
     * @return
     */
    @GetMapping("/loginCheck")
    @TrackLog
    public Mono<SecuritiesRedisDTO> loginCheck(@RequestParam("code") String code) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String jsessionId = UUIDUtil.createUUID32();

            //【一】根据code获取 openId、accessToken
            WechatGroupEnum wechatGroup = WechatGroupEnum.FXGJ;
            log.info("wechatGroup:[{}][{}], code:[{}]", wechatGroup.getId(), wechatGroup.getDesc(), code);
            AccessTokenDTO accessTokenDTO = wechatRedisService.oauth2AccessToken(wechatGroup, code);
            log.info("accessTokenDTO:[{}]", JacksonUtil.objectToJson(accessTokenDTO));
            String openId = accessTokenDTO.getOpenid();
            String accessToken = accessTokenDTO.getAccessToken();
            if (chain.utils.commons.StringUtils.isEmpty(openId)) {
                throw new ParamsIllegalException(chain.fxgj.server.payroll.constant.ErrorConstant.AUTH_ERR.getErrorMsg());
            }

            //用openId查询唯销是否已登录，同时放入redis
            SecuritiesRedisDTO securitiesRedisDTO = securitiesService.qrySecuritiesCustInfo(jsessionId, openId);
            log.info("securitiesRedisDTO:[{}]", JacksonUtil.objectToJson(securitiesRedisDTO));

            Integer loginStatus = securitiesRedisDTO.getLoginStatus();
            if (loginStatus == 0) {
                //未登录
                //【二】根据 openId、accessToken 获取用户信息
                UserInfoDTO userInfo = wechatRedisService.getUserInfo(accessToken, openId);
                String nickName = userInfo.getNickname();
                log.info("userInfo:[{}]", JacksonUtil.objectToJson(userInfo));
                if (null == userInfo || chain.utils.commons.StringUtils.isEmpty(nickName)) {
                    log.info("根据openId、accessToken获取用户信息失败");
                }
                securitiesRedisDTO.setNickname(nickName);
                securitiesRedisDTO.setHeadimgurl(userInfo.getHeadimgurl());
                securitiesRedisDTO.setSex(String.valueOf(userInfo.getSex()));
                securitiesRedisDTO.setCountry(userInfo.getCountry());
                securitiesRedisDTO.setCity(userInfo.getCity());
                //更新缓存
                securitiesService.upSecuritiesRedis(jsessionId, securitiesRedisDTO);
                //todo 唯销没有值 ，再根据openId，查询本地Mysql 微信表，有数据则返回 jsessionId、手机号
            }
            return securitiesRedisDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 证券登录
     *
     * @param reqSecuritiesLoginDTO
     */
    @PostMapping("/securitiesLogin")
    @TrackLog
    public Mono<ResSecuritiesLoginDTO> securitiesLogin(@RequestBody ReqSecuritiesLoginDTO reqSecuritiesLoginDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        log.info("securitiesLogin.reqSecuritiesLoginDTO:[{}]", JacksonUtil.objectToJson(reqSecuritiesLoginDTO));
        String phone = reqSecuritiesLoginDTO.getPhone();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //1.短信验证码校验是否通过
            WageReqPhone wageReqPhone = new WageReqPhone();
            wageReqPhone.setCode(reqSecuritiesLoginDTO.getMsgCode());
            wageReqPhone.setCodeId(reqSecuritiesLoginDTO.getMsgCodeId());
            wageReqPhone.setPhone(phone);
            String retStr = insideFeignService.checkPhoneCode(wageReqPhone);
            if (!StringUtils.equals("0000", retStr)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format(retStr));
            }

            //2.从缓存取数据入库
            String jsessionId = reqSecuritiesLoginDTO.getJsessionId();
            SecuritiesRedisDTO securitiesRedisDTO = securitiesService.qrySecuritiesRedis(jsessionId);

            //3.唯销入库
            securitiesRedisDTO.setPhone(phone);
            String customerId = reqSecuritiesLoginDTO.getCustomerId();
            String invitationId = reqSecuritiesLoginDTO.getInvitationId();
            securitiesRedisDTO.setInvitationId(invitationId);
            securitiesRedisDTO.setCustomerId(customerId);
            String custId = securitiesService.securitiesLogin(securitiesRedisDTO);

            ResSecuritiesLoginDTO resSecuritiesLoginDTO = new ResSecuritiesLoginDTO();
            resSecuritiesLoginDTO.setCustId(custId);
            resSecuritiesLoginDTO.setLoginStatus(1);
            return resSecuritiesLoginDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 邀请奖励列表查询
     * @param custIdOrManagerId
     * @return
     */
    @GetMapping("/qryInvitationAward")
    @TrackLog
    public Mono<List<SecuritiesInvitationAwardDTO>> qryInvitationAward(@RequestParam String custIdOrManagerId) {

        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("custIdOrManagerId:[{}]", custIdOrManagerId);
            List<SecuritiesInvitationAwardDTO> securitiesInvitationAwardDTOList = securitiesService.qryInvitationAward(custIdOrManagerId);
            log.info("securitiesInvitationAwardDTOList:[{}]", JacksonUtil.objectToJson(securitiesInvitationAwardDTOList));
            return securitiesInvitationAwardDTOList;

        }).subscribeOn(Schedulers.elastic());
    }




    //----以下未开发
    /**
     * 查询金豆个数
     * @return
     */
    @GetMapping("/qryGoldenBean")
    @TrackLog
    public Mono<BigDecimal> qryGoldenBean(@RequestParam(value = "custId") AppPartnerEnum custId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            BigDecimal goldenBean = BigDecimal.ZERO;
            return goldenBean;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 查询更新时间
     * @return
     */
    @GetMapping("/qryDataSynTime")
    @TrackLog
    public Mono<Long> qryDataSynTime(@RequestParam(value = "custId") AppPartnerEnum custId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            long dataSynTime = 10000000000L;
            return dataSynTime;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 查询开户奖励列表
     * @return
     */
    @GetMapping("/qryOpenRewardList")
    @TrackLog
    public Mono<List<SecuritiesOpenRewardDTO>> qryOpenRewardList(@RequestParam(value = "custId") String custId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<SecuritiesOpenRewardDTO> list = new ArrayList<>();
            return list;
        }).subscribeOn(Schedulers.elastic());
    }
    /**
     * 查询被邀请人列表
     * @return
     */
    @GetMapping("/qryBeInvitedList")
    @TrackLog
    public Mono<List<SecuritiesBeInvitedDTO>> qryBeInvitedList(@RequestParam(value = "custId") String custId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<SecuritiesBeInvitedDTO> list = new ArrayList<>();
            SecuritiesBeInvitedDTO securitiesBeInvitedDTO = new SecuritiesBeInvitedDTO();
            list.add(securitiesBeInvitedDTO);
            return list;
        }).subscribeOn(Schedulers.elastic());
    }
    /**
     * 投资奖励列表
     * @return
     */
    @PostMapping("/qryInvestmentRewardList")
    @TrackLog
    public Mono<List<SecuritiesInvestmentRewardDTO>> qryBeInvitedList(@RequestBody ReqRewardDTO reqRewardDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<SecuritiesInvestmentRewardDTO> list = new ArrayList<>();
            SecuritiesInvestmentRewardDTO securitiesInvestmentRewardDTO = new SecuritiesInvestmentRewardDTO();
            list.add(securitiesInvestmentRewardDTO);
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 拖拽式滑块图形验证
     *
     * @return
     */
    @GetMapping("/pictureCheck")
    @TrackLog
    public Mono<String> pictureCheck() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //todo 拖拽式滑块图形验证

            return "";
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 查询分享携带信息
     *
     * @return
     */
    @GetMapping("/shareInfo")
    @TrackLog
    public Mono<String> shareInfo() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            return principal.getCustId();
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 生成验证返回图片base64
     *
     * @return
     */
    @GetMapping("/imageCaptcha")
    @TrackLog
    public Mono<CaptchaDTO> imageCaptcha() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CaptchaDTO captcha = captchaFeignClient.captcha();
            log.info("captcha:[{}]", JacksonUtil.objectToJson(captcha));
            return captcha;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 验证验证码是否正确
     *
     * @return
     */
    @GetMapping("/imageValidate")
    @TrackLog
    public Mono<Void> imageValidate(@RequestParam("imageCodeId") String imageCodeId, @RequestParam("code") String code) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("imageCodeId:[{}], code:[{}]", imageCodeId, code);
            captchaFeignClient.validate(imageCodeId, code);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

}

package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.exception.ServiceHandleException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.feign.client.InsideFeignService;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.securities.request.ReqSecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.ResSecuritiesLoginDTO;
import chain.fxgj.server.payroll.dto.securities.response.SecuritiesRedisDTO;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.SecuritiesService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.fxgj.server.payroll.util.SensitiveInfoUtils;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.InsideFeignController;
import chain.pub.client.feign.CaptchaFeignClient;
import chain.pub.common.dto.captcha.CaptchaDTO;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import chain.wage.manager.core.dto.request.WageReqPhone;
import chain.wisales.core.constant.dictEnum.UserTypeEnum;
import chain.wisales.core.dto.securities.*;
import chain.wisales.core.dto.securitiesIntegral.SecuritiesIntegralRewardDTO;
import core.dto.request.ReqPhone;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * ??????????????????
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
    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    InsideFeignController insideFeignController;

    /**
     * ????????????-???????????????????????????
     * @return
     */
    @GetMapping("/loginCheck")
    @TrackLog
    public Mono<SecuritiesRedisDTO> loginCheck(@RequestParam("code") String code,
        @RequestHeader(value = "encry-salt", required = false) String salt,
        @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String jsessionId = UUIDUtil.createUUID32();

            //???????????????code?????? openId???accessToken
            WechatGroupEnum wechatGroup = WechatGroupEnum.FXGJ;
            log.info("wechatGroup:[{}][{}], code:[{}]", wechatGroup.getId(), wechatGroup.getDesc(), code);
            AccessTokenDTO accessTokenDTO = wechatRedisService.oauth2AccessToken(wechatGroup, code);
            log.info("accessTokenDTO:[{}]", JacksonUtil.objectToJson(accessTokenDTO));
            String openId = accessTokenDTO.getOpenid();
            String accessToken = accessTokenDTO.getAccessToken();
            if (chain.utils.commons.StringUtils.isEmpty(openId)) {
                throw new ParamsIllegalException(chain.fxgj.server.payroll.constant.ErrorConstant.AUTH_ERR.getErrorMsg());
            }

            //???openId??????????????????????????????????????????redis
            SecuritiesRedisDTO securitiesRedisDTO = securitiesService.qrySecuritiesCustInfo(jsessionId, openId);
            log.info("securitiesRedisDTO:[{}]", JacksonUtil.objectToJson(securitiesRedisDTO));

            Integer loginStatus = securitiesRedisDTO.getLoginStatus();
            if (loginStatus == 0) {
                //?????????
                //??????????????? openId???accessToken ??????????????????
                UserInfoDTO userInfo = wechatRedisService.getUserInfo(accessToken, openId);
                String nickName = userInfo.getNickname();
                log.info("userInfo:[{}]", JacksonUtil.objectToJson(userInfo));
                if (null == userInfo || chain.utils.commons.StringUtils.isEmpty(nickName)) {
                    log.info("??????openId???accessToken????????????????????????");
                }
                securitiesRedisDTO.setNickname(nickName);
                securitiesRedisDTO.setHeadimgurl(userInfo.getHeadimgurl());
                securitiesRedisDTO.setSex(String.valueOf(userInfo.getSex()));
                securitiesRedisDTO.setCountry(userInfo.getCountry());
                securitiesRedisDTO.setCity(userInfo.getCity());
                //????????????
                securitiesService.upSecuritiesRedis(jsessionId, securitiesRedisDTO);
                //todo ??????????????? ????????????openId???????????????Mysql ?????????????????????????????? jsessionId????????????
            }
            // ?????????????????????
            securitiesRedisDTO.setPhone(EncrytorUtils.encryptField(securitiesRedisDTO.getPhone(), salt, passwd));
            securitiesRedisDTO.setSalt(salt);
            securitiesRedisDTO.setPasswd(passwd);
            log.info("loginCheck.securitiesRedisDTO:[{}]", JacksonUtil.objectToJson(securitiesRedisDTO));
            return securitiesRedisDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????-???????????????????????????
     * @return
     */
    @GetMapping("/loginCheckIn")
    @TrackLog
    public Mono<SecuritiesRedisDTO> loginCheckIn(@RequestParam("jsessionId") String jsessionId,
                                               @RequestHeader(value = "encry-salt", required = false) String salt,
                                               @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
            String openId = wechatInfoDetail.getOpenId();
            if (StringUtils.isEmpty(openId)) {
                throw new ParamsIllegalException(ErrorConstant.AUTH_ERR.getErrorMsg());
            }

            //???openId?????????????????????????????????????????? redis
            SecuritiesRedisDTO securitiesRedisDTO = securitiesService.qrySecuritiesCustInfo(jsessionId, openId);
            log.info("securitiesRedisDTO:[{}]", JacksonUtil.objectToJson(securitiesRedisDTO));

            Integer loginStatus = securitiesRedisDTO.getLoginStatus();
            if (loginStatus == 0) {
                //?????????
                String nickName = wechatInfoDetail.getNickname();
                securitiesRedisDTO.setNickname(nickName);
                securitiesRedisDTO.setHeadimgurl(wechatInfoDetail.getHeadimgurl());
                //todo ????????????????????????????????????CacheUserPrincipal???????????????????????????
//                securitiesRedisDTO.setSex(String.valueOf(userPrincipal.getSex()));
//                securitiesRedisDTO.setCountry(userPrincipal.getCountry());
//                securitiesRedisDTO.setCity(userPrincipal.getCity());
                //????????????
                securitiesService.upSecuritiesRedis(jsessionId, securitiesRedisDTO);
            }
            // ?????????????????????
            securitiesRedisDTO.setPhone(EncrytorUtils.encryptField(securitiesRedisDTO.getPhone(), salt, passwd));
            securitiesRedisDTO.setSalt(salt);
            securitiesRedisDTO.setPasswd(passwd);
            log.info("loginCheck.securitiesRedisDTO:[{}]", JacksonUtil.objectToJson(securitiesRedisDTO));
            return securitiesRedisDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    /**
     * ????????????
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

            String key ="inside_send"+reqSecuritiesLoginDTO.getPhone();
            String codeId = (String) redisTemplate.opsForValue().get(key);
            if(null ==codeId) {
                throw new ServiceHandleException(ErrorConstant.Error0004.format());
            }
            //1.?????????????????????????????????
            ReqPhone reqPhone = new ReqPhone();
            reqPhone.setCode(reqSecuritiesLoginDTO.getMsgCode());
            reqPhone.setCodeId(codeId);
            reqPhone.setPhone(phone);
            String retStr = insideFeignController.checkPhoneCode(reqPhone);
            if (!StringUtils.equals("0000", retStr)) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format(retStr));
            }

            //2.????????????????????????
            String jsessionId = reqSecuritiesLoginDTO.getJsessionId();
            SecuritiesRedisDTO securitiesRedisDTO = securitiesService.qrySecuritiesRedis(jsessionId);

            //3.????????????
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????????????????
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
            //?????????????????????
            List<SecuritiesInvitationAwardDTO> securitiesInvitationAwardDTONewList = new ArrayList<>();
            if (null != securitiesInvitationAwardDTOList && securitiesInvitationAwardDTOList.size() > 0) {
                for (SecuritiesInvitationAwardDTO securitiesInvitationAwardDTO : securitiesInvitationAwardDTOList) {
                    SecuritiesInvitationAwardDTO securitiesInvitationAwardDTONew = new SecuritiesInvitationAwardDTO();
                    BeanUtils.copyProperties(securitiesInvitationAwardDTO, securitiesInvitationAwardDTONew);
                    securitiesInvitationAwardDTONew.setPhoneNo(SensitiveInfoUtils.mobilePhonePrefix(securitiesInvitationAwardDTONew.getPhoneNo()));
                    securitiesInvitationAwardDTONewList.add(securitiesInvitationAwardDTONew);
                }
            }
            log.info("qryInvitationAward.securitiesInvitationAwardDTONewList:[{}]", JacksonUtil.objectToJson(securitiesInvitationAwardDTONewList));
            return securitiesInvitationAwardDTONewList;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ??????????????????
     * @return
     */
    @GetMapping("/qryGoldenBean")
    @TrackLog
    public Mono<BigDecimal> qryGoldenBean(@RequestParam(value = "custId") String custId,
                                          @RequestParam(value = "userType") String userType) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserTypeEnum userTypeEnum = null;
            if (StringUtils.equals("0", userType)) {
                userTypeEnum = UserTypeEnum.NORMAL;
            } else if (StringUtils.equals("1", userType)) {
                userTypeEnum = UserTypeEnum.MANAGER;
            }else {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("???????????????"));
            }
            BigDecimal goldenBean = securitiesService.qryGoldenBean(custId, userTypeEnum);
            return goldenBean;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ??????????????????
     * @return
     */
    @GetMapping("/qryDataSynTime")
    @TrackLog
    public Mono<Long> qryDataSynTime() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<SecuritiesDataSynTimeDTO> securitiesDataSynTimeDTOList = securitiesService.qryDataSynTimeList();

            //??????????????????????????????
            Collections.sort(securitiesDataSynTimeDTOList, new Comparator<SecuritiesDataSynTimeDTO>() {
                @Override
                public int compare(SecuritiesDataSynTimeDTO o1, SecuritiesDataSynTimeDTO o2) {
                    return o2.getDataSynTime().compareTo(o1.getDataSynTime());
                }
            });

            SecuritiesDataSynTimeDTO securitiesDataSynTimeDTO = securitiesDataSynTimeDTOList.get(0);
            Long dataSynTime = securitiesDataSynTimeDTO.getDataSynTime();
            // LocalDateTime ??? Long
//            Long dataSynTimeLong = dataSynTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return dataSynTime;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????????????????
     * @return
     */
    @GetMapping("/qryOpenRewardList")
    @TrackLog
    public Mono<List<SecuritiesOpenRewardDTO>> qryOpenRewardList(@RequestParam(value = "custId") String custId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<SecuritiesOpenRewardDTO> securitiesOpenRewardDTOList = securitiesService.qryOpenRewardList(custId);
            return securitiesOpenRewardDTOList;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ??????????????????
     * @return
     */
    @GetMapping("/qryInvestmentRewardList")
    @TrackLog
    public Mono<List<SecuritiesRewardResDTO>> qryInvestmentRewardList(@RequestParam String custId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<SecuritiesRewardResDTO> securitiesRewardResDTOList = securitiesService.qryInvestmentRewardList(custId);
            return securitiesRewardResDTOList;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????????????????
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????????????????base64
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ???????????????????????????
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
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ???????????????????????????????????????
     *
     * @param reqSecuritiesLoginDTO
     * @return
     */
    @PostMapping("/transferThirdPage")
    @TrackLog
    public Mono<String> transferThirdPage(@RequestBody ReqSecuritiesLoginDTO reqSecuritiesLoginDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        log.info("transferThirdPage.reqSecuritiesLoginDTO:[{}]",JacksonUtil.objectToJson(reqSecuritiesLoginDTO));
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String url = securitiesService.transferThirdPage(reqSecuritiesLoginDTO);
            return url;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????????????????????????????
     *
     * @param managerId ????????????ID
     * @return list
     */
    @GetMapping("/unAuth/querySecuritiesInvitation")
    @TrackLog
    public Mono<List<SecuritiesIntegralRewardDTO>> querySecuritiesInvitation(@RequestParam String managerId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<SecuritiesIntegralRewardDTO> securitiesLoginDTOList = securitiesService.querySecuritiesInvitation(managerId);
            return securitiesLoginDTOList;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * ????????????????????????????????????
     *
     * @param managerId ????????????ID
     * @return list
     */
    @GetMapping("/unAuth/queryInvitationIntegral")
    @TrackLog
    public Mono<List<SecuritiesIntegralRewardDTO>> queryInvitationIntegral(@RequestParam String managerId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<SecuritiesIntegralRewardDTO> securitiesLoginDTOList = securitiesService.queryInvitationIntegral(managerId);
            return securitiesLoginDTOList;
        }).subscribeOn(Schedulers.elastic());
    }
}

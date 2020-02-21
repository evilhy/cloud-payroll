package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.server.payroll.dto.securities.request.ReqRewardDTO;
import chain.fxgj.server.payroll.dto.securities.response.*;
import chain.fxgj.server.payroll.service.SecuritiesService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
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
import java.net.URLEncoder;
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
            securitiesService.pictureCheck();

            return "";
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 登录校验
     * @param code
     * @param appPartner
     * @return
     */
    @GetMapping("/loginCheck")
    @TrackLog
    public Mono<SecuritiesCustInfoDTO> loginCheck(@RequestParam("code") String code,
        @RequestParam(value = "appPartner", required = true, defaultValue = "FXGJ") AppPartnerEnum appPartner) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //【一】根据code获取openId、accessToken
//            WechatGroupEnum wechatGroup = WechatGroupEnum.valueOf(appPartner.name());
//            log.info("wechatGroup:[{}][{}], code:[{}]", wechatGroup.getId(), wechatGroup.getDesc(), code);
//            AccessTokenDTO accessTokenDTO = wechatRedisService.oauth2AccessToken(wechatGroup, code);
//            log.info("accessTokenDTO:[{}]", JacksonUtil.objectToJson(accessTokenDTO));
//            String openId = accessTokenDTO.getOpenid();
//            String accessToken = accessTokenDTO.getAccessToken();
//            if (chain.utils.commons.StringUtils.isEmpty(openId)) {
//                throw new ParamsIllegalException(chain.fxgj.server.payroll.constant.ErrorConstant.AUTH_ERR.getErrorMsg());
//            }
//            //【二】根据 openId、accessToken 获取用户信息
//            UserInfoDTO userInfo = wechatRedisService.getUserInfo(accessToken, openId);
//            String nickName = userInfo.getNickname();
//            String headImgurl = userInfo.getHeadimgurl();
//            log.info("userInfo:[{}]", JacksonUtil.objectToJson(userInfo));
//            if (null == userInfo || chain.utils.commons.StringUtils.isEmpty(userInfo.getNickname())) {
//                log.info("根据openId、accessToken获取用户信息失败");
//            } else {
//                try {
//                    nickName = URLEncoder.encode(userInfo.getNickname(), "UTF-8");
//                } catch (Exception e) {
//                    log.info("获取昵称出现异常！");
//                }
//                headImgurl = userInfo.getHeadimgurl();
//            }
            //todo 根据openId查询唯销用户信息，有值则组装数据，入缓存，并返回给前端
            //唯销没有值 ，查询本地Mysql 微信表，有数据则返回 jsessionId、手机号
            SecuritiesCustInfoDTO securitiesCustInfoDTO = new SecuritiesCustInfoDTO();
            securitiesCustInfoDTO.setJsessionId("123123");
            securitiesCustInfoDTO.setPhone("13400000000");
            securitiesCustInfoDTO.setLoginStatus(IsStatusEnum.YES.getCode());
            securitiesCustInfoDTO.setLoginStatusVal(IsStatusEnum.YES.getDesc());
            return securitiesCustInfoDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 证券登录
     *
     * @param securitiesLoginDTO
     */
    @PostMapping("/securitiesLogin")
    @TrackLog
    public Mono<Boolean> securitiesLogin(@RequestBody SecuritiesLoginDTO securitiesLoginDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //从缓存取数据入库
            Boolean loginSuccess = true;
            return loginSuccess;
        }).subscribeOn(Schedulers.elastic());
    }

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


}

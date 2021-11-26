package chain.fxgj.server.payroll.controller;

import chain.activity.dto.PageDTO;
import chain.activity.dto.mq.AccedeAnswerReq;
import chain.activity.dto.mq.AccedeRainReq;
import chain.activity.dto.request.activity.*;
import chain.activity.dto.response.activity.*;
import chain.activity.feign.ActivityAccedeFeignService;
import chain.activity.feign.ActivityStartFeignService;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.PageResponseDTO;
import chain.fxgj.server.payroll.dto.activity.*;
import chain.fxgj.server.payroll.dto.activity.convert.ActivityTransLogConvert;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.service.ActivityConfigureService;
import chain.fxgj.server.payroll.service.ActivityStartService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.dto.wechat.UserInfoDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import chain.utils.fxgj.constant.DictEnums.*;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 活动参与
 */
@RestController
@Validated
@RequestMapping(value = "/activityStart")
@Slf4j
@SuppressWarnings("unchecked")
public class ActivityStartRS {
    @Autowired
    ActivityStartService activityStartService;
    @Autowired
    ActivityStartFeignService activityStartFeignService;
    @Autowired
    ActivityAccedeFeignService activityAccedeFeignService;
    @Autowired
    WechatRedisService wechatRedisService;
    @Autowired
    ActivityConfigureService activityConfigureService;


    /**
     * 【通用接口】微信回调接口
     *
     * @param code
     * @param routeName
     * @return
     * @throws Exception
     */
    @GetMapping("/wxCallback")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<Res100705> wxCallback(@RequestParam("code") String code,
                                      @RequestHeader(value = "routeName", required = false) String routeName) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            AppPartnerEnum appPartner = AppPartnerEnum.FXGJ;

            String jsessionId = UUIDUtil.createUUID32();
            Res100705 res100705 = Res100705.builder()
                    .jsessionId(jsessionId)
                    .apppartner(appPartner)
                    .apppartnerDesc(appPartner.getDesc())
                    .build();
            if ("authdeny".equals(code)) {
                return res100705;
            }
            //【一】根据code获取openId、accessToken
            WechatGroupEnum wechatGroup = WechatGroupEnum.valueOf(appPartner.name());
            log.info("wechatGroup:[{}][{}], code:[{}]", wechatGroup.getId(), wechatGroup.getDesc(), code);
            AccessTokenDTO accessTokenDTO = wechatRedisService.oauth2AccessToken(wechatGroup, code);
            log.info("accessTokenDTO:[{}]", JacksonUtil.objectToJson(accessTokenDTO));
            String openId = accessTokenDTO.getOpenid();
            String accessToken = accessTokenDTO.getAccessToken();
            if (StringUtils.isEmpty(openId)) {
                throw new ParamsIllegalException(ErrorConstant.AUTH_ERR.getErrorMsg());
            }
            //【二】根据openId、accessToken获取用户信息
            UserInfoDTO userInfo = wechatRedisService.getUserInfo(accessToken, openId);
            String nickName = userInfo.getNickname();
            String headImgurl = userInfo.getHeadimgurl();
            log.info("userInfo:[{}]", JacksonUtil.objectToJson(userInfo));
            if (null == userInfo || StringUtils.isEmpty(userInfo.getNickname())) {
                log.info("根据openId、accessToken获取用户信息失败");
            } else {
                try {
                    nickName = URLEncoder.encode(userInfo.getNickname(), "UTF-8");
                } catch (Exception e) {
                    log.info("获取昵称出现异常！");
                }
            }

            //【三】用户登录工资条
            CacheUserPrincipal cacheUserPrincipal = wechatRedisService.registeWechatPayroll(jsessionId, openId, nickName, headImgurl, "", appPartner);
            if (StringUtils.isNotBlank(cacheUserPrincipal.getIdNumber())) {
                res100705.setBindStatus("1");
                res100705.setIdNumber(cacheUserPrincipal.getIdNumberEncrytor());
                res100705.setIfPwd(StringUtils.isEmpty(StringUtils.trimToEmpty(cacheUserPrincipal.getQueryPwd())) ? IsStatusEnum.NO.getCode() : IsStatusEnum.YES.getCode());
                res100705.setName(cacheUserPrincipal.getName());
                res100705.setPhone(cacheUserPrincipal.getPhone());
            }
            res100705.setHeadimgurl(headImgurl);
            log.info("activity.res100705:[{}]", JacksonUtil.objectToJson(res100705));
            return res100705;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 员工是否参与此活动
     * <p>
     * redis需要设置超时时间
     * <p>
     * 1、查询 redis 数据，根据（证件号码、活动id） 员工是否已经参与此活动
     * 2、redis 未查询到数据，则直接  调用服务化数据
     * =====>未查询到数据
     * -------->调用服务化数据   activityStart/involved
     * -------->判断是否已经参与了此活动, 根据查询对象 ActivityTransLogRes 是否为空
     * -------->如果查询对象不对空 则参与，需要存入redis 缓存，并设置过期时间
     * =====>查询到数据
     * -------->返回已参与此活动
     *
     * @param jsessionId 用户jsessionId
     * @param id         活动id
     * @param entId      企业id
     * @return
     */
    @GetMapping("/isRandom/{id}")
    @TrackLog
    @PermitAll
    public Mono<Boolean> isRandom(@RequestHeader("jsession-id") String jsessionId,
                                  @PathVariable(value = "id") String id,
                                  @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //初始化，未参与此活动
            Boolean isRandom = false;

            //[1] 检查 redis 值拼接
            String redisKey = PayrollConstants.ACTIVITYACCEDE_LOCK_ACTIVITYID_IDNUMBER.replaceAll("activityid", id);
            redisKey = redisKey.replaceAll("idnumber", idNumber);
            Object involved = activityStartService.getRedisKey(redisKey);

            //[2] 无数据，则 远程调用
            if (involved != null) {
                //缓存中有数据，则说明参加过活动
                return isRandom = true;
            }
            // 查询 活动交易记录表  构建数据
            ActivityTransLogReq activityTransLogReq = ActivityTransLogConvert.of(id, idNumber, entId);
            log.info("isRandom.activityTransLogReq:[{}]", JacksonUtil.objectToJson(activityTransLogReq));
            ActivityTransLogRes activityTransLogRes = activityStartFeignService.involved(activityTransLogReq);

            if (activityTransLogRes != null) {
                isRandom = true;
                //[通过异步] redis 存入数据
                activityStartService.setRedisKey(redisKey, JacksonUtil.objectToJson(activityTransLogRes), PayrollConstants.ACTIVITY_EXPIRESIN, TimeUnit.SECONDS);
            }

            return isRandom;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 【通用接口】查询用户企业信息（一个用户可能在多家企业）
     * <p>
     * 用户所在企业，可以参与最新活动
     * 用户可能在企业
     * <p>
     * 1、通过缓存 筛选 在职员工 ，只有 在职状成的员工，才参与活动
     * 2、筛选  过滤出 ent
     * 3、循环 ent 数组
     * ====> 构建查询对象   entId、groupIds、OPEN("待开启"), ING("进行中"),a.after(LocalDateTime.now()))
     * ====> activityStatus.desc(), qActivityInfo.startDateTime.asc()
     * ====> 判断  进行中  --> 随机红包 ->  是否参与过
     * ====> 如果 entId 查询出多项，则只能取  其中一项
     * 4、调用服务化 activityStart /joinActivity
     *
     * @param jsessionId
     * @return
     * @throws Exception
     */
    @GetMapping("/enterprises")
    @TrackLog
    @PermitAll
    public Mono<EntInfoActivityDTO> enterprises(@RequestHeader("jsession-id") String jsessionId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //查询企业员工 对应企业信息( 可以属于多个企业， 或是在一家企业 属于 多个机构)
        UserPrincipal principal = WebContext.getCurrentUser();
        log.info("enterprises.principal.getSessionId():[{}]", principal.getSessionId());
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            List<core.dto.wechat.EntInfoDTO> list = principal.getEntInfoDTOS();

            List<core.dto.wechat.EntInfoDTO> checkList = new LinkedList<>();

            //list中员工的数据（在职、离职、删除等状态），只有在职员工才能参与活动， 剔除删除员工
            //员工可能在多个企业，下面的多个机构，每个机构状态可能不一样，需要筛选出来在职
            for (int i = 0; i < list.size(); i++) {
                core.dto.wechat.EntInfoDTO entInfoDTO = list.get(i);
                LinkedList<core.dto.wechat.EntInfoDTO.GroupInfo> groupInfolist = entInfoDTO.getGroupInfoList();
                int k = 0;
                log.info("员工所有机构数据[{}]", JacksonUtil.objectToJson(groupInfolist));
                for (int j = 0; j < groupInfolist.size(); j++) {
                    core.dto.wechat.EntInfoDTO.GroupInfo groupInfo = groupInfolist.get(j);
                    log.info("EntInfoDTO.GroupInfo:[{}]", JacksonUtil.objectToJson(groupInfo));
                    if (!groupInfo.getEmpGroupInservice()) {
                        k = k + 1;
                    }
                }

                if (k == groupInfolist.size()) {
                    log.error("====>员工所在机构下，员工状态都不为【在职】状态");
                } else {
                    checkList.add(entInfoDTO);
                }
            }

            EntInfoActivityDTO entInfoActivityDTO = new EntInfoActivityDTO();

            //并发数据转换（根据 活动类型，查询属性数据）
            Iterator iterator = Flux.fromIterable(checkList)
                    .flatMapSequential(entinfodto -> Mono.fromCallable(() ->
                            {
                                mdcContext.put(PayrollConstants.SUB_TOKEN, UUIDUtil.createUUID8());
                                MDC.setContextMap(mdcContext);

                                //员工分布企业所在机构，员工所属多机构
                                LinkedList<core.dto.wechat.EntInfoDTO.GroupInfo> groupInfoList = entinfodto.getGroupInfoList();
                                int len = groupInfoList.size();
                                String[] groupIds = new String[len];
                                for (int i = 0; i < len; i++) {
                                    log.info("====>i={},{}", i, groupInfoList.get(i).getGroupId());
                                    groupIds[i] = groupInfoList.get(i).getGroupId();
                                }

                                EntInfoActivityDTO.EntInfo entInfo = new EntInfoActivityDTO.EntInfo();
                                entInfo.setEntId(entinfodto.getEntId());
                                entInfo.setEntName(entinfodto.getEntName());
                                entInfo.setActivityId("");
                                entInfo.setActivityType(-1); //-1表示 不存在
                                entInfo.setActivityTypeDesc("");
                                //如果机构为空，则不在远程调用
                                if (groupIds.length > 0) {
                                    //构建 活动查询对象
                                    ActivityQueryRequest request = ActivityQueryRequest.builder()
                                            .entId(entinfodto.getEntId())
                                            .activityStatusList(new LinkedList(Arrays.asList(ActivityStatusEnum.OPEN, ActivityStatusEnum.ING)))
                                            .groupIds(groupIds)
                                            .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                                            .build();
                                    ActivityInfoRes activityInfoRes = activityStartFeignService.joinActivity(request);
                                    if (activityInfoRes != null) {
                                        entInfo.setActivityId(activityInfoRes.getActivityId());
                                        entInfo.setActivityType(activityInfoRes.getActivityType().getCode());
                                        entInfo.setActivityTypeDesc(activityInfoRes.getActivityType().getDesc());
                                    }
                                }
                                return entInfo;
                            }
                    ).subscribeOn(Schedulers.boundedElastic()), 5)
                    .toIterable()
                    .iterator();

            List<EntInfoActivityDTO.EntInfo> convert = IteratorUtils.toList(iterator);
            entInfoActivityDTO.setEntInfoList(convert);
            entInfoActivityDTO.setBindStatus("1");
            return entInfoActivityDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 企业活动（统计）信息
     * <p>
     * 1、根据 证件号码、企业id 、活动类型
     * 2、调用服务化 activityStart /perAnalyse
     *
     * @param jsessionId
     * @param entId
     * @return
     * @throws Exception
     */
    @GetMapping("/entactivStat")
    @TrackLog
    @PermitAll
    public Mono<List<EntActivityInfoStatisticsDTO>> entactivStat(@RequestHeader("jsession-id") String jsessionId,
                                                                 @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //查询企业员工 对应企业信息( 可以属于多个企业， 或是在一家企业 属于 多个机构)
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String entName = "";
            List<core.dto.wechat.EntInfoDTO> list = principal.getEntInfoDTOS();
            //取 企业 名称
            List<core.dto.wechat.EntInfoDTO> entInfoDTOList = principal.getEntInfoDTOS();
            for (core.dto.wechat.EntInfoDTO entInfoDTO : entInfoDTOList) {
                if (entId.equalsIgnoreCase(entInfoDTO.getEntId())) {
                    entName = entInfoDTO.getEntName();
                    break;
                }
            }

            ActivityPrizeAnalyseReq request = ActivityPrizeAnalyseReq
                    .builder()
                    .entId(entId)
                    .activityTypeList(new LinkedList(Arrays.asList(ActivityTypeEnum.ANSWER, ActivityTypeEnum.RANDOM, ActivityTypeEnum.RAIN)))
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .idNumber(idNumber)
                    .build();
            List<ActivityPrizeAnalyseRes> resList = activityStartFeignService.perAnalyse(request);


            List<EntActivityInfoStatisticsDTO> analyses = new LinkedList<EntActivityInfoStatisticsDTO>();
            for (ActivityPrizeAnalyseRes activityPrizeAnalyseRes : resList) {
                EntActivityInfoStatisticsDTO dto = EntActivityInfoStatisticsDTO.builder()
                        .entId(activityPrizeAnalyseRes.getEntId())
                        .entName(entName)
                        .activityTypeCode(activityPrizeAnalyseRes.getActivityType().getCode())
                        .activityTypeDesc(activityPrizeAnalyseRes.getActivityType().getDesc())
                        .realAmt(activityPrizeAnalyseRes.getRealAmt())
                        .build();
                analyses.add(dto);
            }

            return analyses;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 企业活动（基本）信息列表
     * <p>
     * 1、枚举 活动类型 、 活动状态
     * 2、构建查询对象 证件号码、企业id、活动类型、活动状态
     * 3、分页查询
     * ====>crtDateTime.desc()
     * 4、查询结果，循环
     * ====>企业id entId     （请求值）
     * ====>企业名称 entName （请求值）
     * ====>活动ID activityId
     * ====>活动名称 activityName
     * ====>活动类型
     * ====>活动状态
     * ====>预算金额 budgetAmt
     * ====>活动实发金额 activityRealAmt  *
     * ====>实发金额 prizeRealAmt
     * ------> 默认为0
     * ====>资金状态 prizeDescribe
     * ------> 未入账
     * ------> 已入账 activityBalance = BONU("奖金清算完成"),
     * ====>服务器时间 startDateTime
     * ====>活动结束时间 endDateTime
     * ====>开奖时间 lotteryDateTime
     * <p>
     * 5、 按用户查询汇总 PRIZE("中奖")  DELIVERY("清算")
     * ------>查询的金额
     *
     * @param jsessionId
     * @param entId
     * @return
     * @throws Exception
     */
    @GetMapping("/entActivityInfoList")
    @TrackLog
    @PermitAll
    public Mono<PageResponseDTO<EntActivityInfoDTO>> entActivityInfoList(@RequestHeader("jsession-id") String jsessionId,
                                                                         @RequestHeader("page-num") @DefaultValue("1") int pageNum,
                                                                         @RequestHeader("limit") @DefaultValue("10") int size,
                                                                         @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //查询企业员工 对应企业信息( 可以属于多个企业， 或是在一家企业 属于 多个机构)
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            String entName = "";
            List<core.dto.wechat.EntInfoDTO> list = principal.getEntInfoDTOS();
            //取 企业 名称
            List<core.dto.wechat.EntInfoDTO> entInfoDTOList = principal.getEntInfoDTOS();
            for (core.dto.wechat.EntInfoDTO entInfoDTO : entInfoDTOList) {
                if (entId.equalsIgnoreCase(entInfoDTO.getEntId())) {
                    entName = entInfoDTO.getEntName();
                    break;
                }
            }

            ActivityQueryRequest request = ActivityQueryRequest.builder()
                    .entId(entId)
                    .activityTypeList(new LinkedList(Arrays.asList(ActivityTypeEnum.ANSWER, ActivityTypeEnum.RANDOM, ActivityTypeEnum.RAIN)))
                    .activityStatusList(new LinkedList(Arrays.asList(ActivityStatusEnum.ING, ActivityStatusEnum.OPEN, ActivityStatusEnum.AWARD, ActivityStatusEnum.END)))
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .idNumber(idNumber)
                    .build();

            PageDTO<ActivityInfoRes> pageDTO = activityStartFeignService.activityPage(request);

            List<EntActivityInfoDTO> dtolist = new LinkedList<>();
            for (ActivityInfoRes activityInfoRes : pageDTO.getContent()) {
                EntActivityInfoDTO entActivityInfoDTO = EntActivityInfoDTO.builder()
                        .entId(activityInfoRes.getEntId())
                        .entName(entName)
                        .activityId(activityInfoRes.getActivityId())
                        .activityName(activityInfoRes.getActivityName())
                        .activityTypeCode(activityInfoRes.getActivityType().getCode())
                        .activityTypeDesc(activityInfoRes.getActivityType().getDesc())
                        .activityStatusCode(activityInfoRes.getActivityStatus().getCode())
                        .activityStatusDesc(activityInfoRes.getActivityStatus().getDesc())
                        .budgetAmt(activityInfoRes.getBudgetAmt())
                        //.activityRealAmt(activityInfoRes.get())
                        .prizeRealAmt(activityInfoRes.getPrizeRealAmt())
                        .prizeDescribe(activityInfoRes.getPrizeDescribe())
                        .startDateTime(activityInfoRes.getStartDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .endDateTime(activityInfoRes.getEndDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .build();
                //todo lotteryDateTime 未获取到值
                LocalDateTime lotteryDateTime = activityInfoRes.getLotteryDateTime();
                if (null != lotteryDateTime) {
                    long lotteryLongTime = lotteryDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    entActivityInfoDTO.setLotteryDateTime(lotteryLongTime);
                }
                dtolist.add(entActivityInfoDTO);
            }

            PageResponseDTO<EntActivityInfoDTO> responseDTO = new PageResponseDTO<EntActivityInfoDTO>();
            responseDTO.setPage(pageDTO.getPageNum());
            responseDTO.setSize(pageDTO.getPageSize());
            responseDTO.setTotalElements(pageDTO.getTotalElements());
            responseDTO.setTotalPages(pageDTO.getTotalPages());
            responseDTO.setResponeList(dtolist);

            return responseDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }

/////////////////////////////红包雨////////红包雨////////////////////红包雨///////////////////////////////////

    /**
     * 1、活动 查询条件校验 活动ID(activityId)  活动类型(activityType)  所属企业(entId)
     * 2、查询是否有开始的活动，如果存在，则取缓存中数据，不存在 则调用服务化
     * 3、调用服务化 activityStart/activity
     * 4、根据活动信息进行判断
     * 5、活动类型 是否相同
     * 6、此接口 isRain = false
     * ====> 红包雨 在活动结束后，才提交点击次数， 如果中途退出，则可以在次参加，时间会变短
     * ====> 活动结束后，无法参与此活动
     */
    @PostMapping("/accedeRain")
    @TrackLog
    @PermitAll
    public Mono<ActivityRainResponseDTO> isRandom(@RequestBody ActivityInfoRequestDTO activityInfoRequestDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();


        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            // 活动信息 与 登录人员信息 检查
            activityStartService.checkActivityInfoPrincipal(activityInfoRequestDTO, principal);

            ActivityQueryRequest request = ActivityQueryRequest.builder()
                    .activityId(activityInfoRequestDTO.getActivityId())
                    .entId(activityInfoRequestDTO.getEntId())
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .activityStatusList(new LinkedList(Arrays.asList(ActivityStatusEnum.OPEN, ActivityStatusEnum.ING, ActivityStatusEnum.AWARD, ActivityStatusEnum.END)))
                    .activityTypeList(new LinkedList(Arrays.asList(ActivityTypeEnum.RAIN)))
                    .build();
            ActivityInfoRes rain = activityStartFeignService.activity(request);
            ActivityRainResponseDTO activityRainResponseDTO = new ActivityRainResponseDTO(rain);

            ActivityRainRes activityInfoRain = rain.getActivityRain();
            if (activityInfoRain != null) {
                activityRainResponseDTO.setIsRain(false);
                activityRainResponseDTO.setRate(activityInfoRain.getRate());
                activityRainResponseDTO.setMinAmt(activityInfoRain.getMinAmt());
                activityRainResponseDTO.setMaxAmt(activityInfoRain.getMaxAmt());
            }
            // 活动信息 与 登录人员信息 检查
            activityStartService.checkAccedeActivityInfo(activityInfoRequestDTO, activityRainResponseDTO);

            return activityRainResponseDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 调用服务化  activityAccede/accedeRain
     *
     * @param jsessionId
     * @param id
     * @param tol
     * @param entId
     * @return
     * @throws Exception
     */
    @GetMapping("/accedeRain/{id}")
    @TrackLog
    @PermitAll
    public Mono<Void> isRandom(@RequestHeader("jsession-id") String jsessionId,
                               @PathVariable(value = "id") String id,
                               @RequestParam("tol") Integer tol,
                               @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            AccedeReq request = AccedeReq.builder()
                    .activityId(id)
                    .activityType(ActivityTypeEnum.RAIN)
                    .entId(entId)
                    .idNumber(idNumber)
                    .accedeNumber(tol > 300 ? 300 : tol)
                    .employeeName(principal.getName())
                    .openId(principal.getOpenId())
                    .nickname(principal.getNickname())
                    .headimgurl(principal.getHeadimgurl())
                    .build();
            //红包雨提交数据入MQ
            String messageId = UUIDUtil.createUUID32();
            AccedeRainReq accedeRainReq = AccedeRainReq.builder()
                    .messageId(messageId)
                    .accedeReq(request)
                    .build();
            activityStartService.accedeRain(accedeRainReq);

            //原流程直接feign调用方式注释
//            activityAccedeFeignService.accedeRain(request);

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }


    /**
     * 1、type  =  0 触发开奖
     * 2、type = 1  查询中奖汇总，和当前用户中奖信息
     *
     * @param jsessionId
     * @param id
     * @param type       类型（0 开奖， 1 红包查询）
     * @param entId
     * @return
     */
    @GetMapping("/rainAccede/per/{type}/{id}")
    @TrackLog
    @PermitAll
    public Mono<ActivityRainPerResultDTO> rainAccedeResultPer(@RequestHeader("jsession-id") String jsessionId,
                                                              @PathVariable(value = "id") String id,
                                                              @PathVariable("type") Integer type,
                                                              @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String[] msgs = new String[]{
                "你以为点了就有钱吗？",
                "再点一次试试看！",
                "慢点，屏幕要碎了！",
                "我的小心肝有点紧张！",
                "妈咪妈咪哄，开出心想大包！",
                "郭总说，机器挂了可以省点红包！",
                "郭总说，红包拿的多，涨薪就别想了！",
                "今年加班有点多，领个红包好过年！",
                "大雨浇跨了服务器!",
                "我想看璐璐姐跳个舞！",
                "人数有点多，CPU算不过来了！",
                "宋总说，答题要向高考看齐！",
                "宋总，能不能来个段子！",
                "偷偷告诉你，最多三分钟你就能知道结果！",
                "雨下的有点小，再来一次好不好！"
        };

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //根据id查询活动,活动未清算完成，不给与查询中奖金额！
            ActivityQueryRequest activityQueryRequest = ActivityQueryRequest.builder()
                    .activityId(id)
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();
            ActivityInfoRes activityInfoRes = activityConfigureService.findActivityById(id, activityQueryRequest);
            if (null == activityInfoRes || !activityInfoRes.getActivityStatus().equals(ActivityStatusEnum.END)) {
                Random rand = new Random();
                Integer index = rand.nextInt(msgs.length);
                log.debug("活动正在清算中！");
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format(msgs[index]));
            }
            ActivityRainPerResultDTO dto = ActivityRainPerResultDTO.builder()
                    .prizeTime(60000L)
                    .employeeName(principal.getName())
                    .openId(principal.getOpenId())
                    .nickname(principal.getNickname())
                    .headimgurl(principal.getHeadimgurl())
                    .isRain(false)
                    .cardNo("")
                    .issuerName("")
                    .prizeDateTime(0)
                    .totleAmt(BigDecimal.ZERO)
                    .prizeTotlePer(0)
                    .rank(0)
                    .build();

            ActivityPrizeReq allPrize = ActivityPrizeReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .activityType(ActivityTypeEnum.RAIN)
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .build();
            List<ActivityPrizeRes> allprize = activityStartFeignService.prizeAll(allPrize);

            for (ActivityPrizeRes activityPrizeRes : allprize) {
                dto.setTotleAmt(dto.getTotleAmt().add(activityPrizeRes.getPrizeAmt()));
                dto.setPrizeTotlePer(dto.getPrizeTotlePer() + 1);
            }


            ActivityPrizeReq per = ActivityPrizeReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .idNumber(idNumber)
                    .activityType(ActivityTypeEnum.RAIN)
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .build();
            ActivityPrizeRes perPrize = activityStartFeignService.prize(per);


            if (perPrize != null) {
                dto.setEmployeeName(perPrize.getEmployeeName());
                dto.setOpenId(perPrize.getOpenId());
                dto.setNickname(perPrize.getNickname());
                dto.setHeadimgurl(perPrize.getHeadimgurl());
                dto.setIsRain(true);
                dto.setRank(perPrize.getRank());
                dto.setPrizeAmt(perPrize.getPrizeAmt());
                dto.setPrizeDateTime(perPrize.getPrizeDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }

            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 分面查询中奖信息
     *
     * @param jsessionId
     * @param id
     * @param entId
     * @return
     * @throws Exception
     */
    @GetMapping("/rainAccede/{id}")
    @TrackLog
    @PermitAll
    public Mono<PageResponseDTO<AccedeRandomResultDTO>> rainAccedeResult(@RequestHeader("jsession-id") String jsessionId,
                                                                         @PathVariable(value = "id") String id,
                                                                         @RequestHeader("page-num") @DefaultValue("1") int pageNum,
                                                                         @RequestHeader("limit") @DefaultValue("10") int size,
                                                                         @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);


            ActivityPrizeReq allPrize = ActivityPrizeReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .activityType(ActivityTypeEnum.RAIN)
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .build();

            PageDTO<ActivityPrizeRes> pageDTO = activityStartFeignService.prizePage(allPrize);
            //排序
            List<ActivityPrizeRes> activityPrizeResList = pageDTO.getContent();
            Collections.sort(activityPrizeResList, new Comparator<ActivityPrizeRes>() {
                @Override
                public int compare(ActivityPrizeRes a1, ActivityPrizeRes a2) {
                    return a1.getRank().compareTo(a2.getRank());
                }
            });

            List<AccedeRandomResultDTO> dtolist = new LinkedList<>();
            for (ActivityPrizeRes activityPrizeRes : activityPrizeResList) {
                AccedeRandomResultDTO accedeRandomResultDTO = AccedeRandomResultDTO.builder()
                        .employeeName(activityPrizeRes.getEmployeeName())
                        .openId(activityPrizeRes.getOpenId())
                        .nickname(activityPrizeRes.getNickname())
                        .headimgurl(activityPrizeRes.getHeadimgurl())
                        .prizeAmt(activityPrizeRes.getPrizeAmt())
                        .prizeDateTime(activityPrizeRes.getPrizeDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .prizeName(activityPrizeRes.getPrizeName())
                        .build();

                dtolist.add(accedeRandomResultDTO);
            }

            PageResponseDTO<AccedeRandomResultDTO> responseDTO = new PageResponseDTO<AccedeRandomResultDTO>();
            responseDTO.setPage(pageDTO.getPageNum());
            responseDTO.setSize(pageDTO.getPageSize());
            responseDTO.setTotalElements(pageDTO.getTotalElements());
            responseDTO.setTotalPages(pageDTO.getTotalPages());
            responseDTO.setResponeList(dtolist);


            return responseDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }

////////////答题/////////////////答题//////答题/////////////////////////////////////////////////////////


    /**
     * 查询（答题）详情
     * <p>
     * 1、 活动 查询条件校验   活动ID(activityId)  活动类型(activityType)  所属企业(entId)
     * 1、查询是否有开始的活动，如果存在，则取缓存中数据，不存在则查数据库
     * 2、查询数据库
     * 3、根据活动信息进行判断
     * 活动类型 是否相同
     */
    @PostMapping("/accedeAnswer")
    @TrackLog
    @PermitAll
    public Mono<ActivityAnswerResponseDTO> accedeAnswerInfo(@RequestBody ActivityAnswerResponseDTO activityAnswerResponseDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //TODO 待完成
            //activityInfoService.checkActivityInfoPrincipal(activityInfoRequestDTO, principal);

            //查询 答题 信息
            ActivityQueryRequest activityQuery = ActivityQueryRequest.builder()
                    .activityId(activityAnswerResponseDTO.getActivityId())
                    .activityTypeList(new LinkedList(Arrays.asList(ActivityTypeEnum.ANSWER)))
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();
            ActivityInfoRes activityInfoRes = activityStartFeignService.activity(activityQuery);
            List<ActivityInfoAnswerRes> answerResList = activityInfoRes.getActivityInfoAnswer();

            ActivityAnswerResponseDTO dto = new ActivityAnswerResponseDTO(activityInfoRes);
            for (ActivityInfoAnswerRes activityInfoAnswerRes : answerResList) {
                ActivityAnswerResponseDTO.ActivityAnswerDetail detail = new ActivityAnswerResponseDTO.ActivityAnswerDetail(activityInfoAnswerRes);
                dto.getAnswerDetails().add(detail);
            }

            //TODO 待完成 参数校验
            //activityInfoService.checkAccedeActivityInfo(activityAnswerResponseDTO, activityInfoRequestDTO);

            ActivityTransLogReq involvedlog = ActivityTransLogReq.builder()
                    .activityId(activityAnswerResponseDTO.getActivityId())
                    .entId(activityAnswerResponseDTO.getEntId())
                    .idNumber(idNumber)
                    .activityType(ActivityTypeEnum.ANSWER)
                    .build();
            ActivityTransLogRes activityTransLogRes = activityStartFeignService.involved(involvedlog);
            dto.setIsRandom(false);
            if (activityTransLogRes != null) {
                dto.setIsRandom(true);
            }

            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 参加（答题）详情
     *
     * @param jsessionId
     * @param id
     * @param answer
     * @param entId
     * @return
     * @throws Exception
     */
    @GetMapping("/answerAccede/{id}")
    @TrackLog
    @PermitAll
    public Mono<ActivityAnswerTimelyPrizeDTO> accedeActivityAnswer(@RequestHeader("jsession-id") String jsessionId,
                                                                   @PathVariable(value = "id") String id,
                                                                   @RequestParam("answer") String answer,
                                                                   @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            ActivityAnswerTimelyPrizeDTO dto = new ActivityAnswerTimelyPrizeDTO();
            dto.setActivityId(id);
            dto.setActivityType(ActivityTypeEnum.ANSWER.getCode());
            dto.setEntId(entId);
            //dto.setEmployeeId();
            dto.setEmployeeName(principal.getName());
            dto.setOpenId(principal.getOpenId());
            dto.setNickname(principal.getNickname());
            dto.setHeadimgurl(principal.getHeadimgurl());
            dto.setIsAnswer(false);
            dto.setPrizeAmt(BigDecimal.ZERO);


            //判断是否已经参与此活动
            ActivityTransLogReq involvedlog = ActivityTransLogReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .idNumber(idNumber)
                    .activityType(ActivityTypeEnum.ANSWER)
                    .build();
            ActivityTransLogRes activityTransLogRes = activityStartFeignService.involved(involvedlog);
            if (activityTransLogRes != null) {
                //抛出异常
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("已参与此项活动!"));
            }
            AccedeReq request = AccedeReq.builder()
                    .activityId(id)
                    .activityType(ActivityTypeEnum.ANSWER)
                    .entId(entId)
                    .idNumber(idNumber)
                    .answer(answer)
                    .employeeName(principal.getName())
                    .openId(principal.getOpenId())
                    .nickname(principal.getNickname())
                    .headimgurl(principal.getHeadimgurl())
                    .build();

            //改用MQ start 查询正确答案
            ActivityQueryRequest activityQueryRequest = ActivityQueryRequest.builder()
                    .activityId(id)
                    .build();
            String correctAnswer = activityStartService.findCorrectAnswer(id, activityQueryRequest);
            // 判断是否中奖
            chain.utils.fxgj.constant.DictEnums.IsStatusEnum isStatus = chain.utils.fxgj.constant.DictEnums.IsStatusEnum.NO;
            if (StringUtils.equalsAnyIgnoreCase(StringUtils.trimToEmpty(answer), correctAnswer)) {
                isStatus = chain.utils.fxgj.constant.DictEnums.IsStatusEnum.YES;
            }
            String messageId = UUIDUtil.createUUID32();
            AccedeAnswerReq accedeAnswerReq = AccedeAnswerReq.builder()
                    .messageId(messageId)
                    .isStatus(isStatus)
                    .accedeReq(request)
                    .build();
            //答题提交异步放入队列
            activityStartService.accedeAnswer(accedeAnswerReq);

            dto.setActivityId(request.getActivityId());
            dto.setActivityType(request.getActivityType().getCode());
            dto.setEntId(request.getEntId());
            dto.setEmployeeId(request.getEmployeeId());
            dto.setEmployeeName(request.getEmployeeName());
            dto.setIdNumber(request.getIdNumber());
            dto.setOpenId(request.getOpenId());
            dto.setNickname(request.getNickname());
            dto.setHeadimgurl(request.getHeadimgurl());
            dto.setIsAnswer(isStatus.getCode().equals(1) ? true : false);
            dto.setPrizeAmt(BigDecimal.ZERO);
            //改用MQ end

            //未使用MQ的原流程，暂注释
//            ActivityPrizeRes activityPrizeRes = activityAccedeFeignService.accedeAnswer(request);
//            if (activityPrizeRes != null) {
//                dto.setActivityId(activityPrizeRes.getActivityId());
//                dto.setActivityType(activityPrizeRes.getActivityType().getCode());
//                dto.setEntId(activityPrizeRes.getEntId());
//                dto.setEmployeeId(activityPrizeRes.getEmployeeId());
//                dto.setEmployeeName(activityPrizeRes.getEmployeeName());
//                dto.setIdNumber(activityPrizeRes.getIdNumber());
//                dto.setOpenId(activityPrizeRes.getOpenId());
//                dto.setNickname(activityPrizeRes.getNickname());
//                dto.setHeadimgurl(activityPrizeRes.getHeadimgurl());
//                dto.setIsAnswer(true);
//                dto.setPrizeAmt(activityPrizeRes.getPrizeAmt());
//            }

            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 个人（答题）活动(结果查询
     * 1、type  =  0 触发开奖
     * 2、type = 1  查询中奖汇总，和当前用户中奖信息
     *
     * @param jsessionId
     * @param id
     * @param entId
     * @return
     */
    @GetMapping("/answerAccede/per/{id}")
    @TrackLog
    @PermitAll
    public Mono<ActivityAnswerPerResultDTO> answerAccedeResultPer(@RequestHeader("jsession-id") String jsessionId,
                                                                  @PathVariable(value = "id") String id,
                                                                  @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            ActivityAnswerPerResultDTO dto = new ActivityAnswerPerResultDTO();
            dto.setEmployeeName(principal.getName());
            dto.setOpenId(principal.getOpenId());
            dto.setNickname(principal.getNickname());
            dto.setHeadimgurl(principal.getHeadimgurl());
            dto.setIsAnswe(false);
            dto.setPrizeAmt(BigDecimal.ZERO);
            dto.setPrizeDateTime(0);
            dto.setTotleAmt(BigDecimal.ZERO);
            dto.setPrizeTotlePer(0);
            dto.setPrizeAmtPer(BigDecimal.ZERO);


            //全部中奖数据
            ActivityPrizeReq allPrize = ActivityPrizeReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .activityType(ActivityTypeEnum.ANSWER)
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .build();
            List<ActivityPrizeRes> allprize = activityStartFeignService.prizeAll(allPrize);
            Integer i = 0;
            BigDecimal totleAmt = new BigDecimal(0);
            BigDecimal totleAmtSum = new BigDecimal(0);

            for (ActivityPrizeRes activityPrizeRes : allprize) {
                if (ActivityPrizeStatusEnum.PRIZE.equals(activityPrizeRes.getPrizeStatus()) || ActivityPrizeStatusEnum.DELIVERY.equals(activityPrizeRes.getPrizeStatus())) {
                    i = i + 1;
                    totleAmt = totleAmt.add(activityPrizeRes.getPrizeAmt());
                }
                totleAmtSum = totleAmtSum.add(activityPrizeRes.getPrizeAmt());
            }

            dto.setTotleAmt(totleAmt);
            //如果 答题一个人都没有答对，则取产品预算总金额
            if (totleAmt.compareTo(BigDecimal.ZERO) == 0) {
                dto.setTotleAmt(totleAmtSum);
            }
            if (totleAmtSum.compareTo(BigDecimal.ZERO) == 0) {
                ActivityQueryRequest activityQuery = ActivityQueryRequest.builder()
                        .activityId(id)
                        .activityTypeList(new LinkedList(Arrays.asList(ActivityTypeEnum.ANSWER)))
                        .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                        .build();
                ActivityInfoRes activityInfoRes = activityStartFeignService.activity(activityQuery);
                dto.setTotleAmt(activityInfoRes.getBudgetAmt());
            }
            dto.setPrizeTotlePer(allprize.size());


            ActivityPrizeReq per = ActivityPrizeReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .idNumber(idNumber)
                    .activityType(ActivityTypeEnum.ANSWER)
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .build();
            ActivityPrizeRes perPrize = activityStartFeignService.prize(per);

            //取中奖信息
            if (perPrize != null) {
                dto.setPrizeDateTime(perPrize.getPrizeDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                dto.setPrizeAmt(perPrize.getPrizeAmt());
                dto.setIsAnswe(true);
            }

            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 分面查询中奖信息
     *
     * @param jsessionId
     * @param id
     * @param entId
     * @param desc       结果查询（0 正序 1 倒序）
     * @return
     * @throws Exception
     */
    @GetMapping("/answerAccede/{id}/{desc}")
    @TrackLog
    @PermitAll
    public Mono<PageResponseDTO<AccedeRandomResultDTO>> rainAccedeResult(@RequestHeader("jsession-id") String jsessionId,
                                                                         @PathVariable(value = "id") String id,
                                                                         @PathVariable(value = "desc", required = false) int desc,
                                                                         @RequestHeader("page-num") @DefaultValue("1") int pageNum,
                                                                         @RequestHeader("limit") @DefaultValue("10") int size,
                                                                         @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            ActivityPrizeReq allPrize = ActivityPrizeReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .activityType(ActivityTypeEnum.ANSWER)
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .build();

            PageDTO<ActivityPrizeRes> pageDTO = activityStartFeignService.prizePage(allPrize);


            List<AccedeRandomResultDTO> dtolist = new LinkedList<>();
            for (ActivityPrizeRes activityPrizeRes : pageDTO.getContent()) {
                AccedeRandomResultDTO accedeRandomResultDTO = AccedeRandomResultDTO.builder()
                        .employeeName(activityPrizeRes.getEmployeeName())
                        .openId(activityPrizeRes.getOpenId())
                        .nickname(activityPrizeRes.getNickname())
                        .headimgurl(activityPrizeRes.getHeadimgurl())
                        .prizeAmt(activityPrizeRes.getPrizeAmt())
                        .prizeDateTime(activityPrizeRes.getPrizeDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .prizeName(activityPrizeRes.getPrizeName())
                        .build();

                dtolist.add(accedeRandomResultDTO);
            }

            PageResponseDTO<AccedeRandomResultDTO> responseDTO = new PageResponseDTO<AccedeRandomResultDTO>();
            responseDTO.setPage(pageDTO.getPageNum());
            responseDTO.setSize(pageDTO.getPageSize());
            responseDTO.setTotalElements(pageDTO.getTotalElements());
            responseDTO.setTotalPages(pageDTO.getTotalPages());
            responseDTO.setResponeList(dtolist);


            return responseDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /////////////////////////////手气红包////////手气红包////////////////////手气红包///////////////////////////////////


    /**
     * 查询（手气红包）详情
     * <p>
     * 1、 活动 查询条件校验   活动ID(activityId)  活动类型(activityType)  所属企业(entId)
     * 1、查询是否有开始的活动，如果存在，则取缓存中数据，不存在则查数据库
     * 2、查询数据库
     * 3、根据活动信息进行判断
     * 活动类型 是否相同
     */
    @PostMapping("/accedeRandom")
    @TrackLog
    @PermitAll
    public Mono<ActivityRandomResponseDTO> accedeRandomInfo(@RequestBody ActivityInfoRequestDTO activityInfoRequestDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //TODO 待完成
            //activityInfoService.checkActivityInfoPrincipal(activityInfoRequestDTO, principal);

            //查询 答题 信息
            ActivityQueryRequest activityQuery = ActivityQueryRequest.builder()
                    .activityId(activityInfoRequestDTO.getActivityId())
                    .activityTypeList(new LinkedList(Arrays.asList(ActivityTypeEnum.RANDOM)))
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();
            ActivityInfoRes activityInfoRes = activityStartFeignService.activity(activityQuery);
            List<ActivityInfoRandomRes> randomList = activityInfoRes.getActivityInfoRandoms();

            ActivityRandomResponseDTO dto = new ActivityRandomResponseDTO(activityInfoRes);
            for (ActivityInfoRandomRes activityInfoRandomRes : randomList) {
                ActivityRandomResponseDTO.ActivityRandomDetail detail = new ActivityRandomResponseDTO.ActivityRandomDetail(activityInfoRandomRes);
                dto.getRandomDetails().add(detail);
            }

            //TODO 待完成 参数校验
            //activityInfoService.checkAccedeActivityInfo(activityAnswerResponseDTO, activityInfoRequestDTO);

            ActivityTransLogReq involvedlog = ActivityTransLogReq.builder()
                    .activityId(activityInfoRequestDTO.getActivityId())
                    .entId(activityInfoRequestDTO.getEntId())
                    .idNumber(idNumber)
                    .activityType(ActivityTypeEnum.RANDOM)
                    .build();
            ActivityTransLogRes activityTransLogRes = activityStartFeignService.involved(involvedlog);
            dto.setIsRandom(false);
            if (activityTransLogRes != null) {
                dto.setIsRandom(true);
            }

            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 参加（答题）详情
     *
     * @param jsessionId
     * @param id
     * @param entId
     * @return
     * @throws Exception
     */
    @GetMapping("/accedeRandom/{id}")
    @TrackLog
    @PermitAll
    public Mono<ActivityRandomTimelyPrizeDTO> accedeActivityRandom(@RequestHeader("jsession-id") String jsessionId,
                                                                   @PathVariable(value = "id") String id,
                                                                   @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            ActivityRandomTimelyPrizeDTO dto = new ActivityRandomTimelyPrizeDTO();
            dto.setActivityId(id);
            dto.setActivityType(ActivityTypeEnum.RANDOM.getCode());
            dto.setEntId(entId);
            //dto.setEmployeeId();
            dto.setEmployeeName(principal.getName());
            dto.setOpenId(principal.getOpenId());
            dto.setNickname(principal.getNickname());
            dto.setHeadimgurl(principal.getHeadimgurl());
            dto.setIsRandom(false);
            dto.setPrizeAmt(BigDecimal.ZERO);


            //判断是否已经参与此活动
            ActivityTransLogReq involvedlog = ActivityTransLogReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .idNumber(idNumber)
                    .activityType(ActivityTypeEnum.RANDOM)
                    .build();
            ActivityTransLogRes activityTransLogRes = activityStartFeignService.involved(involvedlog);
            if (activityTransLogRes != null) {
                //抛出异常
                dto.setIsRandom(true);
            }
            AccedeReq request = AccedeReq.builder()
                    .activityId(id)
                    .activityType(ActivityTypeEnum.RANDOM)
                    .entId(entId)
                    .idNumber(idNumber)
                    .employeeName(principal.getName())
                    .openId(principal.getOpenId())
                    .nickname(principal.getNickname())
                    .headimgurl(principal.getHeadimgurl())
                    .build();
            ActivityPrizeRes activityPrizeRes = activityAccedeFeignService.accedeRandom(request);

            if (activityPrizeRes != null) {
                dto.setActivityId(activityPrizeRes.getActivityId());
                dto.setActivityType(activityPrizeRes.getActivityType().getCode());
                dto.setEntId(activityPrizeRes.getEntId());
                dto.setEmployeeId(activityPrizeRes.getEmployeeId());
                dto.setEmployeeName(activityPrizeRes.getEmployeeName());
                dto.setIdNumber(activityPrizeRes.getIdNumber());
                dto.setOpenId(activityPrizeRes.getOpenId());
                dto.setNickname(activityPrizeRes.getNickname());
                dto.setHeadimgurl(activityPrizeRes.getHeadimgurl());
                dto.setIsRandom(true);
                dto.setPrizeAmt(activityPrizeRes.getPrizeAmt());
                dto.setPrizeName(activityPrizeRes.getPrizeName());
                //dto.setGrade();   //中奖等级（-1 未中奖 数字越大 等级越高
            }
            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 个人（手气红包）活动(结果查询)
     *
     * @param jsessionId
     * @param id
     * @param entId
     * @return
     */
    @GetMapping("/accedeRandomResult/per/{id}")
    @TrackLog
    @PermitAll
    public Mono<AccedeRandomPerResultDTO> accedeRandomResultPer(@RequestHeader("jsession-id") String jsessionId,
                                                                @PathVariable(value = "id") String id,
                                                                @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            AccedeRandomPerResultDTO dto = new AccedeRandomPerResultDTO();
            dto.setEmployeeName(principal.getName());
            dto.setOpenId(principal.getOpenId());
            dto.setNickname(principal.getNickname());
            dto.setHeadimgurl(principal.getHeadimgurl());
            dto.setIsRandom(false);
            dto.setPrizeAmt(BigDecimal.ZERO);
            dto.setPrizeDateTime(0);
            dto.setPrizeTotlePer(0);


            //全部中奖数据
            ActivityPrizeReq allPrize = ActivityPrizeReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .activityType(ActivityTypeEnum.RANDOM)
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .build();
            List<ActivityPrizeRes> totlePrize = activityStartFeignService.prizeAll(allPrize);
            Integer i = 0;

            for (ActivityPrizeRes activityPrizeRes : totlePrize) {
                if (ActivityPrizeStatusEnum.PRIZE.equals(activityPrizeRes.getPrizeStatus()) || ActivityPrizeStatusEnum.DELIVERY.equals(activityPrizeRes.getPrizeStatus())) {
                    i = i + 1;
                }
            }

            dto.setTotle(totlePrize.size());
            dto.setPrizeTotle(i);
            dto.setPrizeTotlePer(i);


            ActivityPrizeReq per = ActivityPrizeReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .idNumber(idNumber)
                    .activityType(ActivityTypeEnum.RANDOM)
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .build();
            ActivityPrizeRes perPrize = activityStartFeignService.prize(per);

            //取中奖信息
            if (perPrize != null) {
                dto.setPrizeDateTime(perPrize.getPrizeDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                dto.setPrizeAmt(perPrize.getPrizeAmt());
                dto.setIsRandom(true);
            }
            //设置
            ActivityQueryRequest activityQueryRequest = ActivityQueryRequest.builder()
                    .activityId(id)
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();
            ActivityInfoRes activityById = activityConfigureService.findActivityById(id, activityQueryRequest);
            LocalDateTime startDateTime = activityById.getStartDateTime();
            LocalDateTime endDateTime = activityById.getEndDateTime();
            Duration duration = Duration.between(startDateTime, endDateTime);
            long prizeTimeLong = duration.toMillis();
            dto.setPrizeTime(prizeTimeLong);
            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 分面查询中奖信息
     *
     * @param jsessionId
     * @param id
     * @param entId
     * @param desc       结果查询（0 正序 1 倒序）
     * @return
     * @throws Exception
     */
    @GetMapping("/accedeRandomResult/{id}/{desc}")
    @TrackLog
    @PermitAll
    public Mono<PageResponseDTO<AccedeRandomResultDTO>> accedeRandomResult(@RequestHeader("jsession-id") String jsessionId,
                                                                           @PathVariable(value = "id") String id,
                                                                           @PathVariable(value = "desc") int desc,
                                                                           @RequestHeader("page-num") @DefaultValue("1") int pageNum,
                                                                           @RequestHeader("limit") @DefaultValue("10") int size,
                                                                           @RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        //从缓存中读取用户信息
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            ActivityPrizeReq allPrize = ActivityPrizeReq.builder()
                    .activityId(id)
                    .entId(entId)
                    .activityType(ActivityTypeEnum.RANDOM)
                    .prizeStatusList(new LinkedList(Arrays.asList(ActivityPrizeStatusEnum.PRIZE, ActivityPrizeStatusEnum.DELIVERY)))
                    .build();

            PageDTO<ActivityPrizeRes> pageDTO = activityStartFeignService.prizePage(allPrize);


            List<AccedeRandomResultDTO> dtolist = new LinkedList<>();
            for (ActivityPrizeRes activityPrizeRes : pageDTO.getContent()) {
                AccedeRandomResultDTO accedeRandomResultDTO = AccedeRandomResultDTO.builder()
                        .employeeName(activityPrizeRes.getEmployeeName())
                        .openId(activityPrizeRes.getOpenId())
                        .nickname(activityPrizeRes.getNickname())
                        .headimgurl(activityPrizeRes.getHeadimgurl())
                        .prizeAmt(activityPrizeRes.getPrizeAmt())
                        .prizeDateTime(null == activityPrizeRes.getPrizeDateTime() ? 0L : activityPrizeRes.getPrizeDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .prizeName(activityPrizeRes.getPrizeName())
                        .build();

                dtolist.add(accedeRandomResultDTO);
            }

            PageResponseDTO<AccedeRandomResultDTO> responseDTO = new PageResponseDTO<AccedeRandomResultDTO>();
            responseDTO.setPage(pageDTO.getPageNum());
            responseDTO.setSize(pageDTO.getPageSize());
            responseDTO.setTotalElements(pageDTO.getTotalElements());
            responseDTO.setTotalPages(pageDTO.getTotalPages());
            responseDTO.setResponeList(dtolist);


            return responseDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }


}

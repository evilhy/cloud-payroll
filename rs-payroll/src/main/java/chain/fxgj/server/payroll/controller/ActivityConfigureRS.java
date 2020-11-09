package chain.fxgj.server.payroll.controller;

import chain.activity.dto.PageDTO;
import chain.activity.dto.request.activity.ActivityQueryRequest;
import chain.activity.dto.response.activity.*;
import chain.activity.feign.ActivityConfigureFeignService;
import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import chain.css.exception.ServiceHandleException;
import chain.css.log.annotation.TrackLog;
import chain.feign.hxinside.account.service.AccountFeignService;
import chain.feign.hxinside.ent.service.EntBusinessWhiteListServiceFeign;
import chain.feign.hxinside.ent.service.UserInfoServiceFeign;
import chain.fxgj.account.core.dto.request.account.AccountQueryRequest;
import chain.fxgj.account.core.dto.response.account.AccountInfoListDTO;
import chain.fxgj.account.core.dto.response.account.BalanceDTO;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.constant.PayrollDBConstant;
import chain.fxgj.ent.core.dto.request.CheckGroupListReq;
import chain.fxgj.ent.core.dto.request.EntBusinessWhiteListReq;
import chain.fxgj.ent.core.dto.request.UserQueryRequest;
import chain.fxgj.ent.core.dto.response.*;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.MsgCodeLogCheckRequestDTO;
import chain.fxgj.server.payroll.dto.MsgCodeLogResponeDTO;
import chain.fxgj.server.payroll.dto.PageResponseDTO;
import chain.fxgj.server.payroll.dto.activity.*;
import chain.fxgj.server.payroll.dto.base.ErrorDTO;
import chain.fxgj.server.payroll.service.ActivityConfigureService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.UUIDUtil;
import chain.utils.fxgj.constant.DictEnums.*;
import chain.wage.core.dto.activity.ActivityRequestDTO;
import chain.wage.core.dto.other.GroupAccountDTO;
import chain.wage.core.dto.other.GroupCheckDTO;
import chain.wage.core.dto.response.WageSheetRespone;
import core.dto.request.group.GroupQueryReq;
import core.dto.response.ent.EntErpriseDTO;
import core.dto.response.group.GroupDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Future;


/**
 * 红包配置
 */
@RestController
@Validated
@RequestMapping("/activityConfigure")
@Slf4j
@SuppressWarnings("unchecked")
public class ActivityConfigureRS {
    @Autowired
    ActivityConfigureService activityConfigureService;
    @Autowired
    WechatRedisService wechatRedisService;
    @Autowired
    ActivityConfigureFeignService activityConfigureFeignService;
    @Autowired
    PayrollProperties payrollProperties;
    @Autowired
    UserInfoServiceFeign userInfoServiceFeign;
    @Autowired
    EntBusinessWhiteListServiceFeign entBusinessWhiteListServiceFeign;
    @Autowired
    AccountFeignService accountFeignService;

    /**
     * 根据code 获取openId
     * 然后创建 UserRedisDTO 设置jsessionId、openId、时间、过期时间，最后放入redis
     */
    @GetMapping("/wxCallback")
    @TrackLog
    public Mono<ActivityCallBackDTO> wxCallback(@RequestParam("code") String code, @RequestHeader(value = "routeName", required = false) String routeName) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String jsessionId = UUIDUtil.createUUID32();

            // 用户同意授权
            if (!"authdeny".equals(code)) {
                log.info("=========code={},routeName={}", code, routeName);

                WechatGroupEnum wechatGroup = WechatGroupEnum.FXGJ;
                log.info("wechatGroup:[{}][{}], code:[{}]", wechatGroup.getId(), wechatGroup.getDesc(), code);
                AccessTokenDTO weixinOauthTokenResponeDTO = wechatRedisService.oauth2AccessToken(wechatGroup, code);
                String openId = weixinOauthTokenResponeDTO.getOpenid();
                String accessToken = weixinOauthTokenResponeDTO.getAccessToken();
                log.info("============openId={}, accessToken={}", openId, accessToken);

                if (StringUtils.isEmpty(openId)) {
                    throw new ParamsIllegalException(ErrorConstant.WECHAR_004.getErrorMsg());
                }
                UserRedisDTO redisDTO = new UserRedisDTO();
                redisDTO.setOpenId(openId);
                //设置超时时间
                wechatRedisService.setActivitySessionTimeOut(jsessionId, openId);
                //设置用户缓存
                activityConfigureService.getUserRedis(jsessionId, redisDTO);

                log.info("微信：{},{}", jsessionId, openId);
            }
            ActivityCallBackDTO activityCallBackDTO = ActivityCallBackDTO.builder()
                    .jsessionId(jsessionId)
                    .build();
            return activityCallBackDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 原流程： 根据 jsessionId、path(手机校验/登录)从redis中获取用户信息， 再根据配置企业entId判断是否开放
     *
     * @param jsessionId
     * @param path
     * @return
     */
    private UserRedisDTO getUser(String jsessionId, String path) {
        if (StringUtils.isEmpty(jsessionId)) {
            throw new ParamsIllegalException(ErrorConstant.ACTIVITY_001.getErrorMsg());
        }
        UserRedisDTO userRedisDTO = activityConfigureService.getUserRedis(jsessionId, null);
        if (StringUtils.isEmpty(userRedisDTO.getOpenId())) {
            throw new ParamsIllegalException(ErrorConstant.ACTIVITY_001.getErrorMsg());
        }
        LocalDateTime sessionTimeOut = userRedisDTO.getSessionTimeOut();
        if (LocalDateTime.now().isAfter(sessionTimeOut)) {
            throw new ParamsIllegalException(ErrorConstant.ACTIVITY_001.getErrorMsg());
        }
        if (!path.equals("checkPhone") && !path.equals("login")) {
            if (userRedisDTO.getIsLogin().equals(IsStatusEnum.NO.getCode())) {
                throw new ParamsIllegalException(ErrorConstant.ACTIVITY_002.getErrorMsg());
            }
        }
        log.info("entId:[{}]", userRedisDTO.getEntId());

        if (!path.equals("checkPhone")) {
            log.info("企业白名单查询!");
            EntBusinessWhiteListReq entBusinessWhiteListReq = EntBusinessWhiteListReq.builder()
                    .entId(userRedisDTO.getEntId())
                    .whiteStatus(WhiteListStatusEnum.OPEN)
                    .whiteType(WhiteListTypeEnum.REDPACKETS)
                    .build();
            List<EntBusinessWhiteListRes> entBusinessWhiteListRes = entBusinessWhiteListServiceFeign.entBusinessWhiteList(entBusinessWhiteListReq);
            log.info("entBusinessWhiteListRes:[{}]", JacksonUtil.objectToJson(entBusinessWhiteListRes));
            if (null == entBusinessWhiteListRes || entBusinessWhiteListRes.size() == 0) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("活动内测中，暂未开放！"));
            }
        }
        return userRedisDTO;
    }

    /**
     * 验证手机号是否为虎符用户
     */
    @GetMapping("/checkPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<Void> checkPhone(@RequestHeader("jsession-id") String jsessionId,
                                 @RequestParam(value = "phone") String phone,
                                 @RequestHeader(value = "routeName", required = false) String routeName) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            UserRedisDTO userRedisDTO = this.getUser(jsessionId, "checkPhone");

            log.info("校验用户权限 phone:[{}]", phone);
            UserQueryRequest userQueryRequest = UserQueryRequest.builder()
                    .phone(phone)
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .userStatus(new LinkedList(Arrays.asList(UserStatusEnum.NORMAL)))
                    .build();
            UserInfoRes userInfoRes = userInfoServiceFeign.find(userQueryRequest);
            if (null == userInfoRes) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("用户信息不存在！"));
            }
            if (!UserStatusEnum.NORMAL.equals(userInfoRes.getUserStatus())) {
                throw new ParamsIllegalException(ErrorConstant.ACTIVITY_004.getErrorMsg());
            }
            log.info("====>userInfoRes:[{}]", JacksonUtil.objectToJson(userInfoRes));

            log.info("====>获取 企业信息！entiId={}", userInfoRes.getEntId());
            Future<EntErpriseDTO> futureEnt = activityConfigureService.getEntErpriseInfoRes(userInfoRes.getEntId());

            log.info("====>获取 用户权限!userId={}", userInfoRes.getUserId());
            userQueryRequest.setUserId(userInfoRes.getUserId());
            Future<List<AuthorityInfoRes>> futureAut = activityConfigureService.getUserAuthorityInfo(userQueryRequest);

            //验证企业状态
            EntErpriseDTO entErpriseInfoRes = futureEnt.get();
            if (null == entErpriseInfoRes) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("企业信息不存在！"));
            }
            if (EnterpriseStatusEnum.INVALID.equals(entErpriseInfoRes.getEntStatus()) || EnterpriseStatusEnum.DELETE.equals(entErpriseInfoRes.getEntStatus())) {
                throw new ParamsIllegalException(ErrorConstant.ACTIVITY_003.getErrorMsg());
            }

            //验证用户权限 (M301-工资发放权限)
            List<AuthorityInfoRes> userAuthorityInfo = futureAut.get();
            boolean authorBool = false;
            if (null != userAuthorityInfo && userAuthorityInfo.size() > 0) {
                for (AuthorityInfoRes authorityInfoRes : userAuthorityInfo) {
                    if ("M301".equals(authorityInfoRes.getId())) {
                        authorBool = true;
                        break;
                    }
                }
            }
            if (!authorBool) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("该用户无权限!"));
            }

            //根据用户id查询用户机构列表
            List<GroupInfoRes> userGroupInfo = userInfoServiceFeign.getUserGroupInfo(userQueryRequest);
            List<String> groupIdList = new ArrayList<>();
            for (GroupInfoRes groupInfoRes : userGroupInfo) {
                groupIdList.add(groupInfoRes.getGroupId());
            }
            userRedisDTO.setGroupIdList(groupIdList);

            userRedisDTO.setPhone(userInfoRes.getPhone());
            userRedisDTO.setUserId(userInfoRes.getUserId());
            userRedisDTO.setEntId(userInfoRes.getEntId());

            log.info("checkPhone.setUserRedis jsessionId:[{}], :[{}]", jsessionId, JacksonUtil.objectToJson(userRedisDTO));
            activityConfigureService.setUserRedis(jsessionId, userRedisDTO);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 用户登录
     */
    @PostMapping("login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<Void> login(@RequestHeader("jsession-id") String jsessionId, @RequestBody UserLoginRequestDTO request) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "login");

            if (StringUtils.isEmpty(redisDTO.getUserId())) {
                throw new ParamsIllegalException(ErrorConstant.ACTIVITY_014.getErrorMsg());
            }

            //验证短信验证码
            MsgCodeLogCheckRequestDTO dto = new MsgCodeLogCheckRequestDTO();
            dto.setSystemId(0);
            dto.setCheckType(1);
            dto.setCodeId(request.getSmsCodeId());
            dto.setBusiType(MsgBuisTypeEnum.SMS_01.getCode());
            dto.setCode(request.getSmsCode());
            dto.setMsgMedium(request.getPhone());
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(payrollProperties.getInsideUrl() + "msgCode/smsCodeCheck");
            log.info("管家url:{}", webTarget.getUri());
            Response response = webTarget.request()
                    .header(PayrollDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(PayrollDBConstant.LOG_TOKEN)))
                    .post(Entity.entity(dto, MediaType.APPLICATION_JSON_TYPE));
            log.debug("{}", response.getStatus());
            if (response.getStatus() != 200) {
                ErrorDTO errorDTO = response.readEntity(ErrorDTO.class);
                throw new ParamsIllegalException(new ErrorMsg(errorDTO.getErrorCode(), errorDTO.getErrorMsg()));
            }
            MsgCodeLogResponeDTO msgCodeLogResponeDTO = response.readEntity(MsgCodeLogResponeDTO.class);
            if (msgCodeLogResponeDTO.getMsgStatus() != 1) {
                throw new ParamsIllegalException(ErrorConstant.Error0004.getErrorMsg());
            }

            //保存redis
            redisDTO.setIsLogin(IsStatusEnum.YES.getCode());
            UserRedisDTO userRedisDTO = activityConfigureService.setUserRedis(jsessionId, redisDTO);
            log.info("登录后redis:[{}]", JacksonUtil.objectToJson(userRedisDTO));

            return null;
        }).subscribeOn(Schedulers.elastic()).then();

    }

    /**
     * 用户企业机构
     */
    @GetMapping("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<List<EntGroupDTO>> groups(@RequestHeader("jsession-id") String jsessionId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "groups");
            UserQueryRequest userQueryRequest = UserQueryRequest.builder()
                    .userId(redisDTO.getUserId())
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();

            List<GroupInfoRes> userGroupInfo = userInfoServiceFeign.getUserGroupInfo(userQueryRequest);
            List<EntGroupDTO> entGroups = new LinkedList<>();
            if (null != userGroupInfo || userGroupInfo.size() > 0) {
                for (GroupInfoRes groupInfoRes : userGroupInfo) {
                    EntGroupDTO entGroupDTO = new EntGroupDTO();
                    entGroupDTO.setGroupCnt(groupInfoRes.getEmployeeInfos());
                    entGroupDTO.setGroupName(groupInfoRes.getGroupName());
                    entGroupDTO.setId(groupInfoRes.getGroupId());
                    entGroups.add(entGroupDTO);
                }
            }
            log.info("entGroups:[{}]", JacksonUtil.objectToJson(entGroups));
            return entGroups;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 代发账户查询
     */
    @GetMapping("account")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<List<EntAccountDTO>> accountList(@RequestHeader("jsession-id") String jsessionId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "account");

            //账户查询
            AccountQueryRequest accountQueryRequest = AccountQueryRequest.builder()
                    .entId(redisDTO.getEntId())
                    .build();

            //设置用户机构
            List<String> groupIdList = redisDTO.getGroupIdList();
            if (null != groupIdList && groupIdList.size() > 0) {
                int groupIdListSize = groupIdList.size();
                String[] groupIds = new String[groupIdListSize];
                for (int i = 0; i < groupIdList.size(); i++) {
                    groupIds[i] = groupIdList.get(i);
                }
                accountQueryRequest.setGroupIds(groupIds);
            }
            log.info("account req accountQueryRequest:[{}]", JacksonUtil.objectToJson(accountQueryRequest));
            List<AccountInfoListDTO> accountInfoListDTOS = accountFeignService.accountInfoList(accountQueryRequest);
            log.info("account ret accountInfoListDTOS:[{}]", JacksonUtil.objectToJson(accountInfoListDTOS));


            Iterator iterator = Flux.fromIterable(accountInfoListDTOS)
                    .flatMapSequential(accountInfoListDTO -> Mono.fromCallable(() ->
                            {
                                mdcContext.put(PayrollConstants.SUB_TOKEN, UUIDUtil.createUUID8());
                                MDC.setContextMap(mdcContext);
                                Integer accountStatus = accountInfoListDTO.getAccountStatus();
                                EntAccountDTO entAccountDTO = null;
                                if (null != accountStatus && accountStatus.equals(AccountStatusEnum.BINDING.getCode())) {
                                    entAccountDTO = new EntAccountDTO();
                                    entAccountDTO.setId(accountInfoListDTO.getId());
                                    entAccountDTO.setAccount(accountInfoListDTO.getAccountStar());

                                    BalanceDTO balance = activityConfigureService.getAccountBalance(accountInfoListDTO.getId());
                                    String balanceStr = StringUtils.isEmpty(balance.getBalance()) ? "0" : balance.getBalance();
                                    entAccountDTO.setAccountBalance(new BigDecimal(balanceStr));
                                }
                                return entAccountDTO;
                            }
                    ).subscribeOn(Schedulers.elastic()), 5)
                    .onErrorContinue((err, n) -> log.error("【代发账户】远程调用出现异常: {}" , err.getMessage()))
                    .toIterable()
                    .iterator();

            List<EntAccountDTO> convert = IteratorUtils.toList(iterator);

            log.info("accountDTOList:[{}]", JacksonUtil.objectToJson(convert));
            return convert;
        }).subscribeOn(Schedulers.elastic());

    }

    /**
     * 活动列表查询
     */
    @GetMapping("/acticityList")
    @TrackLog
    public Mono<PageResponseDTO<ActivityListDTO>> acticityList(@RequestHeader("jsession-id") String jsessionId,
                                                               @RequestHeader("page-num") @DefaultValue("1") int pageNum,
                                                               @RequestHeader("limit") @DefaultValue("10") int size,
                                                               @RequestParam(value = "status", required = false) Integer status,
                                                               @RequestHeader(value = "routeName", required = false) String routeName) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "acticityList");
            List<ActivityStatusEnum> activityStatusEnums = new ArrayList<ActivityStatusEnum>();
            if (status == null || status == -1) {
                activityStatusEnums.add(ActivityStatusEnum.CHECK);
                activityStatusEnums.add(ActivityStatusEnum.REFUSE);
                activityStatusEnums.add(ActivityStatusEnum.OPEN);
                activityStatusEnums.add(ActivityStatusEnum.ING);
                activityStatusEnums.add(ActivityStatusEnum.END);
            } else if (status == 3) {
                activityStatusEnums.add(ActivityStatusEnum.REFUSE);
                activityStatusEnums.add(ActivityStatusEnum.OPEN);
            } else {
                activityStatusEnums.add(ActivityStatusEnum.values()[status]);
            }

            ActivityQueryRequest activityQueryPage = ActivityQueryRequest.builder()
                    .entId(redisDTO.getEntId())
                    .activityStatusList(activityStatusEnums)
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();

            log.info("acticityList.acticityPage:[{}]", activityQueryPage);
            PageDTO<ActivityInfoRes> activityInfoResPageDTO = activityConfigureFeignService.activityPage(activityQueryPage);
            LinkedList<ActivityInfoRes> content = new LinkedList<>(activityInfoResPageDTO.getContent()) ;

            Iterator iterator = Flux.fromIterable(content)
                    .flatMapSequential(activityInfoRes -> Mono.fromCallable(() ->
                            {
                                mdcContext.put(PayrollConstants.SUB_TOKEN, UUIDUtil.createUUID8());
                                MDC.setContextMap(mdcContext);

                                ActivityListDTO activityListDTO = activityInfoResToActivityListDTO(activityInfoRes);

                                //红包雨
                                if (activityInfoRes.getActivityType().equals(ActivityTypeEnum.RAIN)) {
                                    ActivityQueryRequest rainActivityRequest = ActivityQueryRequest.builder()
                                            .activityId(activityInfoRes.getActivityId())
                                            .build();
                                    ActivityInfoRes rainActivityInfo = activityConfigureFeignService.findActivityById(rainActivityRequest);
                                    ActivityRainRes activityRain = rainActivityInfo.getActivityRain();
                                    activityListDTO.setRate(activityRain.getRate());
                                    activityListDTO.setMinAmt(activityRain.getMinAmt());
                                    activityListDTO.setMaxAmt(activityRain.getMaxAmt());
                                }

                                //活动结束 查询代发方案
                                if (ActivityStatusEnum.END.equals(activityInfoRes.getActivityStatus())) {
                                    String wageSheetId = activityInfoRes.getWageSheetId();
                                    log.info("acticityList.getWageSheetInfo:[{}]", wageSheetId);
                                    // todo 需要根据 wageSheetId 查询 wageSheetInfo， 获取 发放成功、失败人数
                                    activityListDTO.setSuccessCnt(0);
                                    activityListDTO.setFailCnt(0);
                                }
                                return activityListDTO;
                            }
                    ).subscribeOn(Schedulers.elastic()), 5)
                    .flatMapSequential(activityListDTO -> Mono.fromCallable(() ->
                            {
                                mdcContext.put(PayrollConstants.SUB_TOKEN, UUIDUtil.createUUID8());
                                MDC.setContextMap(mdcContext);

                                //活动操作流程
                                List<ActivityListDTO.ActivityFlow> activityFlowList = new ArrayList<ActivityListDTO.ActivityFlow>();
                                ActivityQueryRequest activityQueryRequestLogs = ActivityQueryRequest.builder()
                                        .activityId(activityListDTO.getId())
                                        .build();
                                List<ActivityFlowLogRes> activityFlowLogs = activityConfigureFeignService.findActivityFlowLogs(activityQueryRequestLogs);

                                for (ActivityFlowLogRes activityFlowLog : activityFlowLogs) {
                                    ActivityListDTO.ActivityFlow activityFlow = new ActivityListDTO.ActivityFlow();
                                    LocalDateTime crtDateTime = activityFlowLog.getCrtDateTime();
                                    if (null != crtDateTime) {
                                        long crtTimeLong = crtDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                        activityFlow.setCrtDateTime(crtTimeLong);
                                    }
                                    activityFlow.setActivityFlow(activityFlowLog.getActivityFlow().getCode());
                                    activityFlow.setActivityFlowStr(activityFlowLog.getActivityFlow().getDesc());
                                    activityFlowList.add(activityFlow);
                                }
                                activityListDTO.setActivityFlowList(activityFlowList);
                                return activityListDTO;
                            }
                    ).subscribeOn(Schedulers.elastic()), 5)
                    .toIterable()
                    .iterator();

            List<ActivityListDTO> convert = IteratorUtils.toList(iterator);

            PageResponseDTO<ActivityListDTO> response = new PageResponseDTO<>(convert,
                    activityInfoResPageDTO.getTotalPages(), activityInfoResPageDTO.getTotalElements(), pageNum, size);
            return response;
        }).subscribeOn(Schedulers.elastic());
    }

    public ActivityListDTO activityInfoResToActivityListDTO(ActivityInfoRes activityInfoRes) {
        ActivityListDTO activityListDTO = new ActivityListDTO();
        activityListDTO.setId(activityInfoRes.getActivityId());
        activityListDTO.setActivityType(activityInfoRes.getActivityType().getCode());
        activityListDTO.setActivityStatus(activityInfoRes.getActivityStatus().getCode());
        activityListDTO.setActivityName(activityInfoRes.getActivityName());
        activityListDTO.setActivitySpeech(activityInfoRes.getActivitySpeech());
        activityListDTO.setBudgetAmt(activityInfoRes.getBudgetAmt());
        activityListDTO.setRealAmt(activityInfoRes.getRealAmt());
        activityListDTO.setTotalCnt(activityInfoRes.getTotalCnt());
        activityListDTO.setRealCnt(activityInfoRes.getRealCnt());
        activityListDTO.setNum(activityInfoRes.getNum());
        activityListDTO.setDuration(activityInfoRes.getDuration());
        activityListDTO.setIsTimeOpen(activityInfoRes.getDuration());
        LocalDateTime startDateTime = activityInfoRes.getStartDateTime();
        if (null != startDateTime) {
            long startTimeLong = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            activityListDTO.setStartDateTime(startTimeLong);
        }
        LocalDateTime endDateTime = activityInfoRes.getEndDateTime();
        if (null != endDateTime) {
            long endTimeLong = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            activityListDTO.setEndDateTime(endTimeLong);
        }

        return activityListDTO;
    }

    /**
     * 查询编辑中的活动
     */
    @GetMapping("ing")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<List<ActivityIngDTO>> ing(@RequestHeader("jsession-id") String jsessionId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "ing");

            //TODO
            //[1] 调用 服务化  activityConfigure/acticityPage
            //[2] 接口改为 分页查询
            ActivityQueryRequest activityQueryRequest = ActivityQueryRequest.builder()
                    .entId(redisDTO.getEntId())
                    .openId(redisDTO.getOpenId())
                    .activityStatusList(new LinkedList(Arrays.asList(ActivityStatusEnum.SUBMIT)))
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();
            PageDTO<ActivityInfoRes> activityInfoResPageDTO = activityConfigureFeignService.activityPage(activityQueryRequest);

            List<ActivityInfoRes> content = activityInfoResPageDTO.getContent();
            List<ActivityIngDTO> list = new ArrayList<>();
            for (ActivityInfoRes activityInfoRes : content) {
                ActivityIngDTO activityIngDTO = new ActivityIngDTO();
                activityIngDTO.setActivityType(activityInfoRes.getActivityType().getCode());
                activityIngDTO.setId(activityInfoRes.getActivityId());
                list.add(activityIngDTO);
            }
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 编辑红包雨活动
     */
    @PostMapping("rain")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<String> addActivityRain(@RequestHeader("jsession-id") String jsessionId,
                                        @RequestBody ActivityRainDTO activityRainDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "rain");

            //TODO
            //[1] 开始时间不能为空
            //    只支持 定时开启
            //    红包时间 1 分钟， 根据 活动开始时间 ，计算出  活动结束时间 =（活动开始时间+1分钟）
            //[2] checkStartEndtime 该时间段已有进行的活动  todo 调用服务接口
            //[3] 根据活动id、openId、RAIN("红包雨"),SUBMIT("编辑")  查询【第一条】活动信息   (【重点】只能 openId用户编辑)
            //    调用 服务化  activityConfigure/###   查询活动
            //    查询到数据，取活ID,
            //    最小金额、 最大金额 为 0 ， todo 前台需要去掉  最小、最大字段
            //[4] 构建 ActivityModifyRequest 对象
            //    调用 服务化  activityConfigure/###   保存活动

            String id = activityConfigureService.saveActivityRain(ActivityStatusEnum.SUBMIT,
                    activityRainDTO, redisDTO.getEntId(), redisDTO.getUserId(), redisDTO.getOpenId());
            return id;
        }).subscribeOn(Schedulers.elastic());

    }

    /**
     * 提交红包雨活动方案
     */
    @PutMapping("rain")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<Void> updActivityRain(@RequestHeader("jsession-id") String jsessionId,
                                      @RequestBody ActivityRainDTO activityRainDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "rain");
            //TODO
            //[1] 开始时间不能为空
            //[2] 活动时间限制  活动开启时间不能在22:00-00:00
            //[3] 开始时间不能为空
            //    只支持 定时开启
            //    红包时间 1 分钟， 根据 活动开始时间 ，计算出  活动结束时间 =（活动开始时间+1分钟）
            //[4] checkStartEndtime 该时间段已有进行的活动  todo 调用服务接口
            //[5] 计算 最小金额、 最大金额
            //[6] 构建 ActivityModifyRequest 对象
            //    调用 服务化  activityConfigure/###   保存活动
            if (activityRainDTO.getStartDateTime() != null && activityRainDTO.getStartDateTime() > 0) {
                check(activityRainDTO.getStartDateTime());
            }
            //Mongo保存
            String entId = redisDTO.getEntId();
            String activityId = activityConfigureService.saveActivityRain(ActivityStatusEnum.CHECK,
                    activityRainDTO, entId, redisDTO.getUserId(), redisDTO.getOpenId());
            log.info("rain.activityId:[{}]", activityId);
            //MySql保存
            //调用服务保存 wageSheet
            String groupId = activityRainDTO.getGroupsList().get(0).getId();
            ActivityRequestDTO activityRequestDTO = ActivityRequestDTO.builder()
                    .activityId(activityId)
                    .wageName(activityRainDTO.getActivityName())
                    .groupId(groupId)
                    .entId(entId)
                    .applyUser(redisDTO.getUserId())
                    .realTotalAmt(activityRainDTO.getBudgetAmt())
                    .build();

            //查询机构信息
            GroupDTO groupInfo = activityConfigureService.findGroupInfo(entId, groupId);

            activityRequestDTO.setIsOrder(groupInfo.getIsOrder());

            activityRequestDTO.setCheckType(groupInfo.getCheckType());

            GroupAccountDTO groupAccountDTO = GroupAccountDTO.builder()
                    .id(activityRainDTO.getAccountId())
                    .account(activityRainDTO.getAccount())
                    .build();
            activityRequestDTO.setGroupAccountDTO(groupAccountDTO);

            //查询机构审核信息
            CheckGroupListReq checkGroupListReq = CheckGroupListReq.builder()
                    .delStatusEnum(DelStatusEnum.normal)
                    .groupId(groupId)
                    .build();
            List<CheckGroupListRes> checkGroupListResList = activityConfigureService.groupCheck(checkGroupListReq);

            List<GroupCheckDTO> checkDTOS = new ArrayList<>();
            for (CheckGroupListRes checkGroupListRes : checkGroupListResList) {
                GroupCheckDTO checkDTO = GroupCheckDTO.builder()
                        .crtDateTime(checkGroupListRes.getCrtDateTime())
                        .orderNum(checkGroupListRes.getOrderNum())
                        .unionId(checkGroupListRes.getUnionId())
                        .userName(checkGroupListRes.getUserName())
                        .userId(checkGroupListRes.getUserId())
                        .build();
                checkDTOS.add(checkDTO);
            }
            activityRequestDTO.setCheckDTOS(checkDTOS);
            log.info("activityRequestDTOReq:[{}]", JacksonUtil.objectToJson(activityRequestDTO));
            WageSheetRespone wageSheetRespone = activityConfigureService.saveWage(activityRequestDTO);

            activityConfigureService.activityUpStatus(activityId, ActivityStatusEnum.CHECK, wageSheetRespone.getWageSheetId());

            //新增活动流程记录
            activityConfigureService.addActivityFlowLog(activityId, redisDTO.getOpenId(), redisDTO.getUserId());

        return null;
        }).subscribeOn(Schedulers.elastic()).then();

    }

    /**
     * 红包雨活动详情
     */
    @GetMapping("rain/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<ActivityRainDTO> activityRain(@RequestHeader("jsession-id") String jsessionId,
                                              @PathVariable("id") String id) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "rain");
            //TODO
            //[1] 根据活动id  取活动信息
            //    活动不存在，则抛错
            //[2] 根据代发账户id,查询 代发账户余额
            //[3] 构建返回数据
            ActivityRainDTO activityRainDTO = new ActivityRainDTO();

            ActivityQueryRequest activityQueryRequest = ActivityQueryRequest.builder()
                    .activityId(id)
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();

            ActivityInfoRes activityInfoRes = activityConfigureFeignService.findActivityById(activityQueryRequest);

            if (null == activityInfoRes) {
                log.info("根据ativityId:[{}]未查询到红包雨活动", id);
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("未查询到红包雨活动！"));
            }
            String accountId = activityInfoRes.getAccountId();
            if (null != accountId) {
                BalanceDTO balance = activityConfigureService.getAccountBalance(accountId);
                String account = balance.getAccount();
                activityRainDTO.setAccountId(accountId);
//                activityRainDTO.setAccount(TransUtil.accountStar(balance.getAccount()));
                activityRainDTO.setAccount(StringUtils.isNotEmpty(account) ? account.substring(account.length() - 4, account.length()): "");
                activityRainDTO.setAccountBalance(new BigDecimal(balance.getBalance()));
            }

            ActivityRainDTO.ActivityRainDetail activityRainDetail = new ActivityRainDTO.ActivityRainDetail();
            ActivityRainRes activityRain = activityInfoRes.getActivityRain();
            activityRainDetail.setId(activityRain.getId());
            activityRainDetail.setMaxAmt(activityRain.getMaxAmt());
            activityRainDetail.setMinAmt(activityRain.getMinAmt());
            activityRainDetail.setRate(activityRain.getRate());
            activityRainDTO.setActivityRainDetail(activityRainDetail);

            activityRainDTO.setId(activityInfoRes.getActivityId());
            activityRainDTO.setActivityType(activityInfoRes.getActivityType().getCode());
            activityRainDTO.setActivityStatus(activityInfoRes.getActivityStatus().getCode());
            activityRainDTO.setActivityName(activityInfoRes.getActivityName());
            activityRainDTO.setActivitySpeech(activityInfoRes.getActivitySpeech());
            activityRainDTO.setBudgetAmt(activityInfoRes.getBudgetAmt());
            activityRainDTO.setRealAmt(activityInfoRes.getRealAmt());
            activityRainDTO.setTotalCnt(activityInfoRes.getTotalCnt());
            activityRainDTO.setRealCnt(activityInfoRes.getRealCnt());
            activityRainDTO.setNum(activityInfoRes.getNum());
            activityRainDTO.setDuration(activityInfoRes.getDuration());
            activityRainDTO.setIsTimeOpen(activityInfoRes.getIsTimeOpen().getCode());
            LocalDateTime startDateTime = activityInfoRes.getStartDateTime();
            if (null != startDateTime) {
                long startLong = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                activityRainDTO.setStartDateTime(startLong);
            }
            LocalDateTime endDateTime = activityInfoRes.getEndDateTime();
            if (null != endDateTime) {
                long endLong = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                activityRainDTO.setEndDateTime(endLong);
            }
            List<ActivityGroupRes> activityGroups = activityInfoRes.getActivityGroups();
            List<EntGroupDTO> groupDTOList = new ArrayList<>();
            for (ActivityGroupRes activityGroup : activityGroups) {
                EntGroupDTO entGroupDTO = new EntGroupDTO();
                entGroupDTO.setId(activityGroup.getId());
                entGroupDTO.setGroupName(activityGroup.getGroupName());
                entGroupDTO.setGroupCnt(activityGroup.getGroupCnt());
                groupDTOList.add(entGroupDTO);
            }
            activityRainDTO.setGroupsList(groupDTOList);
            log.info("activityRainDTO:[{}]", JacksonUtil.objectToJson(activityRainDTO));
            return activityRainDTO;
        }).subscribeOn(Schedulers.elastic());

    }

    /**
     * 编辑随机红包
     */
    @PostMapping("/random")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<String> addActivityRandom(@RequestHeader("jsession-id") String jsessionId,
                                          @RequestBody ActivityRandomDTO activityRandomDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "random");
            //TODO 【X】
            //[1] 判断当前是 操作状态   SUBMIT("编辑"),
            //[2] 根据 entId、openId、RANDOM("随机红包"),SUBMIT("编辑")  查询【第一条】活动信息   (【重点】只能 openId用户编辑)
            // ---> 如果查询到 id is not null ,则 activityId = id  （修改）
            // ---> 如果查询到 id is null  ,则 activityId  is null   （新增）
            //[3] 构建对象
            // --->  id  activityType = RANDOM("随机红包") 、  SUBMIT("编辑")
            String id = activityConfigureService.saveActivityRandom(ActivityStatusEnum.SUBMIT,
                    activityRandomDTO, redisDTO.getEntId(), redisDTO.getUserId(), redisDTO.getOpenId());

            return id;
        }).subscribeOn(Schedulers.elastic());

    }

    /**
     * 提交随机红包
     */
    @PutMapping("/random")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<Void> updActivityRandom(@RequestHeader("jsession-id") String jsessionId,
                                        @RequestBody ActivityRandomDTO activityRandomDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "random");

            //TODO 【X】
            //[1] 开始时间不能为空
            //[2] 活动时间限制  活动开启时间不能在22:00-00:00
            //[3] 构建对象
            // --->  id  activityType = RANDOM("随机红包") 、  CHECK("待审核")

            if (activityRandomDTO.getStartDateTime() != null && activityRandomDTO.getStartDateTime() > 0) {
                check(activityRandomDTO.getStartDateTime());
            }

            //Mongo保存
            String entId = redisDTO.getEntId();
            String activityId = activityConfigureService.saveActivityRandom(ActivityStatusEnum.CHECK,
                    activityRandomDTO, entId, redisDTO.getUserId(), redisDTO.getOpenId());
            //MySql保存
            //调用服务保存 wageSheet
            String groupId = activityRandomDTO.getGroupsList().get(0).getId();
            ActivityRequestDTO activityRequestDTO = ActivityRequestDTO.builder()
                    .activityId(activityId)
                    .wageName(activityRandomDTO.getActivityName())
                    .groupId(groupId)
                    .entId(entId)
                    .applyUser(redisDTO.getUserId())
                    .realTotalAmt(activityRandomDTO.getBudgetAmt())
                    .build();

            //查询机构信息
            GroupDTO groupInfo = activityConfigureService.findGroupInfo(entId, groupId);
            activityRequestDTO.setIsOrder(groupInfo.getIsOrder());

            activityRequestDTO.setCheckType(groupInfo.getCheckType());

            GroupAccountDTO groupAccountDTO = GroupAccountDTO.builder()
                    .id(activityRandomDTO.getAccountId())
                    .account(activityRandomDTO.getAccount())
                    .build();
            activityRequestDTO.setGroupAccountDTO(groupAccountDTO);

            //查询机构审核信息
            CheckGroupListReq checkGroupListReq = CheckGroupListReq.builder()
                    .delStatusEnum(DelStatusEnum.normal)
                    .groupId(groupId)
                    .build();
            List<CheckGroupListRes> checkGroupListResList = activityConfigureService.groupCheck(checkGroupListReq);

            List<GroupCheckDTO> checkDTOS = new ArrayList<>();
            for (CheckGroupListRes checkGroupListRes : checkGroupListResList) {
                GroupCheckDTO checkDTO = GroupCheckDTO.builder()
                        .crtDateTime(checkGroupListRes.getCrtDateTime())
                        .orderNum(checkGroupListRes.getOrderNum())
                        .unionId(checkGroupListRes.getUnionId())
                        .userName(checkGroupListRes.getUserName())
                        .userId(checkGroupListRes.getUserId())
                        .build();
                checkDTOS.add(checkDTO);
            }
            activityRequestDTO.setCheckDTOS(checkDTOS);

            WageSheetRespone wageSheetRespone = activityConfigureService.saveWage(activityRequestDTO);

            //更新活动状态
            activityConfigureService.activityUpStatus(activityId, ActivityStatusEnum.CHECK, wageSheetRespone.getWageSheetId());

            //新增活动流程记录
            activityConfigureService.addActivityFlowLog(activityId, redisDTO.getOpenId(), redisDTO.getUserId());

            return null;
        }).subscribeOn(Schedulers.elastic()).then();

    }

    /**
     * 随机红包活动详情
     */
    @GetMapping("random/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<ActivityRandomDTO> activityRandom(@RequestHeader("jsession-id") String jsessionId,
                                                  @PathVariable("id") String id) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "random");

            //TODO 【X】
            //[1] 根据活动id  取活动信息
            //    活动不存在，则抛错
            //[2] 根据代发账户id,查询 代发账户余额
            //[3] 构建返回数据

            ActivityRandomDTO activityRandom = new ActivityRandomDTO();

            ActivityQueryRequest activityQueryRequest = ActivityQueryRequest.builder()
                    .activityId(id)
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();

            ActivityInfoRes activityInfoRes = activityConfigureFeignService.findActivityById(activityQueryRequest);
            log.info("activityInfoRes:[{}]", JacksonUtil.objectToJson(activityInfoRes));
            if (null == activityInfoRes) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("未查询到随机红包活动！"));
            }

            String accountId = activityInfoRes.getAccountId();
            if (null != accountId) {
                BalanceDTO balance = accountFeignService.getBalance(accountId, null);
                String account = balance.getAccount();
                activityRandom.setAccountId(accountId);
//                activityRandom.setAccount(TransUtil.accountStar(balance.getAccount()));
                activityRandom.setAccount(StringUtils.isNotEmpty(account) ? account.substring(account.length() - 4, account.length()): "");
                activityRandom.setAccountBalance(new BigDecimal(balance.getBalance()));
            }

            List<ActivityInfoRandomRes> activityInfoRandoms = activityInfoRes.getActivityInfoRandoms();
            List<ActivityRandomDTO.ActivityRandomDetail> activityRandomDetailList = new ArrayList<>();
            for (ActivityInfoRandomRes activityInfoRandom : activityInfoRandoms) {
                ActivityRandomDTO.ActivityRandomDetail activityRandomDetail = new ActivityRandomDTO.ActivityRandomDetail();
                activityRandomDetail.setId(activityInfoRandom.getId());
                activityRandomDetail.setRandomAmt(activityInfoRandom.getRandomAmt());
                activityRandomDetail.setRandomName(activityInfoRandom.getRandomName());
                activityRandomDetail.setRandomNum(activityInfoRandom.getRandomNum());
                activityRandomDetailList.add(activityRandomDetail);
            }
            activityRandom.setRandomDetailList(activityRandomDetailList);

            activityRandom.setId(activityInfoRes.getActivityId());
            activityRandom.setActivityType(activityInfoRes.getActivityType().getCode());
            activityRandom.setActivityStatus(activityInfoRes.getActivityStatus().getCode());
            activityRandom.setActivityName(activityInfoRes.getActivityName());
            activityRandom.setActivitySpeech(activityInfoRes.getActivitySpeech());
            activityRandom.setBudgetAmt(activityInfoRes.getBudgetAmt());
            activityRandom.setRealAmt(activityInfoRes.getRealAmt());
            activityRandom.setTotalCnt(activityInfoRes.getTotalCnt());
            activityRandom.setRealCnt(activityInfoRes.getRealCnt());
            activityRandom.setNum(activityInfoRes.getNum());
            activityRandom.setDuration(activityInfoRes.getDuration());
            activityRandom.setIsTimeOpen(activityInfoRes.getIsTimeOpen().getCode());
            LocalDateTime startDateTime = activityInfoRes.getStartDateTime();
            if (null != startDateTime) {
                long startLong = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                activityRandom.setStartDateTime(startLong);
            }
            LocalDateTime endDateTime = activityInfoRes.getEndDateTime();
            if (null != endDateTime) {
                long endLong = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                activityRandom.setEndDateTime(endLong);
            }
            List<ActivityGroupRes> activityGroups = activityInfoRes.getActivityGroups();
            List<EntGroupDTO> groupDTOList = new ArrayList<>();
            for (ActivityGroupRes activityGroup : activityGroups) {
                EntGroupDTO entGroupDTO = new EntGroupDTO();
                entGroupDTO.setId(activityGroup.getId());
                entGroupDTO.setGroupName(activityGroup.getGroupName());
                entGroupDTO.setGroupCnt(activityGroup.getGroupCnt());
                groupDTOList.add(entGroupDTO);
            }
            activityRandom.setGroupsList(groupDTOList);
            log.info("activityRandom:[{}]", JacksonUtil.objectToJson(activityRandom));
            return activityRandom;
        }).subscribeOn(Schedulers.elastic());

    }

    /**
     * 编辑答题活动
     */
    @PostMapping("/answer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<String> addActivityAnswer(@RequestHeader("jsession-id") String jsessionId,
                                          @RequestBody ActivityAnswerDTO activityAnswerDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "answer");
            //TODO 【X】
            //[1] 开始时间不能为空
            //    只支持 定时开启
            //    活动结束时间 ，根据 活动开始时间、答题 活动时长 ，计算出  活动结束时间 =（活动开始时间 + 活动时长）
            //[2] 判断当前是 操作状态   SUBMIT("编辑"),
            //[3] 根据 entId、openId、 ANSWER("答题"),SUBMIT("编辑")  查询【第一条】活动信息   (【重点】只能 openId用户编辑)
            // ---> 如果查询到 id is not null ,则 activityId = id  （修改）
            // ---> 如果查询到 id is null  ,则 activityId  is null   （新增）
            //[3] 构建对象
            // --->  id  activityType =  ANSWER("答题") 、  SUBMIT("编辑")， 问题（对象 转 json）

            String id = activityConfigureService.saveActivityAnswer(ActivityStatusEnum.SUBMIT,
                    activityAnswerDTO, redisDTO.getEntId(), redisDTO.getUserId(), redisDTO.getOpenId());
            return id;
        }).subscribeOn(Schedulers.elastic());

    }

    /**
     * 提交答题活动
     */
    @PutMapping("/answer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<Void> updActivityAnswer(@RequestHeader("jsession-id") String jsessionId,
                                        @RequestBody ActivityAnswerDTO activityAnswerDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            UserRedisDTO redisDTO = this.getUser(jsessionId, "answer");
            if (activityAnswerDTO.getStartDateTime() != null && activityAnswerDTO.getStartDateTime() > 0) {
                check(activityAnswerDTO.getStartDateTime());
            }

            //TODO 【X】
            //[1] 开始时间不能为空
            //[2] 活动时间限制  活动开启时间不能在22:00-00:00
            //[3] 构建对象
            // --->  id  activityType = RANDOM("答题") 、  CHECK("待审核")

            //Mongo保存
            String entId = redisDTO.getEntId();
            String activityId = activityConfigureService.saveActivityAnswer(ActivityStatusEnum.CHECK,
                    activityAnswerDTO, entId, redisDTO.getUserId(), redisDTO.getOpenId());

            //MySql保存
            //调用服务保存 wageSheet
            String groupId = activityAnswerDTO.getGroupsList().get(0).getId();
            ActivityRequestDTO activityRequestDTO = ActivityRequestDTO.builder()
                    .activityId(activityId)
                    .wageName(activityAnswerDTO.getActivityName())
                    .groupId(groupId)
                    .entId(entId)
                    .applyUser(redisDTO.getUserId())
                    .realTotalAmt(activityAnswerDTO.getBudgetAmt())
                    .build();

            //查询机构信息
            GroupDTO groupInfo = activityConfigureService.findGroupInfo(entId, groupId);
            activityRequestDTO.setIsOrder(groupInfo.getIsOrder());

            activityRequestDTO.setCheckType(groupInfo.getCheckType());

            GroupAccountDTO groupAccountDTO = GroupAccountDTO.builder()
                    .id(activityAnswerDTO.getAccountId())
                    .account(activityAnswerDTO.getAccount())
                    .build();
            activityRequestDTO.setGroupAccountDTO(groupAccountDTO);

            //查询机构审核信息
            CheckGroupListReq checkGroupListReq = CheckGroupListReq.builder()
                    .delStatusEnum(DelStatusEnum.normal)
                    .groupId(groupId)
                    .build();
            List<CheckGroupListRes> checkGroupListResList = activityConfigureService.groupCheck(checkGroupListReq);

            List<GroupCheckDTO> checkDTOS = new ArrayList<>();
            for (CheckGroupListRes checkGroupListRes : checkGroupListResList) {
                GroupCheckDTO checkDTO = GroupCheckDTO.builder()
                        .crtDateTime(checkGroupListRes.getCrtDateTime())
                        .orderNum(checkGroupListRes.getOrderNum())
                        .unionId(checkGroupListRes.getUnionId())
                        .userName(checkGroupListRes.getUserName())
                        .userId(checkGroupListRes.getUserId())
                        .build();
                checkDTOS.add(checkDTO);
            }
            activityRequestDTO.setCheckDTOS(checkDTOS);

            WageSheetRespone wageSheetRespone = activityConfigureService.saveWage(activityRequestDTO);

            //更新活动状态
            activityConfigureService.activityUpStatus(activityId, ActivityStatusEnum.CHECK, wageSheetRespone.getWageSheetId());

            //新增活动流程记录
            activityConfigureService.addActivityFlowLog(activityId, redisDTO.getOpenId(), redisDTO.getUserId());

            return null;
        }).subscribeOn(Schedulers.elastic()).then();

    }

    /**
     * 答题活动详情
     */
    @GetMapping("answer/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @TrackLog
    public Mono<ActivityAnswerDTO> activityAnswer(@RequestHeader("jsession-id") String jsessionId,
                                                  @PathVariable("id") String id) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //UserRedisDTO redisDTO=this.getUser(jsessionId,"answer");

            //TODO 【X】
            //[1] 根据活动id  取活动信息
            //    活动不存在，则抛错
            //[2] 根据代发账户id,查询 代发账户余额
            //[3] 构建返回数据   问题（json 转 对象）

            ActivityAnswerDTO activityAnswerDTO = new ActivityAnswerDTO();

            ActivityQueryRequest activityQueryRequest = ActivityQueryRequest.builder()
                    .activityId(id)
                    .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                    .build();

            ActivityInfoRes activityInfoRes = activityConfigureFeignService.findActivityById(activityQueryRequest);
            if (null == activityInfoRes) {
                throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("未查询到答题活动！"));
            }

            String accountId = activityInfoRes.getAccountId();
            if (null != accountId) {
                BalanceDTO balance = accountFeignService.getBalance(accountId, null);
                String account = balance.getAccount();
                activityAnswerDTO.setAccountId(accountId);
                activityAnswerDTO.setAccount(StringUtils.isNotEmpty(account) ? account.substring(account.length() - 4, account.length()): "");
                activityAnswerDTO.setAccountBalance(new BigDecimal(balance.getBalance()));
            }

            List<ActivityInfoAnswerRes> activityInfoAnswer = activityInfoRes.getActivityInfoAnswer();
            List<ActivityAnswerDTO.ActivityAnswerDetail> activityAnswerDetailList = new ArrayList<>();
            for (ActivityInfoAnswerRes activityInfoAnswerRes : activityInfoAnswer) {
                ActivityAnswerDTO.ActivityAnswerDetail activityAnswerDetail = new ActivityAnswerDTO.ActivityAnswerDetail();
                activityAnswerDetail.setCorrectAnswer(activityInfoAnswerRes.getCorrectAnswer());
                activityAnswerDetail.setId(activityInfoAnswerRes.getId());
                activityAnswerDetail.setQuestion(activityInfoAnswerRes.getQuestion());
                //转换
                String answers = activityInfoAnswerRes.getAnswers();
                List<ActivityAnswerDTO.Answer> answerList = JacksonUtil.jsonToList(answers, ActivityAnswerDTO.Answer.class);
                activityAnswerDetail.setAnswers(answerList);
                activityAnswerDetailList.add(activityAnswerDetail);
            }
            activityAnswerDTO.setAnswerDetails(activityAnswerDetailList);

            activityAnswerDTO.setId(activityInfoRes.getActivityId());
            activityAnswerDTO.setActivityType(activityInfoRes.getActivityType().getCode());
            activityAnswerDTO.setActivityStatus(activityInfoRes.getActivityStatus().getCode());
            activityAnswerDTO.setActivityName(activityInfoRes.getActivityName());
            activityAnswerDTO.setActivitySpeech(activityInfoRes.getActivitySpeech());
            activityAnswerDTO.setBudgetAmt(activityInfoRes.getBudgetAmt());
            activityAnswerDTO.setRealAmt(activityInfoRes.getRealAmt());
            activityAnswerDTO.setTotalCnt(activityInfoRes.getTotalCnt());
            activityAnswerDTO.setRealCnt(activityInfoRes.getRealCnt());
            activityAnswerDTO.setNum(activityInfoRes.getNum());
            activityAnswerDTO.setDuration(activityInfoRes.getDuration());
            activityAnswerDTO.setIsTimeOpen(activityInfoRes.getIsTimeOpen().getCode());
            LocalDateTime startDateTime = activityInfoRes.getStartDateTime();
            if (null != startDateTime) {
                long startLong = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                activityAnswerDTO.setStartDateTime(startLong);
            }
            LocalDateTime endDateTime = activityInfoRes.getEndDateTime();
            if (null != endDateTime) {
                long endLong = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                activityAnswerDTO.setEndDateTime(endLong);
            }
            List<ActivityGroupRes> activityGroups = activityInfoRes.getActivityGroups();
            List<EntGroupDTO> groupDTOList = new ArrayList<>();
            for (ActivityGroupRes activityGroup : activityGroups) {
                EntGroupDTO entGroupDTO = new EntGroupDTO();
                entGroupDTO.setId(activityGroup.getId());
                entGroupDTO.setGroupName(activityGroup.getGroupName());
                entGroupDTO.setGroupCnt(activityGroup.getGroupCnt());
                groupDTOList.add(entGroupDTO);
            }
            activityAnswerDTO.setGroupsList(groupDTOList);
            log.info("activityAnswerDTO:[{}]", JacksonUtil.objectToJson(activityAnswerDTO));

            return activityAnswerDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    private void check(Long startTime) {
        //活动开启时间不能在22:00-00:00
        LocalDateTime start = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        int hour = start.getHour();
        if (hour >= 22) {
            throw new ParamsIllegalException(ErrorConstant.ACTIVITY_017.getErrorMsg());
        }
    }

}

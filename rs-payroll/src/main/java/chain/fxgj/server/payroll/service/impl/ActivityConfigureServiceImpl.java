package chain.fxgj.server.payroll.service.impl;

import chain.activity.dto.PageDTO;
import chain.activity.dto.request.activity.*;
import chain.activity.dto.response.activity.ActivityInfoRes;
import chain.activity.feign.ActivityConfigureFeignService;
import chain.css.exception.ParamsIllegalException;
import chain.css.exception.ServiceHandleException;
import chain.feign.hxinside.account.service.AccountFeignService;
import chain.feign.hxinside.ent.service.CheckGroupInfoServiceFeign;
import chain.feign.hxinside.ent.service.EntErpriseInfoServiceFeign;
import chain.feign.hxinside.ent.service.GroupInfoServiceFeign;
import chain.feign.hxinside.ent.service.UserInfoServiceFeign;
import chain.fxgj.account.core.dto.response.account.AccountDetailDTO;
import chain.fxgj.account.core.dto.response.account.BalanceDTO;
import chain.fxgj.core.common.constant.Constants;
import chain.fxgj.ent.core.dto.request.CheckGroupListReq;
import chain.fxgj.ent.core.dto.request.EntErpriseQueryRequest;
import chain.fxgj.ent.core.dto.request.GroupInfoQueryRequest;
import chain.fxgj.ent.core.dto.request.UserQueryRequest;
import chain.fxgj.ent.core.dto.response.AuthorityInfoRes;
import chain.fxgj.ent.core.dto.response.CheckGroupListRes;
import chain.fxgj.ent.core.dto.response.EntErpriseInfoRes;
import chain.fxgj.ent.core.dto.response.GroupInfoResponse;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.activity.*;
import chain.fxgj.server.payroll.service.ActivityConfigureService;
import chain.payroll.client.feign.EntErpriseFeignController;
import chain.payroll.client.feign.GroupFeignController;
import chain.utils.commons.JacksonUtil;
import chain.utils.fxgj.constant.DictEnums.*;
import chain.wage.core.dto.activity.ActivityRequestDTO;
import chain.wage.core.dto.response.WageSheetRespone;
import chain.wage.service.ActivityFeignService;
import core.dto.request.ent.EntErpriseQueryReq;
import core.dto.request.group.GroupQueryReq;
import core.dto.response.ent.EntErpriseDTO;
import core.dto.response.group.GroupDTO;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class ActivityConfigureServiceImpl implements ActivityConfigureService {

    @Autowired
    ActivityConfigureFeignService activityConfigureFeignService;
//    @Autowired
//    EntErpriseInfoServiceFeign entErpriseInfoServiceFeign;
    @Autowired
    UserInfoServiceFeign userInfoServiceFeign;
    @Autowired
    AccountFeignService accountFeignService;
    @Autowired
    ActivityFeignService activityFeignService;
    @Autowired
    GroupInfoServiceFeign groupInfoServiceFeign;
    @Autowired
    CheckGroupInfoServiceFeign checkGroupInfoServiceFeign;

    @Autowired
    EntErpriseFeignController entErpriseFeignController;
    @Autowired
    GroupFeignController groupFeignController;

    @Override
    public UserRedisDTO getUserRedis(String jsessionId, UserRedisDTO redisDTO) {
        if (redisDTO == null) {
            throw new ParamsIllegalException(ErrorConstant.ACTIVITY_001.getErrorMsg());
        }
        redisDTO.setTime(LocalDateTime.now());
        redisDTO.setSessionTimeOut(LocalDateTime.now().plusHours(8));
        return redisDTO;
    }

    @Override
    public UserRedisDTO setUserRedis(String jsessionId, UserRedisDTO redisDTO) {
        return redisDTO;
    }

    /**
     * 获取 企业信息校验
     *
     * @param entId
     * @return
     */
    @Override
    @Async
    public Future<EntErpriseDTO> getEntErpriseInfoRes(String entId) {

        //企业信息
        EntErpriseDTO erpriseDTO = entErpriseFeignController.findById(entId);

        return new AsyncResult(erpriseDTO);
    }

    /**
     * 获取 用户权限
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    @Async
    public Future<List<AuthorityInfoRes>> getUserAuthorityInfo(UserQueryRequest userQueryRequest) {
        List<AuthorityInfoRes> list = userInfoServiceFeign.getUserAuthorityInfo(userQueryRequest);
        return new AsyncResult(list);
    }

    /**
     * 查询余额
     *
     * @param accountId
     * @return
     */
    @Override
    public BalanceDTO getAccountBalance(String accountId) {
        return accountFeignService.getBalance(accountId, null);
    }


    @Override
    public Page<ActivityListDTO> getActivityList(Integer status, String entId, Pageable page) {
        return null;
    }

    @Override
    public List<ActivityIngDTO> getActivityIng(String entId, String openId) {
        return null;
    }

    @Override
    public String saveActivityAnswer(ActivityStatusEnum activityStatusEnum, ActivityAnswerDTO activityAnswerDTO, String entId, String userId, String openId) {
        //校验该时间段是否已有进行的活动
        if (activityAnswerDTO.getStartDateTime() != null && activityAnswerDTO.getIsTimeOpen().equals(IsStatusEnum.YES.getCode())) {
            LocalDateTime start = Instant.ofEpochMilli(activityAnswerDTO.getStartDateTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime end = start.plusSeconds(activityAnswerDTO.getDuration());
            ActivityCheckReq activityCheckReq = new ActivityCheckReq();
            activityCheckReq.setEntId(entId);
            activityCheckReq.setStart(start);
            activityCheckReq.setEnd(end);
            activityConfigureFeignService.checkStartEndtime(activityCheckReq);
        }
        LocalDateTime crtDateTime = LocalDateTime.now();
        String id = "";
        if (activityStatusEnum.equals(ActivityStatusEnum.SUBMIT)) {
            //查询编辑中的活动
            ActivityQueryRequest activityQueryRequest = new ActivityQueryRequest();
            activityQueryRequest.setActivityId(activityAnswerDTO.getId());
            List<ActivityStatusEnum> activityStatusEnumList = new ArrayList<>();
            activityStatusEnumList.add(activityStatusEnum);
            activityQueryRequest.setActivityStatusList(activityStatusEnumList);
            activityQueryRequest.setEntId(entId);
            activityQueryRequest.setOpenId(openId);
            List<DelStatusEnum> delStatusEnumList = new ArrayList<>();
            delStatusEnumList.add(DelStatusEnum.normal);
            activityQueryRequest.setDelStatus(delStatusEnumList);
            List<ActivityTypeEnum> activityTypeEnumList = new ArrayList<>();
            activityTypeEnumList.add(ActivityTypeEnum.ANSWER);
            activityQueryRequest.setActivityTypeList(activityTypeEnumList);
            PageDTO<ActivityInfoRes> activityInfoResPageDTO = activityConfigureFeignService.activityPage(activityQueryRequest);
            List<ActivityInfoRes> content = activityInfoResPageDTO.getContent();
            if (null == content || content.size() == 0) {
                id = new ObjectId().toString();
            } else {
                ActivityInfoRes activityInfoRes = content.get(0);
                id = activityInfoRes.getActivityId();
                crtDateTime = activityInfoRes.getCrtDateTime();
            }
        } else {
            id = activityAnswerDTO.getId();
            ActivityInfoRes activityInfo = findActivityInfo(id);
            crtDateTime = activityInfo.getCrtDateTime();
        }

        //活动设置
        ActivityModifyRequest activityModifyRequest = new ActivityModifyRequest();
        activityModifyRequest.setId(id);
        activityModifyRequest.setActivityName(activityAnswerDTO.getActivityName());
        activityModifyRequest.setActivitySpeech(activityAnswerDTO.getActivitySpeech());
        activityModifyRequest.setBudgetAmt(activityAnswerDTO.getBudgetAmt());
        activityModifyRequest.setTotalCnt(activityAnswerDTO.getTotalCnt());
        activityModifyRequest.setNum(activityAnswerDTO.getNum());
        activityModifyRequest.setDuration(activityAnswerDTO.getDuration());
        activityModifyRequest.setEntId(entId);
        activityModifyRequest.setRealCnt(activityAnswerDTO.getRealCnt());
        Integer isTimeOpen = activityAnswerDTO.getIsTimeOpen();
        if (isTimeOpen != null){
            activityModifyRequest.setIsTimeOpen(chain.utils.fxgj.constant.DictEnums.IsStatusEnum.values()[isTimeOpen]);
        }
        activityModifyRequest.setUserId(userId);
        activityModifyRequest.setAccountId(activityAnswerDTO.getAccountId());
        activityModifyRequest.setOpenId(openId);


        activityModifyRequest.setActivityType(ActivityTypeEnum.ANSWER);
        //编辑、提交都入库为SUBMIT，待方案修改完成之后再修改状态
        activityModifyRequest.setActivityStatus(ActivityStatusEnum.SUBMIT);
        activityModifyRequest.setDelStatus(DelStatusEnum.normal);
        Long startDateTime = activityAnswerDTO.getStartDateTime();
        if (null != startDateTime) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startDateTime), ZoneId.systemDefault());
            activityModifyRequest.setStartDateTime(localDateTime);
            activityModifyRequest.setEndDateTime(localDateTime.plusSeconds(activityAnswerDTO.getDuration()));
        } else {
//            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("请输入活动开始时间！"));
        }
        //机构设置
        List<ActivityGroupRequest> activityGroupRequestList = new ArrayList<>();
        List<EntGroupDTO> groupsList = activityAnswerDTO.getGroupsList();
        if (null != groupsList && groupsList.size() > 0) {
            for (EntGroupDTO entGroupDTO : groupsList) {
                ActivityGroupRequest activityGroupRequest = new ActivityGroupRequest();
                activityGroupRequest.setActivityId(id);
                Long groupCnt = entGroupDTO.getGroupCnt();
                activityGroupRequest.setGroupCnt(groupCnt.intValue());
                activityGroupRequest.setGroupId(entGroupDTO.getId());
                activityGroupRequest.setGroupName(entGroupDTO.getGroupName());
                activityGroupRequestList.add(activityGroupRequest);
            }
        }
        activityModifyRequest.setActivityGroups(activityGroupRequestList);

        List<ActivityInfoAnswerReq> activityInfoAnswerReqList = new ArrayList<>();
        List<ActivityAnswerDTO.ActivityAnswerDetail> answerDetails = activityAnswerDTO.getAnswerDetails();
        for (ActivityAnswerDTO.ActivityAnswerDetail answerDetail : answerDetails) {
            ActivityInfoAnswerReq activityInfoAnswerReq = new ActivityInfoAnswerReq();
            activityInfoAnswerReq.setActivityId(id);
            //答案选项
            List<ActivityAnswerDTO.Answer> answers = answerDetail.getAnswers();
            String answerJson = JacksonUtil.objectToJson(answers);
            activityInfoAnswerReq.setAnswers(answerJson);
            //正确答案
            activityInfoAnswerReq.setCorrectAnswer(answerDetail.getCorrectAnswer());
            //问题
            activityInfoAnswerReq.setQuestion(answerDetail.getQuestion());
            activityInfoAnswerReqList.add(activityInfoAnswerReq);
        }
        activityModifyRequest.setActivityInfoAnswer(activityInfoAnswerReqList);
        activityModifyRequest.setCrtDateTime(crtDateTime);
        if (activityStatusEnum.equals(ActivityStatusEnum.CHECK)) {

            //企业 改用查Mongo所注释 todo 测试完成后可删除
//            EntErpriseQueryRequest entErpriseQueryRequest = new EntErpriseQueryRequest();
//            entErpriseQueryRequest.setEntId(entId);
//            entErpriseQueryRequest.setEntStatus(new LinkedList(Arrays.asList(EnterpriseStatusEnum.NORMAL, EnterpriseStatusEnum.INIT)));
//            EntErpriseInfoRes entErpriseInfoRes = entErpriseInfoServiceFeign.find(entErpriseQueryRequest);

            //代发设置 (生成中奖明细txt需要使用这些账户数据)
            EntErpriseQueryReq entErpriseQueryReq = EntErpriseQueryReq.builder()
                    .entId(entId)
                    .entStatus(new LinkedList<>(Arrays.asList(EnterpriseStatusEnum.INIT, EnterpriseStatusEnum.NORMAL)))
                    .build();
            List<EntErpriseDTO> entErpriseDTOList = entErpriseFeignController.query(entErpriseQueryReq);
            if (null == entErpriseDTOList || entErpriseDTOList.size() <= 0) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未查询到企业信息！"));
            }
            EntErpriseDTO entErpriseInfoRes = entErpriseDTOList.get(0);

            activityModifyRequest.setLiquidation(entErpriseInfoRes.getLiquidation());
            activityModifyRequest.setSubVersion(entErpriseInfoRes.getSubVersion());

            String accountId = activityAnswerDTO.getAccountId();
            AccountDetailDTO accountDetailDTO = accountFeignService.accountDetailInfo(accountId);
            activityModifyRequest.setAccount(accountDetailDTO.getAccount());
            activityModifyRequest.setAccountName(accountDetailDTO.getSignClientName());
            activityModifyRequest.setBatchSpUnitNo(accountDetailDTO.getBatchSpUnitNo());
            activityModifyRequest.setAccountType(AccountTypeEnum.values()[accountDetailDTO.getAccountType()]);
        }

        String activityId = activityConfigureFeignService.modifyActivity(activityModifyRequest);

        return activityId;
    }

    @Override
    public ActivityAnswerDTO getActivityAnswer(String id) {
        return null;
    }

    /**
     * //TODO
     * //[1] 开始时间不能为空
     * //    只支持 定时开启
     * //    红包时间 1 分钟， 根据 活动开始时间 ，计算出  活动结束时间 =（活动开始时间+1分钟）
     * //[2] checkStartEndtime 该时间段已有进行的活动  todo 调用服务接口
     * //[3] 根据活动id、openId、RAIN("红包雨"),SUBMIT("编辑")  查询【第一条】活动信息   (【重点】只能 openId用户编辑)
     * //    调用 服务化  activityConfigure/###   查询活动
     * //    查询到数据，取活ID,
     * //    最小金额、 最大金额 为 0 ， todo 前台需要去掉  最小、最大字段
     * //[4] 构建 ActivityModifyRequest 对象
     * //    调用 服务化  activityConfigure/###   保存活动
     *
     * @param activityStatusEnum
     * @param activityRainDTO
     * @param entId
     * @param userId
     * @param openId
     * @return
     */

    @Override
    public String saveActivityRain(ActivityStatusEnum activityStatusEnum, ActivityRainDTO activityRainDTO, String entId, String userId, String openId) {
        log.info("saveActivityRain request activityRainDTO:[{}]", JacksonUtil.objectToJson(activityRainDTO));
        //校验该时间段是否已有进行的活动
        if (activityRainDTO.getStartDateTime() != null && activityRainDTO.getIsTimeOpen().equals(IsStatusEnum.YES.getCode())) {
            LocalDateTime start = Instant.ofEpochMilli(activityRainDTO.getStartDateTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime end = start.plusSeconds(Constants.RAIN_TIME);
            log.info("saveActivityRain.checkStartEndtime:start[{}],end[{}],entId[{}]", start, end, entId);
            ActivityCheckReq activityCheckReq = new ActivityCheckReq();
            activityCheckReq.setEntId(entId);
            activityCheckReq.setStart(start);
            activityCheckReq.setEnd(end);
            activityConfigureFeignService.checkStartEndtime(activityCheckReq);
        }
        LocalDateTime crtDateTime = LocalDateTime.now();
        String id = "";
        if (activityStatusEnum.equals(ActivityStatusEnum.SUBMIT)) {
            //查询编辑中的活动
            ActivityQueryRequest activityQueryRequest = new ActivityQueryRequest();
            activityQueryRequest.setActivityId(activityRainDTO.getId());
            List<ActivityStatusEnum> activityStatusEnumList = new ArrayList<>();
            activityStatusEnumList.add(activityStatusEnum);
            activityQueryRequest.setActivityStatusList(activityStatusEnumList);
            activityQueryRequest.setEntId(entId);
            activityQueryRequest.setOpenId(openId);
            List<DelStatusEnum> delStatusEnumList = new ArrayList<>();
            delStatusEnumList.add(DelStatusEnum.normal);
            activityQueryRequest.setDelStatus(delStatusEnumList);
            List<ActivityTypeEnum> activityTypeEnumList = new ArrayList<>();
            activityTypeEnumList.add(ActivityTypeEnum.RAIN);
            activityQueryRequest.setActivityTypeList(activityTypeEnumList);
            PageDTO<ActivityInfoRes> activityInfoResPageDTO = activityConfigureFeignService.activityPage(activityQueryRequest);
            List<ActivityInfoRes> content = activityInfoResPageDTO.getContent();
            if (null == content || content.size() == 0) {
                id = new ObjectId().toString();
            } else {
                ActivityInfoRes activityInfoRes = content.get(0);
                id = activityInfoRes.getActivityId();
                crtDateTime = activityInfoRes.getCrtDateTime();
            }
        } else {
            id = activityRainDTO.getId();
            ActivityInfoRes activityInfo = findActivityInfo(id);
            crtDateTime = activityInfo.getCrtDateTime();
        }

        //活动设置
        ActivityModifyRequest activityModifyRequest = new ActivityModifyRequest();
        activityModifyRequest.setId(id);
        activityModifyRequest.setActivityName(activityRainDTO.getActivityName());
        activityModifyRequest.setActivitySpeech(activityRainDTO.getActivitySpeech());
        activityModifyRequest.setBudgetAmt(activityRainDTO.getBudgetAmt());
        activityModifyRequest.setTotalCnt(activityRainDTO.getTotalCnt());
        activityModifyRequest.setNum(activityRainDTO.getNum());
        activityModifyRequest.setDuration(activityRainDTO.getDuration());
        activityModifyRequest.setEntId(entId);
        activityModifyRequest.setRealCnt(activityRainDTO.getRealCnt());
        Integer isTimeOpen = activityRainDTO.getIsTimeOpen();
        if (isTimeOpen != null){
            activityModifyRequest.setIsTimeOpen(chain.utils.fxgj.constant.DictEnums.IsStatusEnum.values()[isTimeOpen]);
        }
        activityModifyRequest.setUserId(userId);
        activityModifyRequest.setAccountId(activityRainDTO.getAccountId());
        activityModifyRequest.setOpenId(openId);

        activityModifyRequest.setActivityType(ActivityTypeEnum.RAIN);
        //新流程为：编辑、提交都入库为SUBMIT，待方案修改完成之后再修改状态
        activityModifyRequest.setActivityStatus(ActivityStatusEnum.SUBMIT);
        activityModifyRequest.setDelStatus(DelStatusEnum.normal);
        Long startDateTime = activityRainDTO.getStartDateTime();
        log.info("startDateTimeLong:[{}]", startDateTime);
        if (null != startDateTime) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startDateTime), ZoneId.systemDefault());
            activityModifyRequest.setStartDateTime(localDateTime);
            activityModifyRequest.setEndDateTime(localDateTime.plusSeconds(Constants.RAIN_TIME));
        }

        //机构设置
        List<ActivityGroupRequest> activityGroupRequestList = new ArrayList<>();
        List<EntGroupDTO> groupsList = activityRainDTO.getGroupsList();
        if (null != groupsList && groupsList.size() > 0) {
            for (EntGroupDTO entGroupDTO : groupsList) {
                ActivityGroupRequest activityGroupRequest = new ActivityGroupRequest();
                activityGroupRequest.setActivityId(id);
                Long groupCnt = entGroupDTO.getGroupCnt();
                activityGroupRequest.setGroupCnt(groupCnt.intValue());
                activityGroupRequest.setGroupId(entGroupDTO.getId());
                activityGroupRequest.setGroupName(entGroupDTO.getGroupName());
                activityGroupRequestList.add(activityGroupRequest);
            }
        }
        activityModifyRequest.setActivityGroups(activityGroupRequestList);

        //红包雨活动设置
        ActivityRainDTO.ActivityRainDetail activityRainDetail = activityRainDTO.getActivityRainDetail();
        ActivityRainRequest activityRainRequest = new ActivityRainRequest();
        activityRainRequest.setActivityId(id);
        activityRainRequest.setMaxAmt(BigDecimal.ZERO);
        activityRainRequest.setMinAmt(BigDecimal.ZERO);
        activityRainRequest.setRate(activityRainDetail.getRate());
        activityModifyRequest.setActivityRain(activityRainRequest);
        activityModifyRequest.setCrtDateTime(crtDateTime);
        if (activityStatusEnum.equals(ActivityStatusEnum.CHECK)) {

            //企业 改用查Mongo所注释 todo 测试完成后可删除
//            EntErpriseQueryRequest entErpriseQueryRequest = new EntErpriseQueryRequest();
//            entErpriseQueryRequest.setEntId(entId);
//            entErpriseQueryRequest.setEntStatus(new LinkedList(Arrays.asList(EnterpriseStatusEnum.NORMAL, EnterpriseStatusEnum.INIT)));
//            EntErpriseInfoRes entErpriseInfoRes = entErpriseInfoServiceFeign.find(entErpriseQueryRequest);

            //代发设置 (生成中奖明细txt需要使用这些账户数据)
            EntErpriseQueryReq entErpriseQueryReq = EntErpriseQueryReq.builder()
                    .entId(entId)
                    .entStatus(new LinkedList<>(Arrays.asList(EnterpriseStatusEnum.INIT, EnterpriseStatusEnum.NORMAL)))
                    .build();
            List<EntErpriseDTO> entErpriseDTOList = entErpriseFeignController.query(entErpriseQueryReq);
            if (null == entErpriseDTOList || entErpriseDTOList.size() <= 0) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未查询到企业信息！"));
            }
            EntErpriseDTO entErpriseInfoRes = entErpriseDTOList.get(0);

            activityModifyRequest.setLiquidation(entErpriseInfoRes.getLiquidation());
            activityModifyRequest.setSubVersion(entErpriseInfoRes.getSubVersion());

            String accountId = activityRainDTO.getAccountId();
            AccountDetailDTO accountDetailDTO = accountFeignService.accountDetailInfo(accountId);
            activityModifyRequest.setAccount(accountDetailDTO.getAccount());
            activityModifyRequest.setAccountName(accountDetailDTO.getSignClientName());
            activityModifyRequest.setBatchSpUnitNo(accountDetailDTO.getBatchSpUnitNo());
            activityModifyRequest.setAccountType(AccountTypeEnum.values()[accountDetailDTO.getAccountType()]);
        }


        log.info("activityModifyRequest:[{}]", JacksonUtil.objectToJson(activityModifyRequest));
        String activityId = activityConfigureFeignService.modifyActivity(activityModifyRequest);
        return activityId;
    }

    @Override
    public ActivityRainDTO getActivityRain(String id) {
        return null;
    }

    /**
     * //TODO 【X】
     * //[1] 判断当前是 操作状态   SUBMIT("编辑"),
     * //[2] 根据 entId、openId、RANDOM("随机红包"),SUBMIT("编辑")  查询【第一条】活动信息   (【重点】只能 openId用户编辑)
     * // ---> 如果查询到 id is not null ,则 activityId = id  （修改）
     * // ---> 如果查询到 id is null  ,则 activityId  is null   （新增）
     * //[3] 构建对象
     * // --->  id  activityType = RANDOM("随机红包") 、  SUBMIT("编辑")
     *
     * @param activityStatusEnum
     * @param activityRandomDTO
     * @param entId
     * @param userId
     * @param openId
     * @return
     */
    @Override
    public String saveActivityRandom(ActivityStatusEnum activityStatusEnum, ActivityRandomDTO activityRandomDTO, String entId, String userId, String openId) {
        LocalDateTime crtDateTime = LocalDateTime.now();
        String id = "";
        if (activityStatusEnum.equals(ActivityStatusEnum.SUBMIT)) {
            //判断是否有编辑中的活动
            ActivityQueryRequest activityQueryRequest = new ActivityQueryRequest();
            activityQueryRequest.setActivityId(activityRandomDTO.getId());
            List<ActivityStatusEnum> activityStatusEnumList = new ArrayList<>();
            activityStatusEnumList.add(activityStatusEnum);
            activityQueryRequest.setActivityStatusList(activityStatusEnumList);
            activityQueryRequest.setEntId(entId);
            activityQueryRequest.setOpenId(openId);
            List<DelStatusEnum> delStatusEnumList = new ArrayList<>();
            delStatusEnumList.add(DelStatusEnum.normal);
            activityQueryRequest.setDelStatus(delStatusEnumList);
            List<ActivityTypeEnum> activityTypeEnumList = new ArrayList<>();
            activityTypeEnumList.add(ActivityTypeEnum.RANDOM);
            activityQueryRequest.setActivityTypeList(activityTypeEnumList);
            PageDTO<ActivityInfoRes> activityInfoResPageDTO = activityConfigureFeignService.activityPage(activityQueryRequest);
            List<ActivityInfoRes> content = activityInfoResPageDTO.getContent();
//            if (null == content || content.size() == 0) {
//                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未查询到编辑中随机红包活动！"));
//            }
            if (null == content || content.size() == 0) {
                id = new ObjectId().toString();
            } else {
                ActivityInfoRes activityInfoRes = content.get(0);
                id = activityInfoRes.getActivityId();
                crtDateTime = activityInfoRes.getCrtDateTime();
            }
        } else {
            id = activityRandomDTO.getId();
            ActivityInfoRes activityInfo = findActivityInfo(id);
            crtDateTime = activityInfo.getCrtDateTime();
        }

        //活动设置
        ActivityModifyRequest activityModifyRequest = new ActivityModifyRequest();
        activityModifyRequest.setId(id);

        activityModifyRequest.setActivityName(activityRandomDTO.getActivityName());
        activityModifyRequest.setActivitySpeech(activityRandomDTO.getActivitySpeech());

        activityModifyRequest.setBudgetAmt(activityRandomDTO.getBudgetAmt());
        activityModifyRequest.setTotalCnt(activityRandomDTO.getTotalCnt());
        activityModifyRequest.setNum(activityRandomDTO.getNum());
        activityModifyRequest.setDuration(activityRandomDTO.getDuration());
        activityModifyRequest.setEntId(entId);
        activityModifyRequest.setRealCnt(activityRandomDTO.getRealCnt());
        Integer isTimeOpen = activityRandomDTO.getIsTimeOpen();
        if (isTimeOpen != null){
            activityModifyRequest.setIsTimeOpen(chain.utils.fxgj.constant.DictEnums.IsStatusEnum.values()[isTimeOpen]);
        }
        activityModifyRequest.setUserId(userId);
        activityModifyRequest.setAccountId(activityRandomDTO.getAccountId());
        activityModifyRequest.setOpenId(openId);
        activityModifyRequest.setActivityType(ActivityTypeEnum.RANDOM);
        //编辑、提交都入库为SUBMIT，待方案修改完成之后再修改状态
        activityModifyRequest.setActivityStatus(ActivityStatusEnum.SUBMIT);
        activityModifyRequest.setDelStatus(DelStatusEnum.normal);
        Long startDateTime = activityRandomDTO.getStartDateTime();
        if (null != startDateTime) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startDateTime), ZoneId.systemDefault());
            activityModifyRequest.setStartDateTime(localDateTime);
            activityModifyRequest.setEndDateTime(localDateTime.plusHours(Constants.RANDOM_TIME));
        } else {
//            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("请输入活动开始时间！!"));
        }

        //机构设置
        List<ActivityGroupRequest> activityGroupRequestList = new ArrayList<>();
        List<EntGroupDTO> groupsList = activityRandomDTO.getGroupsList();
        if (null != groupsList && groupsList.size() > 0) {
            for (EntGroupDTO entGroupDTO : groupsList) {
                ActivityGroupRequest activityGroupRequest = new ActivityGroupRequest();
                activityGroupRequest.setActivityId(id);
                Long groupCnt = entGroupDTO.getGroupCnt();
                activityGroupRequest.setGroupCnt(groupCnt.intValue());
                activityGroupRequest.setGroupId(entGroupDTO.getId());
                activityGroupRequest.setGroupName(entGroupDTO.getGroupName());
                activityGroupRequestList.add(activityGroupRequest);
            }
        }
        activityModifyRequest.setActivityGroups(activityGroupRequestList);

        //随机红包活动设置
        List<ActivityRandomDTO.ActivityRandomDetail> randomDetailList = activityRandomDTO.getRandomDetailList();
        List<ActivityInfoRandomReq> activityInfoRandomReqList = new ArrayList<>();
        for (ActivityRandomDTO.ActivityRandomDetail activityRandomDetail : randomDetailList) {
            ActivityInfoRandomReq activityInfoRandomReq = new ActivityInfoRandomReq();
            activityInfoRandomReq.setActivityId(id);
            activityInfoRandomReq.setRandomAmt(activityRandomDetail.getRandomAmt());
            activityInfoRandomReq.setRandomName(activityRandomDetail.getRandomName());
            activityInfoRandomReq.setRandomNum(activityRandomDetail.getRandomNum());
            activityInfoRandomReqList.add(activityInfoRandomReq);
        }
        activityModifyRequest.setActivityInfoRandoms(activityInfoRandomReqList);
        activityModifyRequest.setCrtDateTime(crtDateTime);
        if (activityStatusEnum.equals(ActivityStatusEnum.CHECK)) {

            //企业 改用查Mongo所注释 todo 测试完成后可删除
//            EntErpriseQueryRequest entErpriseQueryRequest = new EntErpriseQueryRequest();
//            entErpriseQueryRequest.setEntId(entId);
//            entErpriseQueryRequest.setEntStatus(new LinkedList(Arrays.asList(EnterpriseStatusEnum.NORMAL, EnterpriseStatusEnum.INIT)));
//            EntErpriseInfoRes entErpriseInfoRes = entErpriseInfoServiceFeign.find(entErpriseQueryRequest);

            //代发设置 (生成中奖明细txt需要使用这些账户数据)
            EntErpriseQueryReq entErpriseQueryReq = EntErpriseQueryReq.builder()
                    .entId(entId)
                    .entStatus(new LinkedList<>(Arrays.asList(EnterpriseStatusEnum.INIT, EnterpriseStatusEnum.NORMAL)))
                    .build();
            List<EntErpriseDTO> entErpriseDTOList = entErpriseFeignController.query(entErpriseQueryReq);
            if (null == entErpriseDTOList || entErpriseDTOList.size() <= 0) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未查询到企业信息！"));
            }
            EntErpriseDTO entErpriseInfoRes = entErpriseDTOList.get(0);

            activityModifyRequest.setLiquidation(entErpriseInfoRes.getLiquidation());
            activityModifyRequest.setSubVersion(entErpriseInfoRes.getSubVersion());

            String accountId = activityRandomDTO.getAccountId();
            AccountDetailDTO accountDetailDTO = accountFeignService.accountDetailInfo(accountId);
            activityModifyRequest.setAccount(accountDetailDTO.getAccount());
            activityModifyRequest.setAccountName(accountDetailDTO.getSignClientName());
            activityModifyRequest.setBatchSpUnitNo(accountDetailDTO.getBatchSpUnitNo());
            activityModifyRequest.setAccountType(AccountTypeEnum.values()[accountDetailDTO.getAccountType()]);
        }


        String activityId = activityConfigureFeignService.modifyActivity(activityModifyRequest);
        log.info("random update end");
        return activityId;
    }

    @Override
    public ActivityRandomDTO getActivityRandom(String id) {
        return null;
    }

    @Override
    public void activityUpStatus(String activityId, ActivityStatusEnum activityStatus, String wageSheetId) {
        ActivityUpStatusReq activityUpStatusReq = new ActivityUpStatusReq();
        activityUpStatusReq.setActivityId(activityId);
        activityUpStatusReq.setActivityStatus(activityStatus);
        activityUpStatusReq.setWageSheetId(wageSheetId);
        log.info("CHECK start activityUpStatusReq:[{}]", JacksonUtil.objectToJson(activityUpStatusReq));
        activityConfigureFeignService.updActivityStatus(activityUpStatusReq);
        log.info("CHECK end");
    }

    @Override
    public void addActivityFlowLog(String activityId, String openId, String userId) {
        ActivityModifyRequest activityModifyRequest = ActivityModifyRequest.builder()
                .id(activityId)
                .openId(openId)
                .userId(userId)
                .build();
        activityConfigureFeignService.addActivityFlowLog(activityModifyRequest);
    }

    @Override
    public ActivityInfoRes findActivityById(String activityId, ActivityQueryRequest activityQueryRequest) {
        ActivityInfoRes activityInfoRes = activityConfigureFeignService.findActivityById(activityQueryRequest);
        return activityInfoRes;
    }

    @Override
    public WageSheetRespone saveWage(ActivityRequestDTO activityRequestDTO) {
        WageSheetRespone wageSheetRespone = activityFeignService.saveWage(activityRequestDTO);
        log.info("wageSheetRespone:[{}]", JacksonUtil.objectToJson(wageSheetRespone));
        return wageSheetRespone;
    }

    @Override
    public GroupDTO findGroupInfo(String entId, String groupId) {

        //注释原因: 改查mongo todo 测试完成后可删除
//        GroupInfoQueryRequest groupInfoQueryRequest = GroupInfoQueryRequest.builder()
//                .entId(entId)
//                .groupId(groupId)
//                .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
//                .build();
//        GroupInfoResponse groupInfoResponse = groupInfoServiceFeign.find(groupInfoQueryRequest);

        GroupQueryReq queryReq = GroupQueryReq.builder()
                .entId(entId)
                .groupId(groupId)
                .delStatusEnum(DelStatusEnum.normal)
                .build();
        log.info("findGroupInfo.queryReq:[{}]", JacksonUtil.objectToJson(queryReq));
        List<GroupDTO> query = groupFeignController.query(queryReq);

        if (null == query || query.size() > 0) {
            log.error("未查询到机构");
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("未查询到机构！"));
        }
        return query.get(0);
    }

    @Override
    public List<CheckGroupListRes> groupCheck(CheckGroupListReq checkGroupListReq) {
        log.info("checkGroupListReq:[{}]", JacksonUtil.objectToJson(checkGroupListReq));
        List<CheckGroupListRes> CheckGroupListResList = checkGroupInfoServiceFeign.list(checkGroupListReq);
        log.info("CheckGroupListResList:[{}]", JacksonUtil.objectToJson(CheckGroupListResList));
        return CheckGroupListResList;
    }

    /**
     * 根据活动id查询正常的活动详情
     * @param activityId
     */
    public ActivityInfoRes findActivityInfo(String activityId){
        ActivityQueryRequest activityQueryRequest = ActivityQueryRequest.builder()
                .activityId(activityId)
                .delStatus(new LinkedList(Arrays.asList(DelStatusEnum.normal)))
                .build();

        ActivityInfoRes activityInfoRes = activityConfigureFeignService.findActivityById(activityQueryRequest);
        if (null == activityInfoRes) {
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("未查询到活动！"));
        }
        return activityInfoRes;
    }
}

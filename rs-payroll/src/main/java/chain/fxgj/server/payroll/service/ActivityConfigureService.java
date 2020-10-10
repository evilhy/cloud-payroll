package chain.fxgj.server.payroll.service;

import chain.activity.dto.request.activity.ActivityQueryRequest;
import chain.activity.dto.response.activity.ActivityInfoRes;
import chain.fxgj.account.core.dto.response.account.BalanceDTO;
import chain.fxgj.ent.core.dto.request.CheckGroupListReq;
import chain.fxgj.ent.core.dto.request.UserQueryRequest;
import chain.fxgj.ent.core.dto.response.AuthorityInfoRes;
import chain.fxgj.ent.core.dto.response.CheckGroupListRes;
import chain.fxgj.ent.core.dto.response.EntErpriseInfoRes;
import chain.fxgj.ent.core.dto.response.GroupInfoResponse;
import chain.fxgj.server.payroll.dto.activity.*;
import chain.utils.fxgj.constant.DictEnums.ActivityStatusEnum;
import chain.wage.core.dto.activity.ActivityRequestDTO;
import chain.wage.core.dto.response.WageSheetRespone;
import core.dto.request.group.GroupQueryReq;
import core.dto.response.ent.EntErpriseDTO;
import core.dto.response.group.GroupDTO;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.Future;

public interface ActivityConfigureService {

    /**
     * 用户redis信息
     */
    @Cacheable(cacheNames = "activity", key = "'jsession:'.concat(#jsessionId)")
    UserRedisDTO getUserRedis(String jsessionId, UserRedisDTO redisDTO);

    /**
     * 设置redis信息
     */
    @CachePut(cacheNames = "activity", key = "'jsession:'.concat(#jsessionId)")
    UserRedisDTO setUserRedis(String jsessionId, UserRedisDTO redisDTO);


    /**
     * 获取 企业信息
     *
     * @param entId
     * @return
     */
    Future<EntErpriseDTO> getEntErpriseInfoRes(String entId);


    /**
     * 获取 用户权限
     *
     * @param userQueryRequest
     * @return
     */
    Future<List<AuthorityInfoRes>> getUserAuthorityInfo(UserQueryRequest userQueryRequest);


    /**
     * 查询余额
     *
     * @param accountId
     * @return
     */
    BalanceDTO getAccountBalance(String accountId);


    /**
     * 活动列表
     */
    Page<ActivityListDTO> getActivityList(Integer status, String entId, Pageable page);

    /**
     * 编辑中的活动
     */
    List<ActivityIngDTO> getActivityIng(String entId, String openId);

    /**
     * 保存答题活动
     */
    String saveActivityAnswer(ActivityStatusEnum activityStatusEnum, ActivityAnswerDTO activityAnswerDTO, String entId, String userId, String openId);


    /**
     * 答题活动详情
     */
    ActivityAnswerDTO getActivityAnswer(String id);


    /**
     * 保存红包雨活动
     */
    String saveActivityRain(ActivityStatusEnum activityStatusEnum, ActivityRainDTO activityRainDTO, String entId, String userId, String openId);

    /**
     * 红包雨活动详情
     */
    ActivityRainDTO getActivityRain(String id);

    /**
     * 保存随机红包活动
     */
    String saveActivityRandom(ActivityStatusEnum activityStatusEnum, ActivityRandomDTO activityRandomDTO, String entId, String userId, String openId);

    /**
     * 随机红包活动详情
     */
    ActivityRandomDTO getActivityRandom(String id);

    /**
     * 更新活动状态
     * @param activityId
     * @param activityStatus
     */
    void activityUpStatus(String activityId, ActivityStatusEnum activityStatus, String wageSheetId);

    /**
     * 新增活动流程记录
     * @param activityId
     * @param openId
     * @param userId
     */
    void addActivityFlowLog(String activityId, String openId, String userId);

    /**
     * 查询活动详情 缓存1分钟
     * @param activityQueryRequest
     * @return
     */
    @Cacheable(cacheNames = "activityInfo", key = "'activityId:'.concat(#activityId)")
    ActivityInfoRes findActivityById(String activityId, ActivityQueryRequest activityQueryRequest);

    /**
     * 新增方案
     * @param activityRequestDTO
     * @return
     */
    WageSheetRespone saveWage(ActivityRequestDTO activityRequestDTO);

    /**
     * 查询机构信息
     * @param entId
     * @param groupId
     * @return
     */
    GroupDTO findGroupInfo(String entId, String groupId);

    /**
     * 审核人顺序查询参数
     * @param checkGroupListReq
     * @return
     */
    List<CheckGroupListRes> groupCheck(CheckGroupListReq checkGroupListReq);
}

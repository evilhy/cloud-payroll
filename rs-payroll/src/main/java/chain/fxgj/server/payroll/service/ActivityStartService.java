package chain.fxgj.server.payroll.service;

import chain.activity.dto.mq.AccedeAnswerReq;
import chain.activity.dto.mq.AccedeRainReq;
import chain.activity.dto.request.activity.ActivityQueryRequest;
import chain.fxgj.server.payroll.dto.activity.ActivityInfoRequestDTO;
import chain.fxgj.server.payroll.dto.activity.ActivityResponseDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import org.springframework.cache.annotation.CachePut;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface ActivityStartService {


    /**
     * 获取 reidis 值
     *
     * @param redisKey
     * @return
     */
    Object getRedisKey(String redisKey);

    /**
     * @param redisKey  redisKey
     * @param value     value (字符串)
     * @param expiresin 超时间
     * @param timeunit  超时单位
     * @return
     */
    Future<Object> setRedisKey(String redisKey, Object value, Integer expiresin, TimeUnit timeunit);

    /**
     * 活动信息 与 登录人员信息 检查
     *
     * @param activityInfoRequestDTO
     * @param principal
     */
    void checkActivityInfoPrincipal(ActivityInfoRequestDTO activityInfoRequestDTO, UserPrincipal principal);

    /**
     * 根据活动信息校验
     *
     * @param activityResponseDTO    活动信息（查询结果）
     * @param activityInfoRequestDTO 请求参数
     */
    void checkAccedeActivityInfo(ActivityInfoRequestDTO activityInfoRequestDTO, ActivityResponseDTO activityResponseDTO);

    /**
     * 查询正确答案
     *
     * @param activityQueryRequest
     * @return
     */
    @CachePut(cacheNames = "activity", key = "'answer:'.concat(#activityId)")
    String findCorrectAnswer(String activityId, ActivityQueryRequest activityQueryRequest);

    /**
     * 答题异步加入MQ
     *
     * @param accedeAnswerReq
     */
    @Async
    void accedeAnswer(AccedeAnswerReq accedeAnswerReq);

    /**
     * 红包雨异步加入MQ
     *
     * @param accedeRainReq
     */
    @Async
    void accedeRain(AccedeRainReq accedeRainReq);
}

package chain.fxgj.server.payroll.service.impl;

import chain.activity.dto.mq.AccedeAnswerReq;
import chain.activity.dto.mq.AccedeRainReq;
import chain.activity.dto.request.activity.ActivityQueryRequest;
import chain.activity.feign.ActivityConfigureFeignService;
import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.mq.producer.ActivityAnswerProducer;
import chain.fxgj.core.common.mq.producer.ActivityRainProducer;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.activity.ActivityInfoRequestDTO;
import chain.fxgj.server.payroll.dto.activity.ActivityResponseDTO;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.service.ActivityStartService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @program: cloud-payroll
 * @description: 活动参与
 * @author: lius
 * @create: 2019/11/28 14:48
 */
@Slf4j
@Service
@SuppressWarnings("unchecked")
public class ActivityStartServiceImpl implements ActivityStartService {
    @Resource
    RedisTemplate redisTemplate;
    @Autowired
    ActivityConfigureFeignService activityConfigureFeignService;
    @Autowired
    ActivityAnswerProducer activityAnswerProducer;
    @Autowired
    ActivityRainProducer activityRainProducer;

    /**
     * 获取 reidis 值
     *
     * @param redisKey
     * @return
     */
    @Override
    public Object getRedisKey(String redisKey) {
        return redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * @param redisKey  key
     * @param value     value (字符串)
     * @param expiresin 超时间
     * @param timeunit  超时单位
     * @return
     */
    @Override
    public Future<Object> setRedisKey(String redisKey, Object value, Integer expiresin, TimeUnit timeunit) {
        redisTemplate.opsForValue().set(redisKey, value, PayrollConstants.ACTIVITY_EXPIRESIN, TimeUnit.SECONDS);
        return new AsyncResult<>(value);
    }

    /**
     * 活动信息 与 登录人员信息 检查
     *
     * @param activityInfoRequestDTO
     * @param principal
     * @throws Exception
     */
    @Override
    public void checkActivityInfoPrincipal(ActivityInfoRequestDTO activityInfoRequestDTO, UserPrincipal principal) {
        //判断参数必输
        if (StringUtils.isEmpty(activityInfoRequestDTO.getActivityId())) {
            log.error("请求【活动id】为空");
            throw new ParamsIllegalException(ErrorConstant.Error0002.getErrorMsg());
        } else if (StringUtils.isEmpty(activityInfoRequestDTO.getEntId())) {
            log.error("请求【所属企业】为空");
            throw new ParamsIllegalException(ErrorConstant.Error0002.getErrorMsg());
        } else if (principal == null) {
            log.error(" 用户未登录");
            throw new ParamsIllegalException(ErrorConstant.ACTIVITY_007.getErrorMsg());
        }

        List<EntInfoDTO> entInfoList = principal.getEntInfoDTOS();
        if (entInfoList == null || entInfoList.size() < 1) {
            log.error("请求【登录缓存中无企业信息】为空");
            throw new ParamsIllegalException(ErrorConstant.ACTIVITY_007.getErrorMsg());
        }
        Boolean isEntinfo = false;
        for (EntInfoDTO entInfoDTO : entInfoList) {
            if (activityInfoRequestDTO.getEntId().equalsIgnoreCase(entInfoDTO.getEntId())) {
                LinkedList<EntInfoDTO.GroupInfo> groupInfoList = entInfoDTO.getGroupInfoList();
                if (groupInfoList == null || groupInfoList.size() < 1) {
                    log.error("请求【登录缓存中无机构信息】为空");
                    throw new ParamsIllegalException(ErrorConstant.ACTIVITY_007.getErrorMsg());
                }
                String[] groupIds = new String[groupInfoList.size()];
                for (int i = 0; i < groupInfoList.size(); i++) {
                    groupIds[i] = groupInfoList.get(i).getGroupId();
                }
                activityInfoRequestDTO.setGroupIds(groupIds);
                isEntinfo = true;
                break;
            }
        }
        if (!isEntinfo) {
            log.error("请求【上传企业id, 与 缓存中 企业id 】不匹配");
            throw new ParamsIllegalException(ErrorConstant.ACTIVITY_007.getErrorMsg());
        }
    }

    /**
     * 根据活动信息校验
     *
     * @param activityResponseDTO    活动信息（查询结果）
     * @param activityInfoRequestDTO 请求参数
     */
    @Override
    public void checkAccedeActivityInfo(ActivityInfoRequestDTO activityInfoRequestDTO, ActivityResponseDTO activityResponseDTO) {

        //参数校验
        String[] groupIds = activityInfoRequestDTO.getGroupIds();
        HashMap<String, Object> groupMap = activityResponseDTO.getGroupMap();
        Boolean isGroupId = false;
        for (int i = 0; i < groupIds.length; i++) {
            if (groupMap.containsKey(groupIds[i])) {
                isGroupId = true;
                break;
            }
        }
        if (!isGroupId) {
            throw new ParamsIllegalException(ErrorConstant.ACTIVITY_007.getErrorMsg());
        }
    }

    @Override
    public String findCorrectAnswer(String activityId, ActivityQueryRequest activityQueryRequest) {
        String correctAnswer = activityConfigureFeignService.findCorrectAnswer(activityQueryRequest);
        log.info("findCorrectAnswer.ret activityId:[{}], correctAnswer:[{}]", activityQueryRequest.getActivityId(), correctAnswer);
        return correctAnswer;
    }

    @Override
    public void accedeAnswer(AccedeAnswerReq accedeAnswerReq) {
        log.info("activityAnswerProducer.sendMessage:[{}]", JacksonUtil.objectToJson(accedeAnswerReq));
        activityAnswerProducer.sendMessage(accedeAnswerReq);
    }

    @Override
    public void accedeRain(AccedeRainReq accedeRainReq) {
        log.info("activityRainProducer.sendMessage:[{}]", JacksonUtil.objectToJson(accedeRainReq));
        activityRainProducer.sendMessage(accedeRainReq);
    }
}

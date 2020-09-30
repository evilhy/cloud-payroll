package chain.fxgj.server.payroll.dto.activity;

import chain.activity.dto.response.activity.ActivityGroupRes;
import chain.activity.dto.response.activity.ActivityInfoRes;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;


/**
 * 活动基本信息
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unchecked")
public class ActivityResponseDTO {
    /**
     * 活动ID
     */
    private String activityId;
    /**
     * 活动类型 0红包雨 1随机红包 2答题
     */
    private Integer activityType;
    /**
     * 活动类型(描述) 0红包雨 1随机红包 2答题
     */
    private String activityTypeDesc;
    /**
     * 活动状态
     */
    private Integer activityStatus;
    /**
     * 活动状态（描述）
     */
    private String activityStatusDesc;
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 活动欢迎语
     */
    private String activitySpeech;
    /**
     * 预算金额
     */
    private BigDecimal budgetAmt;
    /**
     * 活动时长(秒)
     */
    private Integer duration;
    /**
     * 活动 开奖时间
     */
    private LocalDateTime lotteryDateTime;
    /**
     * 活动 资金清算时间
     */
    private LocalDateTime balanceDateTime;
    /**
     * 企业
     */
    private String entId;
    /**
     * 开始时间
     */
    private long startDateTime;
    /**
     * 结束时间
     */
    private long endDateTime;
    /**
     * 删除状态 (正常、删除)
     */
    private Integer delStatusEnum;
    /**
     * 删除状态 (正常、删除)
     */
    private String delStatusEnumDesc;
    /**
     * 服务器时间
     */
    private long serverDateTime;
    /**
     * 活动参与机构
     */
    private HashMap<String, Object> groupMap;


    public ActivityResponseDTO(ActivityInfoRes activityInfo) {

        this.activityId = activityInfo.getActivityId();
        this.activityType = activityInfo.getActivityType().getCode();
        this.activityTypeDesc = activityInfo.getActivityType().getDesc();
        this.activityStatus = activityInfo.getActivityStatus().getCode();
        this.activityStatusDesc = activityInfo.getActivityStatus().getDesc();
        this.activityName = activityInfo.getActivityName();
        this.activitySpeech = activityInfo.getActivitySpeech();
        this.budgetAmt = activityInfo.getBudgetAmt();
        this.duration = activityInfo.getDuration();
        this.lotteryDateTime = activityInfo.getLotteryDateTime();
        this.balanceDateTime = activityInfo.getBalanceDateTime();
        this.entId = activityInfo.getEntId();
        this.startDateTime = activityInfo.getStartDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.endDateTime = activityInfo.getEndDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.delStatusEnum = activityInfo.getDelStatus().getCode();
        this.delStatusEnumDesc = activityInfo.getDelStatus().getDesc();
        this.serverDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        List<ActivityGroupRes> activityGroupList = activityInfo.getActivityGroups();
        if (activityGroupList != null) {
            if (groupMap == null) {
                groupMap = new HashMap<String, Object>();
            }
            for (int i = 0, len = activityGroupList.size(); i < len; i++) {
                groupMap.put(activityGroupList.get(i).getGroupId(), activityGroupList.get(i).getGroupId());
            }
        }
    }


}

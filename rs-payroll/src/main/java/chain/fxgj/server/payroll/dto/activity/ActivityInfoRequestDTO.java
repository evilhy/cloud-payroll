package chain.fxgj.server.payroll.dto.activity;

import chain.utils.fxgj.constant.DictEnums.ActivityStatusEnum;
import lombok.*;

import java.util.List;

/**
 * 查询活动
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ActivityInfoRequestDTO {
    /**
     * 活动ID
     */
    private String activityId;
    /**
     * 活动类型
     */
    private Integer activityType;
    /**
     * 所属企业
     */
    private String entId;
    /**
     * 所属机构（员工入口查询时，不需要）
     */
    private String[] groupIds;
    /**
     * 活动状态（员工入口查询时，不需要）
     */
    private List<ActivityStatusEnum> activityStatus;
}

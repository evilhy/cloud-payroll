package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 编辑中的活动
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityIngDTO {
    /**
     * id
     */
    private String id;
    /**
     * 活动类型 0红包雨 1随机红包 2答题
     */
    private Integer activityType;

}

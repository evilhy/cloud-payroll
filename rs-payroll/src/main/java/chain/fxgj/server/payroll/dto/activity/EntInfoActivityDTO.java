package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 企业基本信息(活动使用)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntInfoActivityDTO {
    /**
     * 是否绑定 1已绑定 0未绑定
     */
    private String bindStatus;

    private List<EntInfo> entInfoList = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntInfo {
        /**
         * 企业id
         */
        private String entId;

        /**
         * 企业名称
         */
        private String entName;

        /**
         * 活动ID, 不为空则有活动，为空则无活动
         */
        private String activityId;

        /**
         * 活动类型 0红包雨 1随机红包 2答题
         */
        private Integer activityType;

        /**
         * 活动类型 0红包雨 1随机红包 2答题
         */
        private String activityTypeDesc;
    }

}

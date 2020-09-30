package chain.fxgj.server.payroll.dto.activity;

import chain.activity.dto.response.activity.ActivityInfoRandomRes;
import chain.activity.dto.response.activity.ActivityInfoRes;
import lombok.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * （手气）活动详情
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unchecked")
public class ActivityRandomResponseDTO extends ActivityResponseDTO {
    /**
     * 活动数据
     */
    private List<ActivityRandomDetail> randomDetails;
    /**
     * 是否参与手气用包活动（ false 否 true 是）
     */
    private Boolean isRandom;


    public ActivityRandomResponseDTO(ActivityInfoRes activityInfo) {
        super(activityInfo);
        randomDetails = new LinkedList<ActivityRandomDetail>();
    }

    /**
     * 手气红包
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityRandomDetail {
        /**
         * id
         */
        private String id;
        /**
         * 红包名称
         */
        private String randomName;
        /**
         * 红包个数
         */
        private Integer randomNum;
        /**
         * 红包个数金额
         */

        private BigDecimal randomAmt;


        public ActivityRandomDetail(ActivityInfoRandomRes activityInfoRandomRes) {
            this.id = activityInfoRandomRes.getId();
            this.randomName = activityInfoRandomRes.getRandomName();
            this.randomNum = activityInfoRandomRes.getRandomNum();
            this.randomAmt = activityInfoRandomRes.getRandomAmt();
        }
    }

}

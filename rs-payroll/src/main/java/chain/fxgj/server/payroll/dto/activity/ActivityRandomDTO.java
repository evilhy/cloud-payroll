package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 随机红包活动
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ActivityRandomDTO extends ActivityInfoDTO {
    /**
     * 活动数据
     */
    private List<ActivityRandomDetail> randomDetailList;


    /**
     * 随机红包
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
         * 个数
         */
        private Integer randomNum;
        /**
         * 金额
         */
        private BigDecimal randomAmt;

    }
}

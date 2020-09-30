package chain.fxgj.server.payroll.dto.activity;

import chain.activity.dto.response.activity.ActivityInfoRes;
import lombok.*;

import java.math.BigDecimal;

/**
 * （红包雨）活动详情
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unchecked")
public class ActivityRainResponseDTO extends ActivityResponseDTO {
    /**
     * 是否参与红包雨（ false 否 true 是）
     */
    private Boolean isRain;
    /**
     * 中奖率
     */
    private Double rate;
    /**
     * 最小金额
     */
    private BigDecimal minAmt;
    /**
     * 最大金额
     */
    private BigDecimal maxAmt;


    public ActivityRainResponseDTO(ActivityInfoRes activityInfo) {
        super(activityInfo);
    }
}

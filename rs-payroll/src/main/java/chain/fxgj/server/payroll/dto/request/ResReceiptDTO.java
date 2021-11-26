package chain.fxgj.server.payroll.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 回执
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResReceiptDTO {
    /**
     * 工资id
     */
    private String wageDetailId;
    /**
     * 回执状态 0确认无误,1有反馈信息
     */
    private Integer receiptsStatus;
    /**
     * 回执信息
     */
    private String msg;

}

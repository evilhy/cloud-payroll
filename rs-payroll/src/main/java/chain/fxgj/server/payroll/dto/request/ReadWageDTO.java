package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * 已读工资
 * */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadWageDTO {
    /**
     * 工资方案id
     */
    private String wageSheetId;
    /**
     * 员工
     */
    private String idNumber;

}

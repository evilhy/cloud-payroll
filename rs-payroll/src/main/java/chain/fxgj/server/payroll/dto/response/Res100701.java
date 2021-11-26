package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Res100701
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Res100701 {
    /**
     * 是否绑定 1已绑定 0未绑定
     */
    @Builder.Default
    private String bindStatus = "0";
    /**
     * 员工信息列表(身份证号为绑定时显示)
     */
    @Builder.Default
    private List<EmployeeListBean> employeeList = new ArrayList<>();

}

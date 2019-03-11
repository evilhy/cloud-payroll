package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * 通知企业分配客户经理
 * */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistributeDTO {
    /**
     * 机构id
     */
    private String groupId;

}

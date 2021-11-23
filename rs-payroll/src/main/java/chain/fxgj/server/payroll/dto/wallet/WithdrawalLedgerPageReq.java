package chain.fxgj.server.payroll.dto.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Description:提现分页列表
 * @Author: du
 * @Date: 2021/7/14 17:56
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawalLedgerPageReq {

    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;
    /**
     * 提现状态（0:待提现、1:提现成功、2:提现失败、3:处理中）
     */
    private List<Integer> withdrawalStatus;
}

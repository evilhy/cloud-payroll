package chain.fxgj.server.payroll.dto.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Description:提现进度
 * @Author: du
 * @Date: 2021/7/15 9:59
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
public class WithdrawalRecordDetailRes {

    /**
     * 提现记录
     */
    WithdrawalRecordLogDTO withdrawalRecordLog;

    /**
     *提现台账详情
     */
    WithdrawalLedgerDetailRes withdrawalLedgerDetail;
}

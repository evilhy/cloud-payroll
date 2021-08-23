package chain.fxgj.server.payroll.dto.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description:shi是否允许提现
 * @Author: du
 * @Date: 2021/8/23 18:21
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
@XmlRootElement(name = "IsAllowWithdrawRes")
public class IsAllowWithdrawRes {

    /**
     * 企业ID
     */
    String entId;
    /**
     * 身份证号码
     */
    String idNumber;
    /**
     * 是否允许提现
     */
    Boolean status;
}

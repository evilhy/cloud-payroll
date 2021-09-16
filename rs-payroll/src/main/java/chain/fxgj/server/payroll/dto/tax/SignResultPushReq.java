package chain.fxgj.server.payroll.dto.tax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description:签约结果推送 请求信息
 * @Author: du
 * @Date: 2021/8/23 14:31
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
@XmlRootElement(name = "SignResultPushReq")
public class SignResultPushReq {

    /**
     * 交易用户id
     */
    String transUserId;
    /**
     * 签约状态
     */
    Boolean isAuth;
}

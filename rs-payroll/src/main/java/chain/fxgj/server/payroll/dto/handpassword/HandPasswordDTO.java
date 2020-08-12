package chain.fxgj.server.payroll.dto.handpassword;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * @Description:
 * @Author: du
 * @Date: 2020/8/11 20:54
 */
@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HandPasswordDTO {

    /**
     * 是否开启手势密码 0:未开启   1：已开启
     */
    Integer status;

    /**
     * 是否开启手势密码 描述
     */
    String statusVal;
}

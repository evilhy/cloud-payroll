package chain.fxgj.server.payroll.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * @Description:保存密码
 * @Author: du
 * @Date: 2020/8/17 10:37
 */
@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordSaveReq {

    /**
     * 第一次输入的密码  （数字密码以“,”分隔）
     */
    String firstPassword;

    /**
     * 确认密码 （数字密码以“,”分隔）
     */
    String password;

    /**
     * 类型 0:数字密码    1：手势密码
     */
    String type;
}

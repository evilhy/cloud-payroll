package chain.fxgj.server.payroll.dto.elife;

import chain.fxgj.server.payroll.constant.DictEnums.IsStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * e生活微信用户
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    /**
     * jsessionId
     */
    private String jsessionId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 是否已参与 0否 1是
     */
    @Builder.Default
    private Integer join = IsStatusEnum.NO.getCode();

}

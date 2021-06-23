package chain.fxgj.server.payroll.dto.payroll;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/6/11 17:40
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@SuppressWarnings("unchecked")
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentDTO {

    /**
     * 表单属性名
     */
    String name;

    /**
     * 表单属性值
     */
    String value;
}

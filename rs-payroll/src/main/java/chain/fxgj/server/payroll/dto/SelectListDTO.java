package chain.fxgj.server.payroll.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉框
 *
 * @author chain
 * create by chain on 2018/12/19 10:31 PM
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("unchecked")
public class SelectListDTO {
    /**
     * 代码
     */
    private Integer code;
    /**
     * 描述
     */
    private String desc;
}

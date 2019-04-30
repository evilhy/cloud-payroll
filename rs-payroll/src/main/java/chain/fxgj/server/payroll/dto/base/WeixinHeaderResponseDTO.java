package chain.fxgj.server.payroll.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author chain
 * create by chain on 2018/9/10 上午9:48
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WeixinHeaderResponseDTO {
    /**
     * 错误码
     */
    @JsonProperty("errcode")
    private Integer errcode;
    /**
     * 错误信息
     */
    @JsonProperty("errmsg")
    private String errmsg;

}

package chain.fxgj.server.payroll.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author lius
 **/
@XmlRootElement(name = "WeixinExtResponeDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WeixinExtResponeDTO extends WeixinHeaderResponseDTO {
    /**
     * 扩展信息 key
     */
    String key;
    /**
     * 扩展信息 value
     */
    Object value;
}

package chain.fxgj.server.payroll.dto.tax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description:协议列表 响应参数
 * @Author: du
 * @Date: 2021/9/15 15:50
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
@XmlRootElement(name = "TempListRes")
public class TempListRes {

    /**
     * 模板id
     */
    String templateId;
    /**
     * 模板编号
     */
    String templateNo;
    /**
     * 模板名称
     */
    String templateName;
    /**
     * 模板预览url
     */
    String url;
}

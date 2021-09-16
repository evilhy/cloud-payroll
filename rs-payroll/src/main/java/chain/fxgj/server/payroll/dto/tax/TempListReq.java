package chain.fxgj.server.payroll.dto.tax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/9/15 15:51
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
@XmlRootElement(name = "TempListReq")
public class TempListReq {

    /**
     * 服务机构id
     */
    String fwOrgId;
    /**
     * 服务机构
     */
    String fwOrg;
    /**
     * 用工单位Id
     */
    String ygOrgId;
    /**
     * 用工单位
     */
    String ygOrg;
}

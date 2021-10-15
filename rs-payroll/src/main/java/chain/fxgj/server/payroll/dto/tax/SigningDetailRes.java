package chain.fxgj.server.payroll.dto.tax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/10/11 17:45
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
@XmlRootElement(name = "SigningDetailRes")
public class SigningDetailRes {

    /**
     * ID
     */
    private String taxSignId;
    /**
     * 身份认证ID
     */
    private String empTaxAttestId;
    /**
     * 企业ID
     */
    private String entId;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 企业编号
     */
    private String entNum;
    /**
     * 机构（用工单位）ID
     */
    private String groupId;
    /**
     * 机构（用工单位）名称
     */
    private String groupName;
    /**
     * 用工单位编号
     */
    private String groupNum;
    /**
     * 模板ID（协议ID）
     */
    private String templateId;
    /**
     * 模板编号
     */
    private String templateNo;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 是否完成签约 (0:未签约、1：已签约)
     */
    private Integer signStatus;
    /**
     * 是否完成签约 (0:未签约、1：已签约)
     */
    private String signStatusVal;
    /**
     * 签约成功时间
     */
    private Long signDateTime;
}

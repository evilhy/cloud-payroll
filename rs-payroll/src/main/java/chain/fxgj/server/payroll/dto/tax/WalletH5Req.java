package chain.fxgj.server.payroll.dto.tax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description:用户签约    请求信息
 * @Author: du
 * @Date: 2021/8/23 10:57
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
@XmlRootElement(name = "WalletH5Req")
public class WalletH5Req {

    /**
     * 用户ID
     */
    String transUserId;
    /**
     * 姓名
     */
    String userName;
    /**
     * 手机号
     */
    String phone;
    /**
     * 证件类型 (SFZ:身份证,HZ:护照)
     */
    String idType;
    /**
     * 证件号码
     */
    String idCardNo;
    /**
     * 服务机构Id
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
    /**
     * 协议模板id
     */
    String templateId;
}

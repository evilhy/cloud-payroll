package chain.fxgj.server.payroll.dto.tax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/23 14:34
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
@XmlRootElement(name = "SigningDetailsReq")
public class SigningDetailsReq {

    /**
     *签约信息ID
     */
    String taxSignId;
    /**
     *  姓名
     */
    String userName;
    /**
     *证件类型 1：身份证、 2：外国护照
     */
    String idType;
    /**
     *证件号码
     */
    String idNumber;
    /**
     *手机号
     */
    String phone;
    /**
     *身份证正面
     */
    String idCardFront;
    /**
     *身份证反面
     */
    String idCardNegative;
}

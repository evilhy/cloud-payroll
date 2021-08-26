package chain.fxgj.server.payroll.dto.tax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/23 12:51
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
@XmlRootElement(name = "SealUserReq")
public class SealUserReq {


    /**
     * 交易用户id
     */
    String transUserId	;
    /**
     *姓名
     */
    String userName	;
    /**
     *手机号
     */
    String phoneNo	;
    /**
     *证件类型 (SFZ:身份证,HZ:护照)
     */
    String idType	;
    /**
     *证件号码
     */
    String idCardNo	;
    /**
     *身份证正面 Base64
     */
    String idCardImg1	;
    /**
     *身份证反面 Base64
     */
    String idCardImg2	;
    /**
     *服务机构
     */
    String fwOrg;
    /**
     *用工单位
     */
    String ygOrg;

}

package chain.fxgj.server.payroll.dto.tax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description:身份认证详情
 * @Author: du
 * @Date: 2021/10/11 17:32
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
@XmlRootElement(name = "TaxAttestDetailRes")
public class TaxAttestDetailRes {

    /**
     * 认证信息ID
     */
    private String taxAttestId;
    /**
     * 姓名
     */
    private String userName;
    /**
     * 证件类型 1：身份证、 2：外国护照
     */
    private String idType;
    /**
     * 证件类型 描述
     */
    private String idTypeVal;
    /**
     * 证件号码
     */
    private String idNumber;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 身份证正面
     */
    private String idCardFront;
    /**
     * 身份证反面
     */
    private String idCardNegative;
    /**
     * 认证状态（0：未认证、1：认证中、2：认证失败、3：认证成功）
     */
    private Integer attestStatus;
    /**
     * 认证状态描述
     */
    private String attestStatusVal;
    /**
     * 省、自治区、直辖市
     */
    private String provinceCode;
    /**
     * 省、自治区、直辖市
     */
    private String provinceName;
    /**
     * 地级市、地区、自治州、盟
     */
    private String cityCode;
    /**
     * 地级市、地区、自治州、盟
     */
    private String cityName;
    /**
     * 市辖区、县级市、县
     */
    private String areaCode;
    /**
     * 市辖区、县级市、县
     */
    private String areaName;
    /**
     * 街道、镇、乡
     */
    private String streetCode;
    /**
     * 街道、镇、乡
     */
    private String streetName;
    /**
     * 详细地址
     */
    private String address;
    /**
     * 认证失败原因
     */
    private String attestFailMsg;
    /**
     * salt
     */
    private String salt;
    /**
     * password
     */
    private String passwd;
}

package chain.fxgj.server.payroll.dto.wisales;

import chain.wisales.core.constant.dictEnum.YesOrNoEnum;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PayrllWelfareCustAddressInfoDTO {
    /**
     * 客户收货地址ID YYYYMMDD+7位序列
     */
    private String addressId;

    /**
     * 客户Id
     */
    private String custId;
    /**
     * 客户姓名
     */
    private String custName;
    /**
     * 手机号
     */

    private String phoneNo;
    /**
     * UID
     */

    private String uid;

    /**
     * 收货人
     */

    private String receiveName;

    /**
     * 收货人手机号
     */

    private String receivePhone;

    /**
     * 省
     */

    private String province;
    /**
     * 省编码
     */

    private String provinceCode;
    /**
     * 市
     */

    private String city;
    /**
     * 市编码
     */

    private String cityCode;

    /**
     * 城乡
     */

    private String county;
    /**
     * 城乡编码
     */

    private String countyCode;
    /**
     * 镇
     */
    private String town;
    /**
     * 镇编码
     */
    private String townCode;
    /**
     * 详细地址
     */

    private String address;
    /**
     * 是否默认地址
     */

    private YesOrNoEnum isDefault;

    /**
     * 是否默认地址描述
     */
    private String isDefaultDesc;

    /**
     * 证件号
     */
    private String idNumber;

    /**
     * salt
     */
    private String salt;

    /**
     * password
     */
    private String passwd;
}

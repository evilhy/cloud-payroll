package chain.fxgj.server.payroll.dto.payroll;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/6/22 19:50
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@Accessors(chain = true)
@XmlRootElement(name = "SignedReceiptPdfDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignedReceiptPdfDTO {

    /**
     * 方案ID
     */
    String detailId;

    /**
     * 收款卡号
     */
    String bankCard;
    /**
     * 资金月份
     */
    String fundDate;
    /**
     * 资金类型
     */
    String fundType;
    /**
     * 身份证
     */
    String idNumber;
    /**
     * 客户名称
     */
    String custName;
    /**
     * 备注
     */
    String remark;
    /**
     * 发放机构
     */
    String groupName;
    /**
     * 方案名称
     */
    String wageName;
    /**
     * 创建时间
     */
    Long crDateTime;
    /**
     * 付款账户名称
     */
    String accountName;
    /**
     * 发放时间
     */
    Long applyDateTime;
    /**
     * 账户号码
     */
    String account;
    /**
     * 签名（图片地址）
     */
    String signUrl;
    /**
     * 交易金额
     */
    BigDecimal amt;
}

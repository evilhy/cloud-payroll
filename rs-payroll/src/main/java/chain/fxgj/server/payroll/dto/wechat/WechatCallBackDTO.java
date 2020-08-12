package chain.fxgj.server.payroll.dto.wechat;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * 微信回调
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class WechatCallBackDTO {


    /**
     * 微信code
     */
    @NotNull
    private String code;

    /**
     * 方案id(暂未使用)
     */
    private String wageSheetId;

    /**
     * 清算通道
     */
    @NotNull
    private chain.utils.fxgj.constant.DictEnums.AppPartnerEnum appPartner = chain.utils.fxgj.constant.DictEnums.AppPartnerEnum.FXGJ;

    /**
     * 规则(暂未使用)
     */
    private String routeName;

}

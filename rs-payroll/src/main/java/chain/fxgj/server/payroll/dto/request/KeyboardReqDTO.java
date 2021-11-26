package chain.fxgj.server.payroll.dto.request;

import chain.ids.core.commons.enums.EnumKeyboardType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 作者：程彪
 * 时间：2021/11/23 16:45
 */
@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyboardReqDTO {

    /**
     * 键盘申请ID
     */
    private String keyboardId;
    /**
     * 密码键盘类型
     * <p>
     * 字母键盘
     * 纯数字键盘
     * 身份证键盘
     * 金额键盘
     */
    private EnumKeyboardType keyboardType;


    /**
     * 是否随机乱序
     * true 乱序  false 顺序
     */
    @Builder.Default
    private Boolean shuffle = true;

    /**
     * 是否显示logo
     */
    @Builder.Default
    private Boolean topLogo = true;

    /**
     * 是否需要【确认】键
     */
    @Builder.Default
    private Boolean confirm = true;
}

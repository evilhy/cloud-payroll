package chain.fxgj.server.payroll.dto.response;

import chain.ids.core.commons.enums.EnumKeyboardType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 作者：程彪
 * 时间：2021/11/23 17:09
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
public class KeyboardResDTO {
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
     * shuffle 是否随机乱序
     * true 乱序  false 顺序
     */
    @Builder.Default
    private Boolean shuffle = true;
    /**
     * 键盘base64  小写字母
     */
    @Builder.Default
    private String lowercaseBase64 = StringUtils.EMPTY;
    /**
     * 键盘base64  大写字母
     */
    @Builder.Default
    private String uppercaseBase64 = StringUtils.EMPTY;
    /**
     * 键盘base64  数字键盘
     * <p>
     * 纯数字键盘
     * 身份证键盘
     * 金额键盘
     */
    @Builder.Default
    private String numberBase64 = StringUtils.EMPTY;

    //产生a ~ z的字符          （字母键盘）
    Map<String, Character> lower;

    //产生A ~ Z的字符          （字母键盘）
    Map<String, Character> upper;

    //随机 产生 0 ~ 9的数字    (字母键盘/数字键盘)
    Map<String, Character> number;

    //身份证数字               （身份证键盘）
    Map<String, Character> idCard;
}

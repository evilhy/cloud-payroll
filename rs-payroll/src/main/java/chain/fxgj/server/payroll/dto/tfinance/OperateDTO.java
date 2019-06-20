package chain.fxgj.server.payroll.dto.tfinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 用户操作列表
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class OperateDTO {
    /**
     * 系统当前时间
     */
    private Long nowDate;
    /**
     * 操作类型 0 浏览 1预约
     */
    private Integer operate;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像
     */
    private String headimgurl;
    /**
     * 创建时间
     */
    private Long crtDateTime;


}

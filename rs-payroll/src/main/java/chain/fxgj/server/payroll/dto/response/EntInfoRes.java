package chain.fxgj.server.payroll.dto.response;

import lombok.*;

/**
 * 企业基本信息
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntInfoRes {
    /**
     * 企业id
     */
    private String entId;
    /**
     * 企业名称
     */
    private String entName;

}

package chain.fxgj.server.payroll.dto.request;

import lombok.*;

/**
 * 修改手机号
 * */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdPhoneDTO {
    /**
     * wechatId
     */
    private String wechatId;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 身份证号
     */
    private String idNumber;

}

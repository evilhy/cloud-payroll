package chain.fxgj.server.payroll.dto.activity;

import chain.utils.fxgj.constant.DictEnums.IsStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRedisDTO {
    /**
     * openId
     */
    private String openId;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 企业id
     */
    private String entId;
    /**
     * 时间
     */
    private LocalDateTime time;
    /**
     * 过期时间
     */
    private LocalDateTime sessionTimeOut;
    /**
     * 是否登录
     */
    private Integer isLogin = IsStatusEnum.NO.getCode();

    /**
     * 用户机构列表
     */
    private List<String> groupIdList;
}

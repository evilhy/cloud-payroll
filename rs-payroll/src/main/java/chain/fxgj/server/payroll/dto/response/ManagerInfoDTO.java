package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 客户经理信息
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class ManagerInfoDTO {
    /**
     * 客户经理id
     */
    private String id;
    /**
     * 客户经理姓名
     */
    private String managerName;
    /**
     * 客户经理手机号
     */
    private String mobile;
    /**
     * 分行机构号
     */
    private String branchOrgNo;
    /**
     * 分行名称
     */
    private String branchOrgName;
    /**
     * 吸存码
     */
    private String officer;
    /**
     * 支行机构号
     */
    private String subBranchOrgNo;
    /**
     * 支行名称
     */
    private String subBranchOrgName;
    /**
     * 状态
     */
    private String status;
    /**
     * 头像
     */
    private String avatarUrl;
    /**
     * 是否确认
     */
    private String isConfirmed;
    /**
     * 微信号
     */
    private String wechatId;
    /**
     * 二维码链接url
     */
    private String wechatQrUrl;
    /**
     * 微信二维码
     */
    private String wechatQrImgae;
    /**
     * 积分
     */
    private Integer score;

}

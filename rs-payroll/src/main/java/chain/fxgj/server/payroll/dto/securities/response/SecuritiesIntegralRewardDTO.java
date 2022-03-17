package chain.fxgj.server.payroll.dto.securities.response;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 证券活动积分
 *
 * @author zhouhao
 **/
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SecuritiesIntegralRewardDTO {
    /**
     * 客户id
     */
    private String custId;

    /**
     * 用户手机号（被邀请人手机号）
     */
    private String custPhone;

    /**
     * 客户活动参与id
     */
    private String activityId;

    /**
     * 客户经理id
     */
    private String managerId;

    /**
     * 客户经理电话
     */
    private String managerPhone;

    /**
     * 客户经理姓名
     */
    private String managerName;

    /**
     * 达标状态 ()
     */
    private Integer standardStatus;

    /**
     * 达标状态 ()(描述)
     */
    private String standardStatusVal;

    /**
     * 证券公司id
     */
    private String securitiesId;

    /**
     * 证券平台
     */
    private Integer securitiesPlatform;

    /**
     * 证券平台(描述)
     */
    private String securitiesPlatformVal;

    /**
     * 奖励公分
     */
    private int integral;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;

    /**
     * 批次号  yyyyMMddHHmmss
     */
    private String batchNumber;
}

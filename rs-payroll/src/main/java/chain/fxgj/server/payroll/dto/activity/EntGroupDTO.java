package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业机构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntGroupDTO {
    /**
     * id
     */
    private String id;
    /**
     * 机构名称
     */
    private String groupName;
    /**
     * 机构人数
     */
    private long groupCnt;

//    public EntGroupDTO(ActivityGroup activityGroup) {
//        this.id = activityGroup.getGroupId();
//        this.groupName = activityGroup.getGroupName();
//        this.groupCnt = activityGroup.getGroupCnt();
//    }

}

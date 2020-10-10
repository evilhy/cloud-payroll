package chain.fxgj.server.payroll.dto.activity.convert;

import chain.activity.dto.request.activity.ActivityTransLogReq;

import java.util.Optional;

/**
 * @program: cloud-payroll
 * @description: 交易记录转换
 * @author: lius
 * @create: 2019/11/28 15:31
 */
public class ActivityTransLogConvert {

    public static ActivityTransLogReq of(String activityId, String idNumber, String entId) {
        ActivityTransLogReq response = ActivityTransLogReq.builder()
                .activityId(Optional.ofNullable(activityId).orElse(null))
                .idNumber(Optional.ofNullable(idNumber).orElse(null))
                .entId(Optional.ofNullable(entId).orElse(null))
                .build();
        return response;
    }


}

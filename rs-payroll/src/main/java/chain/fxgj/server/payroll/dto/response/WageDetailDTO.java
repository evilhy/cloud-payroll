package chain.fxgj.server.payroll.dto.response;

import chain.fxgj.server.payroll.constant.DictEnums.IsStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 工资信息
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class WageDetailDTO {
    /**
     * 工资条表头
     */
    WageHeadDTO wageHeadDTO;
    /**
     * 工资条展示
     */
    WageShowDTO wageShowDTO;
    /**
     * 工资id
     */
    private String wageDetailId;
    /**
     * 开户行
     */
    private String bankName;
    /**
     * 银行卡号
     */
    private String cardNo;
    /**
     * 方案名称
     */
    private String wageName;
    /**
     * 发放总金额
     */
    private BigDecimal realAmt;
    /**
     * 企业
     */
    private String entName;
    /**
     * 机构
     */
    private String groupName;
    /**
     * 机构id
     */
    private String groupId;
    /**
     * 推送时间
     */
    private Long pushDateTime;
    /**
     * 回馈状态 0确认无误,1有反馈信息,2已沟通 3未确认
     */
    private Integer receiptStautus;
    /**
     * 工资条内容
     */
    private List<Content> content;
    /**
     * 实发金额对比差额
     */
    private BigDecimal differRealAmt;
    /**
     * 支付状态 0未 1已到账 2部分到账
     */
    private String payStatus;

    // 切库注释
//    public WageDetailDTO(WageDetailInfo wageDetailInfo) {
//        this.wageDetailId = wageDetailInfo.getId();
//        this.bankName = wageDetailInfo.getBankName();
//        this.realAmt = wageDetailInfo.getRealTotalAmt();
//        this.groupId = wageDetailInfo.getGroupId();
////        this.pushDateTime = wageDetailInfo.getPayDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        this.receiptStautus = wageDetailInfo.getReceiptsStatus().getCode();
//    }


    /**
     * Res100709
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WageShowDTO {
        /**
         * 是否展示0元金额 0否 1展示
         */
        Integer isShow0;
        /**
         * 是否开启回执功能
         */
        Integer isReceipt;
        /**
         * 默认回执时间(天)
         */
        Integer receiptDay;
        /**
         * 工资发放机构名
         */
        String grantName;
// 切库注释
//        public WageShowDTO(WageShowInfo wageShowInfo) {
//            this.isShow0 = wageShowInfo.getIsShow0() == null ? null : wageShowInfo.getIsShow0().getCode();
//            this.isReceipt = wageShowInfo.getIsReceipt() == null ? IsStatusEnum.YES.getCode() : wageShowInfo.getIsReceipt().getCode();
//            this.receiptDay = wageShowInfo.getReceiptDay();
//            this.grantName=wageShowInfo.getGrantName();
//        }


    }

    /**
     * 工资条内容
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {
        /**
         * 列坐标
         */
        private Integer colNum;
        /**
         * 项目值
         */
        private Object value;

    }


}
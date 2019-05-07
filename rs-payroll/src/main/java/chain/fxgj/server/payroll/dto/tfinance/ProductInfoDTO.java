package chain.fxgj.server.payroll.dto.tfinance;

import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.ProductStatusEnum;
import chain.fxgj.core.jpa.model.BankProductInfo;
import chain.fxgj.core.jpa.model.BankProductMark;
import chain.fxgj.server.payroll.constant.DictEnums.IsStatusEnum;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 理财产品
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfoDTO {
    /**
     * 用户id
     */
    private String wechatId;
    /**
     * 企业id
     */
    private String entId;
    /**
     * 产品id
     */
    private String productId;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 产品图片
     */
    private String productImg;
    /**
     * 产品描述
     */
    private String productDesc;
    /**
     * 产品是否可预约 0否 1是
     */
    @Builder.Default
    private Integer intentFlag = IsStatusEnum.NO.getCode();
    /**
     * 促销说明
     */
    private String promote;
    /**
     * 系统当前时间
     */
    private Long nowDate;
    /**
     * 预约开始时间
     */
    private Long intentStartDate;
    /**
     * 预约结束时间
     */
    private Long intentEndDate;
    /**
     * 认购开始时间
     */
    private Long subscribeStartDate;
    /**
     * 认购结束时间
     */
    private Long subscribeEndDate;
    /**
     * 产品期限
     */
    private Integer productTerm;
    /**
     * 最大人数限制
     */
    private Integer maxLimit;
    /**
     * 起购金额
     */
    private Integer minIntentAmt;
    /**
     * 当前档位
     */
    @Builder.Default
    private Integer nowMark = 1;
    /**
     * 当前预约人数
     */
    @Builder.Default
    private Integer intentNum = 0;
    /**
     * 是否已预约 0否 1是
     */
    @Builder.Default
    private Integer intentStatus = IsStatusEnum.NO.getCode();
    /**
     * 是否已绑定 0否 1是
     */
    @Builder.Default
    private Integer bindStatus = IsStatusEnum.NO.getCode();
    /**
     * 是否已关注公众号 0否 1是
     */
    @Builder.Default
    private Integer followStatus = IsStatusEnum.YES.getCode();
    /**
     * 展示预约内容(0=实时预约 1=企业员工操作)
     */
    @Builder.Default
    private String show = "0";

    private LocalDateTime subscribeEndDate1;
    /**
     * 下期预告图片url
     */
    private String nextImageUrl;

    /**
     * 档位
     */
    private List<ProductMarkDTO> markList;

    public ProductInfoDTO(BankProductInfo bankProductInfo, String imgUrl) {
        this.productId = bankProductInfo.getId();
        this.productName = bankProductInfo.getProductName();
        this.productDesc = bankProductInfo.getProductDesc();
        this.promote = bankProductInfo.getPromote();
        this.intentStartDate = bankProductInfo.getIntentStartDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.intentEndDate = bankProductInfo.getIntentEndDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.subscribeStartDate = bankProductInfo.getSubscribeStartDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.subscribeEndDate = bankProductInfo.getSubscribeEndDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.productTerm = bankProductInfo.getProductTerm();
        this.maxLimit = bankProductInfo.getMaxLimit();
        this.subscribeEndDate1 = bankProductInfo.getSubscribeEndDate();
        this.productImg = bankProductInfo.getProductImages().size() == 0 ? null : imgUrl + bankProductInfo.getProductImages().get(0).getImagePath();
        if (bankProductInfo.getProductStatus().equals(ProductStatusEnum.UP)
                && bankProductInfo.getDelStatusEnum().equals(DelStatusEnum.normal))
            this.intentFlag = IsStatusEnum.YES.getCode();
        this.nextImageUrl = bankProductInfo.getNextImageUrl();
    }

    /**
     * 理财产品档位
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductMarkDTO {
        /**
         * 档位(1=基础档位2=进阶档位3=尊享档位)
         */
        private Integer markLevel;
        /**
         * 档位说明
         */
        private String markRemark;
        /**
         * 档位收益率
         */
        private Double levelRate;
        /**
         * 最小人数
         */
        private Integer minPeople;
        /**
         * 最大人数
         */
        private Integer maxPeople;
        /**
         * 万元收益
         */
        private BigDecimal amt;
        /**
         * 是否达成
         */
        @Builder.Default
        private Integer sucess = IsStatusEnum.NO.getCode();
        /**
         * 是否当前档位 0否 1是
         */
        @Builder.Default
        private Integer nowMark = IsStatusEnum.NO.getCode();

        public ProductMarkDTO(BankProductMark bankProductMark, Integer productTerm) {
            this.markLevel = bankProductMark.getMarkLevel();
            this.markRemark = bankProductMark.getMarkRemark();
            this.levelRate = bankProductMark.getLevelRate();
            this.minPeople = bankProductMark.getMinPeople();
            this.maxPeople = bankProductMark.getMaxPeople();
            //计算收益=金额*利率(0.05)*天数/365
            double amt = 10000 * levelRate * 0.01 * productTerm / 365;
            this.amt = new BigDecimal(amt).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }
}

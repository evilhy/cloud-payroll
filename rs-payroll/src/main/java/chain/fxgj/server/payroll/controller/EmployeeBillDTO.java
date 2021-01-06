package chain.fxgj.server.payroll.controller;

import lombok.*;

import java.util.List;

/**
 * @author syd
 * @description 员工账单
 * @date 2021/1/6.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeBillDTO {
    /**
     * 身份证号
     */
    private String idNumber;
    /**
     * 姓名
     */
    private String employeeName;
    /**
     * 绑定工资条天数
     */
    private Integer differDays;
    /**
     * 推送工资条次数
     */
    private Integer pushTimes;
    /**
     * 代发的月份总数
     */
    private Integer monthCount;
    /**
     * 总代发工资
     */
    private String totalAmount;
    /**
     * 代发工资最高月份金额
     */
    private String maxAmount;
    /**
     * 代发工资最高的月份
     */
    private String maxMonth;
    /**
     * 代发工资最低月份金额
     */
    private String minAmount;
    /**
     * 代发工资最低的月份
     */
    private String minMonth;
    /**
     * 单笔代发金额最高的日期
     */
    private String maxSingleAmountDate;
    /**
     * 单笔代发金额最高的金额
     */
    private String maxSingleAmount;
    /**
     * 差额
     */
    private String differAmount;
    /**
     * 资金类型以及金额
     */
    private List<FundInfo> fundWages;
    /**
     * 行业平均工资
     */
    private String industryAvgAmount;
    /**
     * 一线城市平均薪资
     */
    private String firstTierCitiesAvgAmount;
    /**
     * 二线城市平均薪资
     */
    private String secondTierCitiesAvgAmount;
    /**
     * 击败百分比
     */
    private String percent;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FundInfo {
        /**
         * 资金类型
         */
        private String fundType;
        /**
         * 金额
         */
        private String fundAmount;
        /**
         * 百分比
         */
        private String fundPercent;
    }
}

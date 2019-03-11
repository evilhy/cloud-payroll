package chain.fxgj.server.payroll.dto.response;

import lombok.*;

import java.util.List;

/**
 * 员工企业
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmpEntDTO {
    /**
     * 企业简称
     */
    private String shortEntName;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 基本信息
     */
    private List<Res100708> items;
    /**
     * 银行卡
     */
    private List<BankCard> cards;

    /**
     * 银行卡
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BankCard {
        /**
         * 账号
         */
        private String cardNo;
        /**
         * 修改之前账号
         */
        private String oldCardNo;
        /**
         * 开户行
         */
        private String issuerName;
        /**
         * 银行卡所属机构
         */
        private List<BankCardGroup> bankCardGroups;
        /**
         * 银行卡修改状态
         */
        private Integer cardUpdStatus;
        /**
         * 银行卡修改状态描述
         */
        private String cardUpdStatusVal;
        /**
         * 银行卡修改被拒原因
         */
        private String updDesc;
        /**
         * 银行卡修改是否最新记录（0：已看 1：新，未看）
         */
        private Integer isNew;
    }

    /**
     * 银行卡所属机构
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BankCardGroup {
        /**
         * id
         */
        private String id;
        /**
         * 机构id
         */
        private String groupId;
        /**
         * 机构名称
         */
        private String shortGroupName;
    }

}

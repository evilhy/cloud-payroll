package chain.fxgj.server.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * WageHeadDTO
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WageHeadDTO {
    private Integer headIndex;
    private List<Cell> heads;

    @Builder.Default
    private boolean isDoubleRow = false;

    public WageHeadDTO(Integer headIndex) {
        this.headIndex = headIndex;
    }


    public enum Type {
        UNKNOWN,  //未知
        NAME,     //姓名
        REAL_AMT,  //实发
        SHOULD_AMT,//应发
        DEDUCT_AMT,//扣除
        REMARK,//备注
        BLESS, //祝福
        ID,
        CARD,
        ACCOUNT, //发薪银行账号
        PHONE, //手机号
        ;
    }

    /**
     * 表头
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Cell {
        /**
         * 列名
         */
        private String colName;
        /**
         * 列标
         */
        private List<Integer> colNum;
        /**
         * 类型 未知,姓名,实发,应发,扣除,备注,寄语
         * UNKNOWN,NAME,REAL_AMT,SHOULD_AMT,DEDUCT_AMT,REMARK,BLESS
         */
        private Type type;
        /**
         * 是否显示
         */
        @Builder.Default
        private boolean hidden = false;

        public Cell(String colName, Integer colNum, Type type, Boolean hidden) {
            this.colName = colName;
            this.colNum = new ArrayList<>();
            this.colNum.add(colNum);
            this.type = type;
            this.hidden = hidden;
        }


    }
}

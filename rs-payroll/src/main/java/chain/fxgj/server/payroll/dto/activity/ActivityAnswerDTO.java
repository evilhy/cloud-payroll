package chain.fxgj.server.payroll.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 答题活动
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unchecked")
@EqualsAndHashCode(callSuper = false)
public class ActivityAnswerDTO extends ActivityInfoDTO {
    /**
     * 活动数据
     */
    private List<ActivityAnswerDetail> answerDetails;


    /**
     * 题目
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityAnswerDetail {
        /**
         * id
         */
        private String id;
        /**
         * 问题
         */
        private String question;
        /**
         * 选项组
         */
        private List<Answer> answers;
        /**
         * 正确答案
         */
        private String correctAnswer;

    }

    /**
     * 选项内容
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        /**
         * 序号
         */
        private String num;
        /**
         * 选项
         */
        private String content;
    }

}

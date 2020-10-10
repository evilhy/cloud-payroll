package chain.fxgj.server.payroll.dto.activity;

import chain.activity.dto.response.activity.ActivityInfoAnswerRes;
import chain.activity.dto.response.activity.ActivityInfoRes;
import chain.utils.commons.JacksonUtil;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

/**
 * （答题）活动详情
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unchecked")
public class ActivityAnswerResponseDTO extends ActivityResponseDTO {
    /**
     * 活动数据
     */
    private List<ActivityAnswerDetail> answerDetails;
    /**
     * 是否参与问答活动（ false 否 true 是）
     */

    private Boolean isRandom ;


    public ActivityAnswerResponseDTO(ActivityInfoRes activityInfoRes) {
        super(activityInfoRes);
        answerDetails = new LinkedList<ActivityAnswerDetail>();
    }

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

        public ActivityAnswerDetail(ActivityInfoAnswerRes activityInfoAnswerRes) {
            this.id = activityInfoAnswerRes.getId();
            this.question = activityInfoAnswerRes.getQuestion();
            this.answers = JacksonUtil.jsonToList(activityInfoAnswerRes.getAnswers(), Answer.class);
            this.correctAnswer = activityInfoAnswerRes.getCorrectAnswer();
        }
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

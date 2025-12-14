package com.darauy.quark.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for MCQ validation results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MCQValidationResponse {

    /**
     * Overall success status - true if all questions answered correctly
     */
    private Boolean success;

    /**
     * Total number of questions
     */
    private Integer totalQuestions;

    /**
     * Number of correct answers
     */
    private Integer correctAnswers;

    /**
     * Number of incorrect answers
     */
    private Integer incorrectAnswers;

    /**
     * Total points earned
     */
    private Integer pointsEarned;

    /**
     * Maximum possible points
     */
    private Integer maxPoints;

    /**
     * Percentage score (0-100)
     */
    private Double scorePercentage;

    /**
     * Detailed results for each question
     */
    private List<QuestionResult> questionResults;

    /**
     * Individual question result
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResult {
        /**
         * Question ID
         */
        private Integer questionId;

        /**
         * Question text
         */
        private String question;

        /**
         * Whether the answer was correct
         */
        private Boolean correct;

        /**
         * User's submitted answer
         */
        private String userAnswer;

        /**
         * The correct answer (only shown after submission)
         */
        private String correctAnswer;

        /**
         * Points for this question
         */
        private Integer points;

        /**
         * Points earned (0 if incorrect, points value if correct)
         */
        private Integer pointsEarned;
    }
}

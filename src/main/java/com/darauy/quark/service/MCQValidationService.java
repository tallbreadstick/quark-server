package com.darauy.quark.service;

import com.darauy.quark.dto.response.MCQValidationResponse;
import com.darauy.quark.entity.courses.activity.MCQQuestion;
import com.darauy.quark.entity.courses.activity.Section;
import com.darauy.quark.repository.MCQQuestionRepository;
import com.darauy.quark.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for validating multiple choice question answers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MCQValidationService {

    private final SectionRepository sectionRepository;
    private final MCQQuestionRepository mcqQuestionRepository;

    /**
     * Validate user's answers to MCQ questions
     * 
     * @param sectionId the section containing MCQ questions
     * @param userAnswers map of question ID to user's answer
     * @return validation results with score and detailed feedback
     */
    public MCQValidationResponse validateAnswers(Integer sectionId, Map<Integer, String> userAnswers) {
        try {
            // Get section
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new NoSuchElementException("Section not found"));

            // Get all questions for this section
            List<MCQQuestion> questions = mcqQuestionRepository.findBySectionOrderByIdxAsc(section);

            if (questions.isEmpty()) {
                return MCQValidationResponse.builder()
                        .success(false)
                        .totalQuestions(0)
                        .correctAnswers(0)
                        .incorrectAnswers(0)
                        .pointsEarned(0)
                        .maxPoints(0)
                        .scorePercentage(0.0)
                        .questionResults(new ArrayList<>())
                        .build();
            }

            // Validate each question
            List<MCQValidationResponse.QuestionResult> results = new ArrayList<>();
            int correctCount = 0;
            int incorrectCount = 0;
            int pointsEarned = 0;
            int maxPoints = 0;

            for (MCQQuestion question : questions) {
                String userAnswer = userAnswers.get(question.getId());
                boolean isCorrect = userAnswer != null && 
                                   userAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());

                int questionPoints = question.getPoints();
                maxPoints += questionPoints;

                if (isCorrect) {
                    correctCount++;
                    pointsEarned += questionPoints;
                } else {
                    incorrectCount++;
                }

                MCQValidationResponse.QuestionResult result = MCQValidationResponse.QuestionResult.builder()
                        .questionId(question.getId())
                        .question(question.getQuestion())
                        .correct(isCorrect)
                        .userAnswer(userAnswer)
                        .correctAnswer(question.getCorrectAnswer())
                        .points(questionPoints)
                        .pointsEarned(isCorrect ? questionPoints : 0)
                        .build();

                results.add(result);
            }

            // Calculate percentage
            double percentage = maxPoints > 0 ? (pointsEarned * 100.0 / maxPoints) : 0.0;

            return MCQValidationResponse.builder()
                    .success(correctCount == questions.size())
                    .totalQuestions(questions.size())
                    .correctAnswers(correctCount)
                    .incorrectAnswers(incorrectCount)
                    .pointsEarned(pointsEarned)
                    .maxPoints(maxPoints)
                    .scorePercentage(Math.round(percentage * 100.0) / 100.0) // Round to 2 decimals
                    .questionResults(results)
                    .build();

        } catch (Exception e) {
            log.error("Error validating MCQ answers", e);
            throw new RuntimeException("Validation error: " + e.getMessage(), e);
        }
    }

    /**
     * Get questions for a section (without showing correct answers)
     * Useful for displaying questions to users before submission
     * 
     * @param sectionId the section ID
     * @return list of questions with choices but without correct answers
     */
    public List<MCQQuestion> getQuestions(Integer sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));
        
        return mcqQuestionRepository.findBySectionOrderByIdxAsc(section);
    }
}

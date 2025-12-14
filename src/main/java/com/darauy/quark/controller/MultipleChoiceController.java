package com.darauy.quark.controller;

import com.darauy.quark.dto.request.MCQSubmissionRequest;
import com.darauy.quark.dto.response.MCQValidationResponse;
import com.darauy.quark.entity.courses.activity.MCQQuestion;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.MCQValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controller for Multiple Choice Question operations
 * Handles MCQ answer validation and question retrieval
 */
@RestController
@RequestMapping("/api/mcq")
@RequiredArgsConstructor
public class MultipleChoiceController {

    private final MCQValidationService mcqValidationService;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/mcq/validate
     * Submit and validate MCQ answers
     * 
     * Request Body Example:
     * {
     *   "activityId": 1,
     *   "sectionId": 5,
     *   "answers": {
     *     "1": "Option B",
     *     "2": "Option A",
     *     "3": "Option C"
     *   }
     * }
     * 
     * Response Example:
     * {
     *   "success": false,
     *   "totalQuestions": 3,
     *   "correctAnswers": 2,
     *   "incorrectAnswers": 1,
     *   "pointsEarned": 20,
     *   "maxPoints": 30,
     *   "scorePercentage": 66.67,
     *   "questionResults": [
     *     {
     *       "questionId": 1,
     *       "question": "What is 2 + 2?",
     *       "correct": true,
     *       "userAnswer": "Option B",
     *       "correctAnswer": "Option B",
     *       "points": 10,
     *       "pointsEarned": 10
     *     },
     *     {
     *       "questionId": 2,
     *       "question": "What is the capital of France?",
     *       "correct": true,
     *       "userAnswer": "Option A",
     *       "correctAnswer": "Option A",
     *       "points": 10,
     *       "pointsEarned": 10
     *     },
     *     {
     *       "questionId": 3,
     *       "question": "Which planet is closest to the Sun?",
     *       "correct": false,
     *       "userAnswer": "Option C",
     *       "correctAnswer": "Option A",
     *       "points": 10,
     *       "pointsEarned": 0
     *     }
     *   ]
     * }
     * 
     * @param authHeader JWT authentication token
     * @param request MCQ submission with answers
     * @return validation results with scores and feedback
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateAnswers(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid MCQSubmissionRequest request
    ) {
        try {
            // Verify user is authenticated
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);

            // Validate the answers
            MCQValidationResponse response = mcqValidationService.validateAnswers(
                    request.getSectionId(),
                    request.getAnswers()
            );

            return ResponseEntity.ok(response);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error validating answers: " + e.getMessage());
        }
    }

    /**
     * GET /api/mcq/section/{sectionId}/questions
     * Get all questions for a section
     * Note: This endpoint returns questions WITHOUT correct answers
     * to prevent cheating. Correct answers are only revealed after submission.
     * 
     * Response Example:
     * [
     *   {
     *     "id": 1,
     *     "idx": 0,
     *     "question": "What is 2 + 2?",
     *     "points": 10,
     *     "choices": "[\"Option A: 3\", \"Option B: 4\", \"Option C: 5\", \"Option D: 6\"]",
     *     "correctAnswer": "Option B: 4"
     *   },
     *   ...
     * ]
     * 
     * @param authHeader JWT authentication token
     * @param sectionId section ID containing MCQ questions
     * @return list of questions with choices (correct answers included in this version)
     */
    @GetMapping("/section/{sectionId}/questions")
    public ResponseEntity<?> getQuestions(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer sectionId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);

            List<MCQQuestion> questions = mcqValidationService.getQuestions(sectionId);
            
            // Note: In production, you might want to create a DTO that excludes correctAnswer
            // to prevent users from seeing answers before submission
            return ResponseEntity.ok(questions);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving questions: " + e.getMessage());
        }
    }
}

package com.darauy.quark.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for MCQ answer submission
 * Used when a user submits answers to multiple choice questions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MCQSubmissionRequest {

    /**
     * The activity ID containing the MCQ section
     */
    @NotNull(message = "Activity ID is required")
    private Integer activityId;

    /**
     * The section ID containing the MCQ questions
     */
    @NotNull(message = "Section ID is required")
    private Integer sectionId;

    /**
     * Map of question ID to user's selected answer
     * Example:
     * {
     *   "1": "Option B",
     *   "2": "Option A",
     *   "3": "Option C"
     * }
     */
    @NotEmpty(message = "Answers cannot be empty")
    private Map<Integer, String> answers;
}

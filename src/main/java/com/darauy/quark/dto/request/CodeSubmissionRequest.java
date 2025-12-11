package com.darauy.quark.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for code submission
 * Used when a user submits code to be tested against test cases
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmissionRequest {

    /**
     * The activity ID containing the section with test cases
     */
    @NotNull(message = "Activity ID is required")
    private Integer activityId;

    /**
     * The section ID within the activity (represents a specific coding problem)
     */
    @NotNull(message = "Section ID is required")
    private Integer sectionId;

    /**
     * The user's submitted code solution
     * Example:
     * """
     * class Solution:
     *     def twoSum(self, nums: List[int], target: int) -> List[int]:
     *         seen = {}
     *         for i, num in enumerate(nums):
     *             complement = target - num
     *             if complement in seen:
     *                 return [seen[complement], i]
     *             seen[num] = i
     *         return []
     * """
     */
    @NotBlank(message = "Code cannot be empty")
    private String code;

    /**
     * Programming language (currently supports "python")
     * Future: "javascript", "java", etc.
     */
    private String language = "python";
}

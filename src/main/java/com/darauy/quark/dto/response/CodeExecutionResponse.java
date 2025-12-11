package com.darauy.quark.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for code execution results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeExecutionResponse {

    /**
     * Overall success status - true if all test cases passed
     */
    private Boolean success;

    /**
     * Total number of test cases
     */
    private Integer totalTests;

    /**
     * Number of test cases that passed
     */
    private Integer passedTests;

    /**
     * Number of test cases that failed
     */
    private Integer failedTests;

    /**
     * Detailed results for each test case
     */
    private List<TestCaseResult> testResults;

    /**
     * Error message if compilation/execution failed
     */
    private String error;

    /**
     * Execution time in milliseconds
     */
    private Long executionTimeMs;

    /**
     * Individual test case result
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestCaseResult {
        /**
         * Test case index/number
         */
        private Integer testNumber;

        /**
         * Test case name/description
         */
        private String testName;

        /**
         * Whether this test case passed
         */
        private Boolean passed;

        /**
         * Expected output (human-readable)
         */
        private String expectedOutput;

        /**
         * Actual output from code execution
         */
        private String actualOutput;

        /**
         * Error message if test failed
         */
        private String errorMessage;

        /**
         * Execution time for this specific test in milliseconds
         */
        private Long executionTimeMs;
    }
}

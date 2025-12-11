package com.darauy.quark.controller;

import com.darauy.quark.dto.request.CodeSubmissionRequest;
import com.darauy.quark.dto.response.CodeExecutionResponse;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.CodeExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for code execution and testing
 * Handles submission of code solutions and execution against test cases
 */
@RestController
@RequestMapping("/api/code")
@RequiredArgsConstructor
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/code/submit
     * Submit code for execution and testing
     * 
     * Request Body Example:
     * {
     *   "activityId": 1,
     *   "sectionId": 5,
     *   "language": "python",
     *   "code": "class Solution:\n    def twoSum(self, nums: List[int], target: int) -> List[int]:\n        seen = {}\n        for i, num in enumerate(nums):\n            complement = target - num\n            if complement in seen:\n                return [seen[complement], i]\n            seen[num] = i\n        return []"
     * }
     * 
     * Response Example:
     * {
     *   "success": true,
     *   "totalTests": 3,
     *   "passedTests": 3,
     *   "failedTests": 0,
     *   "executionTimeMs": 245,
     *   "testResults": [
     *     {
     *       "testNumber": 1,
     *       "testName": "Basic case",
     *       "passed": true,
     *       "expectedOutput": "[0, 1]",
     *       "actualOutput": "True",
     *       "errorMessage": null,
     *       "executionTimeMs": 82
     *     },
     *     ...
     *   ]
     * }
     * 
     * @param authHeader JWT authentication token
     * @param request code submission details
     * @return execution results with test outcomes
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitCode(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid CodeSubmissionRequest request
    ) {
        try {
            // Verify user is authenticated
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);

            // Execute the code against test cases
            CodeExecutionResponse response = codeExecutionService.executeCode(
                    request.getSectionId(),
                    request.getCode(),
                    request.getLanguage()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing code: " + e.getMessage());
        }
    }

    /**
     * POST /api/code/run
     * Run code without saving (for testing purposes)
     * Same as /submit but doesn't save progress
     * 
     * @param authHeader JWT authentication token
     * @param request code submission details
     * @return execution results
     */
    @PostMapping("/run")
    public ResponseEntity<?> runCode(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid CodeSubmissionRequest request
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);

            CodeExecutionResponse response = codeExecutionService.executeCode(
                    request.getSectionId(),
                    request.getCode(),
                    request.getLanguage()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error running code: " + e.getMessage());
        }
    }
}

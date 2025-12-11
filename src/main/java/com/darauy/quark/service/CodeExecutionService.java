package com.darauy.quark.service;

import com.darauy.quark.dto.response.CodeExecutionResponse;
import com.darauy.quark.entity.courses.activity.Section;
import com.darauy.quark.entity.courses.activity.TestCase;
import com.darauy.quark.repository.SectionRepository;
import com.darauy.quark.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service for executing and testing submitted code
 * Uses Docker containers for safe sandboxed execution
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeExecutionService {

    private final SectionRepository sectionRepository;
    private final TestCaseRepository testCaseRepository;

    // Timeout for code execution (milliseconds)
    private static final long EXECUTION_TIMEOUT_MS = 10000;

    /**
     * Execute submitted code against test cases
     * 
     * @param sectionId the section containing the coding problem
     * @param submittedCode the user's code solution
     * @param language the programming language (currently "python")
     * @return execution results with test case outcomes
     */
    public CodeExecutionResponse executeCode(Integer sectionId, String submittedCode, String language) {
        long startTime = System.currentTimeMillis();

        try {
            // Get section and test cases
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new NoSuchElementException("Section not found"));

            List<TestCase> testCases = testCaseRepository.findBySectionOrderByIdxAsc(section);

            if (testCases.isEmpty()) {
                return CodeExecutionResponse.builder()
                        .success(false)
                        .error("No test cases found for this section")
                        .build();
            }

            // Execute based on language
            if ("python".equalsIgnoreCase(language)) {
                return executePythonCode(submittedCode, testCases, startTime);
            } else {
                return CodeExecutionResponse.builder()
                        .success(false)
                        .error("Unsupported language: " + language)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error executing code", e);
            return CodeExecutionResponse.builder()
                    .success(false)
                    .error("Execution error: " + e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * Execute Python code in a sandboxed Docker container
     * 
     * @param submittedCode user's code
     * @param testCases list of test cases to run
     * @param startTime execution start time
     * @return execution results
     */
    private CodeExecutionResponse executePythonCode(String submittedCode, List<TestCase> testCases, long startTime) {
        List<CodeExecutionResponse.TestCaseResult> results = new ArrayList<>();
        int passedCount = 0;
        int failedCount = 0;

        // Create temporary directory for code execution
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("code-exec-");
            Path solutionFile = tempDir.resolve("solution.py");

            // Write user's code to solution.py
            Files.writeString(solutionFile, submittedCode);

            // Execute each test case
            for (int i = 0; i < testCases.size(); i++) {
                TestCase testCase = testCases.get(i);
                CodeExecutionResponse.TestCaseResult result = executeTestCase(
                        tempDir,
                        testCase,
                        i + 1
                );

                results.add(result);
                if (result.getPassed()) {
                    passedCount++;
                } else {
                    failedCount++;
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;

            return CodeExecutionResponse.builder()
                    .success(passedCount == testCases.size())
                    .totalTests(testCases.size())
                    .passedTests(passedCount)
                    .failedTests(failedCount)
                    .testResults(results)
                    .executionTimeMs(executionTime)
                    .build();

        } catch (Exception e) {
            log.error("Error executing Python code", e);
            return CodeExecutionResponse.builder()
                    .success(false)
                    .error("Python execution error: " + e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        } finally {
            // Clean up temporary directory
            if (tempDir != null) {
                try {
                    deleteDirectory(tempDir);
                } catch (IOException e) {
                    log.warn("Failed to delete temp directory: " + tempDir, e);
                }
            }
        }
    }

    /**
     * Execute a single test case
     * 
     * @param workDir working directory containing solution.py
     * @param testCase the test case to execute
     * @param testNumber the test case number
     * @return test case result
     */
    private CodeExecutionResponse.TestCaseResult executeTestCase(Path workDir, TestCase testCase, int testNumber) {
        long testStartTime = System.currentTimeMillis();

        try {
            // Create test driver file
            Path driverFile = workDir.resolve("test_driver.py");
            Files.writeString(driverFile, testCase.getDriverCode());

            // Execute using Docker for sandboxing
            // Command: docker run --rm -v <workDir>:/workspace -w /workspace python:3.11-slim python test_driver.py
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "run",
                    "--rm",
                    "--network", "none", // Disable network access
                    "--memory", "256m", // Limit memory
                    "--cpus", "1", // Limit CPU
                    "-v", workDir.toAbsolutePath() + ":/workspace",
                    "-w", "/workspace",
                    "python:3.11-slim",
                    "python", "test_driver.py"
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read output with timeout
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> outputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                } catch (IOException e) {
                    return "Error reading output: " + e.getMessage();
                }
            });

            boolean completed = process.waitFor(
                    testCase.getTimeLimitMs() != null ? testCase.getTimeLimitMs() : EXECUTION_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS
            );

            if (!completed) {
                process.destroyForcibly();
                executor.shutdownNow();
                return CodeExecutionResponse.TestCaseResult.builder()
                        .testNumber(testNumber)
                        .testName(testCase.getName())
                        .passed(false)
                        .expectedOutput(testCase.getExpectedOutput())
                        .actualOutput("")
                        .errorMessage("Timeout: Test exceeded time limit of " + testCase.getTimeLimitMs() + "ms")
                        .executionTimeMs(System.currentTimeMillis() - testStartTime)
                        .build();
            }

            String outputStr = outputFuture.get(1, TimeUnit.SECONDS);
            executor.shutdown();

            int exitCode = process.exitValue();

            // Parse output to determine if test passed
            // The driver code should print "PASS" or "FAIL" or set status variable
            boolean passed = exitCode == 0 && (outputStr.contains("True") || outputStr.contains("PASS"));

            return CodeExecutionResponse.TestCaseResult.builder()
                    .testNumber(testNumber)
                    .testName(testCase.getName())
                    .passed(passed)
                    .expectedOutput(testCase.getExpectedOutput())
                    .actualOutput(outputStr.trim())
                    .errorMessage(passed ? null : "Test failed")
                    .executionTimeMs(System.currentTimeMillis() - testStartTime)
                    .build();

        } catch (Exception e) {
            log.error("Error executing test case {}", testNumber, e);
            return CodeExecutionResponse.TestCaseResult.builder()
                    .testNumber(testNumber)
                    .testName(testCase.getName())
                    .passed(false)
                    .expectedOutput(testCase.getExpectedOutput())
                    .actualOutput("")
                    .errorMessage("Execution error: " + e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - testStartTime)
                    .build();
        }
    }

    /**
     * Recursively delete a directory
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete: " + path, e);
                        }
                    });
        }
    }
}

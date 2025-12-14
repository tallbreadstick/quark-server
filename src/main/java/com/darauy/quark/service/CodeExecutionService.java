package com.darauy.quark.service;

import com.darauy.quark.dto.response.CodeExecutionResponse;
import com.darauy.quark.dto.response.SectionContentResponse;
import com.darauy.quark.entity.courses.activity.Section;
import com.darauy.quark.repository.SectionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Service for executing and testing submitted code.
 * Simplified: single shared code folder, plain python execution (no containers).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeExecutionService {

    private final SectionRepository sectionRepository;
    private final ObjectMapper objectMapper;

    // Timeout for code execution (milliseconds)
    private static final long EXECUTION_TIMEOUT_MS = 10000;

    private static final Path CODE_ROOT = Paths.get("./code").toAbsolutePath().normalize();

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
            // Get section and parse content to extract test cases
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new NoSuchElementException("Section not found"));

            SectionContentResponse sectionContent = objectMapper.readValue(
                    section.getContent(),
                    SectionContentResponse.class
            );

            // Verify this is a CODE section
            if (sectionContent.getSectionType() != SectionContentResponse.SectionType.CODE) {
                return CodeExecutionResponse.builder()
                        .success(false)
                        .error("Section is not a code section")
                        .build();
            }

            List<SectionContentResponse.TestCase> testCases = sectionContent.getCode().getTestCases();

            if (testCases == null || testCases.isEmpty()) {
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
     * Execute Python code using a shared ./code directory.
     * 
     * @param submittedCode user's code
     * @param testCases list of test cases to run
     * @param startTime execution start time
     * @return execution results
     */
    private CodeExecutionResponse executePythonCode(String submittedCode, List<SectionContentResponse.TestCase> testCases, long startTime) {
        List<CodeExecutionResponse.TestCaseResult> results = new ArrayList<>();
        int passedCount = 0;
        int failedCount = 0;

        try {
            ensureCodeRoot();
            Path solutionFile = CODE_ROOT.resolve("solution.py");

            // Write user's code to solution.py
            Files.writeString(solutionFile, submittedCode);

            // Execute each test case
            for (int i = 0; i < testCases.size(); i++) {
                SectionContentResponse.TestCase testCase = testCases.get(i);
                CodeExecutionResponse.TestCaseResult result = executeTestCase(
                        CODE_ROOT,
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
    private CodeExecutionResponse.TestCaseResult executeTestCase(Path workDir, SectionContentResponse.TestCase testCase, int testNumber) {
        long testStartTime = System.currentTimeMillis();

        try {
            // Create test driver file
            Path driverFile = workDir.resolve("test" + testNumber + ".py");
            Files.writeString(driverFile, testCase.getDriver());

            // Clear previous output file
            Path outFile = workDir.resolve("out.txt");
            Files.deleteIfExists(outFile);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", driverFile.getFileName().toString()
            );
            processBuilder.directory(workDir.toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            boolean completed = process.waitFor(
                    EXECUTION_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS
            );

            String outputStr = readProcessOutput(process.getInputStream());

            if (!completed) {
                process.destroyForcibly();
                return CodeExecutionResponse.TestCaseResult.builder()
                        .testNumber(testNumber)
                        .testName(null)
                        .passed(false)
                        .expectedOutput(testCase.getExpected())
                        .actualOutput(outputStr)
                        .errorMessage("Timeout: Test exceeded time limit")
                        .executionTimeMs(System.currentTimeMillis() - testStartTime)
                        .build();
            }

            int exitCode = process.exitValue();

            // Read status from out.txt if present
            String status = Files.exists(outFile) ? Files.readString(outFile).trim() : "";
            boolean passed = parseStatus(status, exitCode);

            return CodeExecutionResponse.TestCaseResult.builder()
                    .testNumber(testNumber)
                    .testName(null)
                    .passed(passed)
                    .expectedOutput(testCase.getExpected())
                    .actualOutput(status.isEmpty() ? outputStr : status)
                    .errorMessage(passed ? null : "Test failed")
                    .executionTimeMs(System.currentTimeMillis() - testStartTime)
                    .build();

        } catch (Exception e) {
            log.error("Error executing test case {}", testNumber, e);
            return CodeExecutionResponse.TestCaseResult.builder()
                    .testNumber(testNumber)
                    .testName(null)
                    .passed(false)
                    .expectedOutput(testCase.getExpected())
                    .actualOutput("")
                    .errorMessage("Execution error: " + e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - testStartTime)
                    .build();
        }
    }

    private void ensureCodeRoot() throws IOException {
        if (!Files.exists(CODE_ROOT)) {
            Files.createDirectories(CODE_ROOT);
        }
    }

    private String readProcessOutput(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        } catch (IOException e) {
            return "Error reading output: " + e.getMessage();
        }
    }

    private boolean parseStatus(String status, int exitCode) {
        if (status == null) status = "";
        String s = status.trim().toLowerCase(Locale.ROOT);
        if (s.equals("true") || s.equals("pass") || s.equals("1")) {
            return true;
        }
        if (s.equals("false") || s.equals("fail") || s.equals("0")) {
            return false;
        }
        // fallback to exit code
        return exitCode == 0;
    }
}

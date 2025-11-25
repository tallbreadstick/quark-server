package com.darauy.quark.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SectionRequest {

    @NotNull(message = "section_type is required")
    private String sectionType; // "mcq" | "code"

    @Valid
    private MCQSection mcq;

    @Valid
    private CodeSection code;

    @Data
    public static class MCQSection {
        @NotEmpty(message = "instructions required")
        private String instructions;

        @NotEmpty(message = "questions required")
        private List<@Valid Question> questions;
    }

    @Data
    public static class Question {
        @NotEmpty(message = "question text required")
        private String question;

        @NotNull(message = "points required")
        private Integer points;

        @NotEmpty(message = "correct choice required")
        private String correct;

        @NotEmpty(message = "choices required")
        private List<String> choices;
    }

    @Data
    public static class CodeSection {
        @NotEmpty(message = "renderer required")
        private String renderer; // "markdown" | "latex"

        @NotEmpty(message = "instructions required")
        private String instructions;

        private String defaultCode;

        private List<String> sources;

        @NotEmpty(message = "test_cases required")
        private List<@Valid TestCase> testCases;
    }

    @Data
    public static class TestCase {
        @NotEmpty(message = "expected required")
        private String expected;

        @NotEmpty(message = "driver required")
        private String driver;

        @NotNull(message = "points required")
        private Integer points;

        @NotNull(message = "hidden required")
        private Boolean hidden;
    }
}

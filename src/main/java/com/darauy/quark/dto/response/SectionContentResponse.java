package com.darauy.quark.dto.response;

import com.darauy.quark.dto.request.SectionRequest;
import lombok.Data;

import java.util.List;

@Data
public class SectionContentResponse {
    private Integer id;
    private SectionType sectionType;
    private MCQSection mcq;
    private CodeSection code;

    public enum SectionType {
        MCQ,
        CODE
    }

    @Data
    public static class MCQSection {
        private String instructions;
        private List<Question> questions;
    }

    @Data
    public static class Question {
        private String question;
        private Integer points;
        private String correct;
        private List<String> choices;
    }

    @Data
    public static class CodeSection {
        private SectionRequest.Renderer renderer;
        private String instructions;
        private String defaultCode;
        private List<String> sources;
        private List<TestCase> testCases;
    }

    public enum Renderer {
        MARKDOWN,
        LATEX
    }

    @Data
    public static class TestCase {
        private String expected;
        private String driver;
        private Integer points;
        private Boolean hidden;
    }
}

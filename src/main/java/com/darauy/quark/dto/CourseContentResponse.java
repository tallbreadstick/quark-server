package com.darauy.quark.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseContentResponse {
    private Integer id;
    private String name;
    private String description;
    private String introduction;
    private Boolean forkable;
    private List<String> tags;
    private List<ChapterResponse> chapters;

    @Data
    @Builder
    public static class ChapterResponse {
        private Integer id;
        private Integer idx;
        private String name;
        private String description;
        private String icon;
    }
}

package com.darauy.quark.dto.response;

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
    private String owner;
    private List<String> tags;
    private List<Chapter> chapters;

    @Data
    @Builder
    public static class Chapter {
        private Integer id;
        private Integer idx;
        private String name;
        private String description;
        private String icon;
    }
}

package com.darauy.quark.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LessonContentResponse {
    private Integer id;
    private Integer idx;
    private String name;
    private String description;
    private String icon;
    private String finishMessage;
    private List<Page> pages;

    @Data
    public static class Page {
        private Integer id;
        private Integer idx;
    }
}

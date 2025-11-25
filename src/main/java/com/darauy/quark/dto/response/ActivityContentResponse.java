package com.darauy.quark.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ActivityContentResponse {
    private Integer id;
    private Integer idx;
    private String name;
    private String description;
    private String icon;
    private Ruleset ruleset;
    private String finishMessage;
    private List<Section> sections;

    @Data
    public static class Section {
        private Integer id;
        private Integer idx;
    }

    @Data
    public static class Ruleset {
        private Boolean enabled;
        private LocalDateTime closeDateTime;
        private Long timeLimit;
    }
}

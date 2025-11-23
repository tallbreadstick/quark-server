package com.darauy.quark.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {
    private String name;
    private String description;
    private String introduction;
    private Boolean forkable;
    private String visibility; // PUBLIC | PRIVATE | UNLISTED
    private List<String> tags; // list of tag names
}

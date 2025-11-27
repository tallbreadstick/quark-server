package com.darauy.quark.dto.response;

import com.darauy.quark.entity.courses.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CourseFilterResponse {
    private Integer id;
    private String name;
    private String description;
    private String introduction;
    private Boolean forkable;
    private String owner;
    private List<Tag> tags;
}

package com.darauy.quark.entity.courses;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTagId implements Serializable {
    private Integer courseId;
    private Integer tagId;
}

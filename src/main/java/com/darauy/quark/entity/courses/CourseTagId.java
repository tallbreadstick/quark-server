package com.darauy.quark.entity.courses;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTagId implements Serializable {

    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "tag_id")
    private Integer tagId;
}

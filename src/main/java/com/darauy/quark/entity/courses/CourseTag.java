package com.darauy.quark.entity.courses;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseTag {

    @EmbeddedId
    private CourseTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    @JsonBackReference
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    @JsonBackReference
    private Tag tag;
}

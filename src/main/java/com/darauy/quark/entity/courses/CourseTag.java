package com.darauy.quark.entity.courses;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "course_tags",
        indexes = {
                @Index(name = "idx_course_tag_course", columnList = "course_id"),
                @Index(name = "idx_course_tag_tag", columnList = "tag_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CourseTagId.class)
public class CourseTag {

    @Id
    @Column(name = "course_id")
    private Integer courseId;

    @Id
    @Column(name = "tag_id")
    private Integer tagId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tag;
}

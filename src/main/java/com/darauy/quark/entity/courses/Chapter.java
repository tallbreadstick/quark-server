package com.darauy.quark.entity.courses;

import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Entity
@Table(
        name = "chapters",
        indexes = {
                @Index(name = "idx_course_id", columnList = "course_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "chapter", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idx ASC")
    @JsonIgnore
    private List<Lesson> lessons;

    @OneToMany(mappedBy = "chapter", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idx ASC")
    @JsonIgnore
    private List<Activity> activities;

    // Helper getters
    public List<Lesson> getLessons() {
        return lessons != null ? lessons : Collections.emptyList();
    }

    public List<Activity> getActivities() {
        return activities != null ? activities : Collections.emptyList();
    }

}

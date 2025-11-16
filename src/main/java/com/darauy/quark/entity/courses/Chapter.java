package com.darauy.quark.entity.courses;

import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "chapters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer number;

    @Column(length = 255)
    private String description; // nullable

    @Column(length = 100)
    private String icon; // nullable

    // ------------------- Relationships -------------------

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference
    private Course course;

    // Future-proofing for Lessons & Activities
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Lesson> lessons;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Activity> activities;
}

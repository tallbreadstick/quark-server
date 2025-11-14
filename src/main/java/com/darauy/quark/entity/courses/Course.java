package com.darauy.quark.entity.courses;

import com.darauy.quark.entity.users.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(length = 255)
    private String description; // nullable

    @Column(columnDefinition = "TEXT")
    private String introduction; // nullable

    @Column(nullable = false)
    private Integer version;

    // ----------- Self-Referential Origin Course -----------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin")
    @JsonBackReference
    private Course origin; // nullable

    @OneToMany(mappedBy = "origin")
    @JsonManagedReference
    private Set<Course> derivedCourses;

    // ----------- Owner (User) -----------
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner", nullable = false)
    private User owner;

    // ----------- Many-to-Many via CourseTag bridge table -----------
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<CourseTag> tags;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

package com.darauy.quark.entity.courses;

import com.darauy.quark.entity.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(
        name = "courses",
        indexes = {
                @Index(name = "idx_course_name", columnList = "name"),
                @Index(name = "idx_course_owner", columnList = "owner_id"),
                @Index(name = "idx_course_created_at", columnList = "created_at"),
                @Index(name = "idx_course_origin", columnList = "origin_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(nullable = false)
    private Integer version;

    // New fields -----------------------------

    @Column(nullable = false)
    private Visibility visibility;
    // "private" | "shared" | "public"

    @Column(nullable = false)
    private Boolean forkable = false;

    // ----------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_id")
    private Course origin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<CourseTag> courseTags;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Chapter> chapters;

    public Integer getOwnerId() {
        return owner != null ? owner.getId() : null;
    }

    public enum Visibility {
        PUBLIC,
        PRIVATE,
        UNLISTED
    }
}

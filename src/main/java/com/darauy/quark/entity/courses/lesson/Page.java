package com.darauy.quark.entity.courses.lesson;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer number;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content; // nullable

    // ---------- Relationship ----------
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    @JsonBackReference
    private Lesson lesson;
}

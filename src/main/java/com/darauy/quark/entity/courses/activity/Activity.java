package com.darauy.quark.entity.courses.activity;

import com.darauy.quark.entity.courses.Chapter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer index;

    @Column(length = 255)
    private String description; // nullable

    @Column(length = 100)
    private String icon; // nullable

    @Column(columnDefinition = "TEXT")
    private String finishMessage; // nullable TEXT

    @Column(nullable = false)
    private Integer version;

    // ---------- Relationship ----------
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    @JsonBackReference
    private Chapter chapter;
}

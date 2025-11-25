package com.darauy.quark.entity.courses.lesson;

import com.darauy.quark.entity.courses.Chapter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "lessons",
        indexes = {
                @Index(name = "idx_chapter_id", columnList = "chapter_id"),
                @Index(name = "idx_chapter_idx", columnList = "chapter_id, idx")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

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

    @Column(columnDefinition = "TEXT")
    private String finishMessage;

    @Column(nullable = false)
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;
}

package com.darauy.quark.entity.courses.activity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a multiple choice question within a section
 */
@Entity
@Table(
        name = "mcq_questions",
        indexes = {
                @Index(name = "idx_section_id_mcq", columnList = "section_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MCQQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The section this question belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    /**
     * Question index/order within the section
     */
    @Column(name = "idx", nullable = false)
    private Integer idx;

    /**
     * The question text
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    /**
     * Points awarded for correct answer
     */
    @Column(nullable = false)
    private Integer points;

    /**
     * The correct answer choice
     */
    @Column(length = 500, nullable = false)
    private String correctAnswer;

    /**
     * All available choices (JSON array stored as text)
     * Example: ["Option A", "Option B", "Option C", "Option D"]
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String choices;
}

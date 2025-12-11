package com.darauy.quark.entity.courses.activity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a test case for a coding problem section
 * Each test case contains driver code and expected output
 */
@Entity
@Table(
        name = "test_cases",
        indexes = {
                @Index(name = "idx_section_id", columnList = "section_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The section this test case belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    /**
     * Test case index/order
     */
    @Column(name = "idx", nullable = false)
    private Integer idx;

    /**
     * Test case name or description
     * Example: "Basic case with positive numbers"
     */
    @Column(length = 255)
    private String name;

    /**
     * Driver code that tests the solution
     * Example:
     * """
     * from solution import Solution
     * driver = Solution()
     * result = driver.twoSum([2, 7, 11, 15], 9)
     * expected = [0, 1]
     * status = result == expected
     * """
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String driverCode;

    /**
     * Expected output in human-readable format
     * Example: "[0, 1]"
     */
    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    /**
     * Whether this is a hidden test case (not shown to students)
     */
    @Column(nullable = false)
    private Boolean hidden = false;

    /**
     * Time limit for test execution in milliseconds
     */
    @Column(name = "time_limit_ms")
    private Integer timeLimitMs = 5000; // Default 5 seconds

    /**
     * Points awarded for passing this test case
     */
    @Column
    private Integer points = 1;
}

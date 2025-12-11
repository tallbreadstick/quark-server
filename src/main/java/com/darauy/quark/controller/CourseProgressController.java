package com.darauy.quark.controller;

import com.darauy.quark.entity.progress.CourseProgress;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.CourseProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/progress/courses")
@RequiredArgsConstructor
public class CourseProgressController {

    private final CourseProgressService courseProgressService;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/progress/courses/{courseId}/enroll
     * Enroll user in a course
     * @param authHeader JWT token
     * @param courseId course id
     * @return success message
     */
    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<?> enrollInCourse(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer courseId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            courseProgressService.enrollUserInCourse(userId, courseId);
            return ResponseEntity.ok("Successfully enrolled in course");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * DELETE /api/progress/courses/{courseId}/unenroll
     * Unenroll user from a course
     * @param authHeader JWT token
     * @param courseId course id
     * @return success message
     */
    @DeleteMapping("/{courseId}/unenroll")
    public ResponseEntity<?> unenrollFromCourse(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer courseId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            courseProgressService.unenrollUserFromCourse(userId, courseId);
            return ResponseEntity.ok("Successfully unenrolled from course");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * GET /api/progress/courses/enrolled
     * Get all enrolled courses for current user
     * @param authHeader JWT token
     * @return list of enrolled courses
     */
    @GetMapping("/enrolled")
    public ResponseEntity<?> getEnrolledCourses(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            List<CourseProgress> enrolledCourses = courseProgressService.getEnrolledCourses(userId);
            return ResponseEntity.ok(enrolledCourses);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * GET /api/progress/courses/{courseId}
     * Get course progress for user
     * @param authHeader JWT token
     * @param courseId course id
     * @return course progress
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseProgress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer courseId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            CourseProgress progress = courseProgressService.getCourseProgress(userId, courseId);
            return ResponseEntity.ok(progress);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * GET /api/progress/courses/{courseId}/enrolled
     * Check if user is enrolled in a course
     * @param authHeader JWT token
     * @param courseId course id
     * @return enrollment status
     */
    @GetMapping("/{courseId}/enrolled")
    public ResponseEntity<?> isEnrolled(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer courseId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            Boolean enrolled = courseProgressService.isEnrolled(userId, courseId);
            return ResponseEntity.ok(enrolled);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

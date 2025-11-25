package com.darauy.quark.controller;

import com.darauy.quark.dto.CourseContentResponse;
import com.darauy.quark.dto.CourseFilterResponse;
import com.darauy.quark.dto.CourseRequest;
import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.repository.UserRepository;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /** CREATE COURSE */
    @PostMapping
    public ResponseEntity<?> createCourse(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid CourseRequest request
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            Course created = courseService.createCourse(user, request);
            return ResponseEntity.ok("Course created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** FORK COURSE */
    @PostMapping("/{courseId}")
    public ResponseEntity<?> forkCourse(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer courseId,
            @RequestBody @Valid CourseRequest request
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            Course forked = courseService.forkCourse(user, courseId, request);
            return ResponseEntity.ok("Course forked successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** EDIT COURSE */
    @PutMapping("/{courseId}")
    public ResponseEntity<?> editCourse(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer courseId,
            @RequestBody @Valid CourseRequest request
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            Course edited = courseService.editCourse(user, courseId, request);
            return ResponseEntity.ok("Course updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** DELETE COURSE */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer courseId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            courseService.deleteCourse(user, courseId);
            return ResponseEntity.ok("Course has been deleted");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** FETCH COURSES BY FILTER */
    @GetMapping
    public ResponseEntity<?> fetchCourses(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Map<String, String> params
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            // Map query params to filter arguments
            Boolean myCourses = params.get("my_courses") != null ? Boolean.valueOf(params.get("my_courses")) : null;
            Boolean sharedWithMe = params.get("shared_with_me") != null ? Boolean.valueOf(params.get("shared_with_me")) : null;
            Boolean forkable = params.get("forkable") != null ? Boolean.valueOf(params.get("forkable")) : null;
            List<String> tags = params.get("tags") != null ?
                    Arrays.stream(params.get("tags").split(",")).map(String::trim).collect(Collectors.toList())
                    : null;
            String sortBy = params.get("sort_by");
            String order = params.get("order");
            String search = params.get("search");

            List<CourseFilterResponse> courses = courseService.fetchCoursesByFilter(user, myCourses, sharedWithMe, forkable, tags, sortBy, order, search);
            return ResponseEntity.ok(courses);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** FETCH COURSE WITH CHAPTERS */
    @GetMapping("/{courseId}")
    public ResponseEntity<?> fetchCourseWithChapters(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer courseId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            CourseContentResponse course = courseService.fetchCourseWithChapters(user, courseId);
            return ResponseEntity.ok(course);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** SHARE COURSE */
    @PostMapping("/share")
    public ResponseEntity<?> shareCourse(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Integer> body
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            Integer courseId = body.get("courseId");
            Integer targetUserId = body.get("userId");

            if (courseId == null || targetUserId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing courseId or userId");
            }

            courseService.shareCourse(user, courseId, targetUserId);
            return ResponseEntity.ok("Course shared successfully");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

package com.darauy.quark.controller;

import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // ----------- CREATE -----------
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        try {
            Course created = courseService.createCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ----------- READ ALL or BY ID -----------
    @GetMapping
    public ResponseEntity<?> getCourses(@RequestParam(required = false) Integer id) {
        try {
            if (id != null) {
                Course course = courseService.getCourseById(id);
                if (course == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
                }
                return ResponseEntity.ok(course);
            } else {
                List<Course> courses = courseService.getAllCourses();
                return ResponseEntity.ok(courses);
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    // ----------- PATCH (UPDATE) -----------
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCourse(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates
    ) {
        try {
            Course updated = courseService.updateCourse(id, updates);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
            }
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    // ----------- DELETE -----------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Integer id) {
        try {
            boolean deleted = courseService.deleteCourse(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
            }
            return ResponseEntity.ok("Course deleted successfully");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }
}

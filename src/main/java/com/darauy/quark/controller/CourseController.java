package com.darauy.quark.controller;

import com.darauy.quark.dto.CourseRequest;
import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * REST controller handling course-related endpoints.
 * 
 * This controller manages the complete course lifecycle:
 * - Create: Create new courses (requires JWT authentication)
 * - Read: Retrieve courses (public for GET requests)
 * - Update: Modify existing courses (requires JWT + ownership)
 * - Delete: Remove courses (requires JWT + ownership)
 * 
 * Authentication Flow:
 * 1. JWT token is validated by JwtInterceptor before reaching controller
 * 2. User ID is extracted from token and set as request attribute
 * 3. Controller retrieves userId from request attribute
 * 4. Service layer uses userId for ownership validation
 * 
 * Ownership Validation:
 * - Course owner is determined from JWT token (not request body)
 * - Users can only update/delete their own courses
 * - Course creation automatically sets owner from JWT token
 */
@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * Creates a new course.
     * 
     * Endpoint: POST /api/course
     * Authentication: Required (JWT token)
     * 
     * Request Flow:
     * 1. JWT token validated by interceptor
     * 2. User ID extracted from token and set in request
     * 3. CourseRequest DTO validated
     * 4. Course created with user as owner
     * 5. Tags and relationships processed
     * 
     * Request Body (CourseRequest):
     * - name: Course name (required)
     * - description: Course description (optional)
     * - introduction: Course introduction text (optional)
     * - originId: ID of origin course for derivation (optional)
     * - tagIds: List of tag IDs to associate (optional)
     * 
     * Note: The version field is managed internally and defaults to 1 on creation.
     * It is automatically incremented on each update (PATCH request).
     * 
     * Response:
     * - 201 CREATED: Created Course entity
     * - 400 BAD_REQUEST: Validation error or missing required fields
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param courseRequest DTO containing course data
     * @param request HttpServletRequest containing userId from JWT token
     * @return ResponseEntity with created Course entity
     */
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CourseRequest courseRequest, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            Course created = courseService.createCourse(courseRequest, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Retrieves all courses.
     * 
     * Endpoint: GET /api/course
     * Authentication: Not required (public endpoint)
     * 
     * Response:
     * - 200 OK: List of all Course entities
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @return ResponseEntity with list of all courses
     */
    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        try {
            List<Course> courses = courseService.getAllCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Retrieves a course by ID.
     * 
     * Endpoint: GET /api/course/{id}
     * Authentication: Not required (public endpoint)
     * 
     * Response:
     * - 200 OK: Course entity
     * - 404 NOT_FOUND: Course not found
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param id Course ID to retrieve
     * @return ResponseEntity with Course entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Integer id) {
        try {
            Course course = courseService.getCourseById(id);
            if (course == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
            }
            return ResponseEntity.ok(course);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Updates a course with partial updates (PATCH operation).
     * 
     * Endpoint: PATCH /api/course/{id}
     * Authentication: Required (JWT token)
     * Authorization: User must be the course owner
     * 
     * Request Flow:
     * 1. JWT token validated by interceptor
     * 2. User ID extracted from token
     * 3. Course retrieved by ID
     * 4. Ownership validated (user must be owner)
     * 5. Partial updates applied
     * 6. Course saved and returned
     * 
     * Request Body (Map<String, Object>):
     * - name: Course name (optional)
     * - description: Course description (optional, can be null)
     * - introduction: Course introduction (optional, can be null)
     * - originId: Origin course ID (optional, can be null)
     * - tagIds: List of tag IDs (optional, replaces existing tags)
     * 
     * Note: The version field is managed internally and is automatically
     * incremented on each update. User-provided version values are ignored.
     * 
     * Response:
     * - 200 OK: Updated Course entity
     * - 400 BAD_REQUEST: Validation error or ownership violation
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 404 NOT_FOUND: Course not found
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param id Course ID to update
     * @param updates Map of field names to new values (partial update)
     * @param request HttpServletRequest containing userId from JWT token
     * @return ResponseEntity with updated Course entity
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCourse(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates,
            HttpServletRequest request
    ) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            Course updated = courseService.updateCourse(id, updates, userId);
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

    /**
     * Deletes a course by ID.
     * 
     * Endpoint: DELETE /api/course/{id}
     * Authentication: Required (JWT token)
     * Authorization: User must be the course owner
     * 
     * Request Flow:
     * 1. JWT token validated by interceptor
     * 2. User ID extracted from token
     * 3. Course retrieved by ID
     * 4. Ownership validated (user must be owner)
     * 5. Course deleted from database
     * 
     * Response:
     * - 200 OK: Success message
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 403 FORBIDDEN: User is not the course owner
     * - 404 NOT_FOUND: Course not found
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param id Course ID to delete
     * @param request HttpServletRequest containing userId from JWT token
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            boolean deleted = courseService.deleteCourse(id, userId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
            }
            return ResponseEntity.ok("Course deleted successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }
}

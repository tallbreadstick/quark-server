package com.darauy.quark.controller;

import com.darauy.quark.dto.ChapterRequest;
import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * REST controller handling chapter-related endpoints.
 * 
 * This controller manages the complete chapter lifecycle:
 * - Create: Create new chapters for a course (requires JWT + course ownership)
 * - Read: Retrieve chapters (requires JWT + course ownership)
 * - Update: Modify existing chapters (requires JWT + course ownership)
 * - Delete: Remove chapters (requires JWT + course ownership)
 * 
 * Authentication Flow:
 * 1. JWT token is validated by JwtInterceptor before reaching controller
 * 2. User ID is extracted from token and set as request attribute
 * 3. Controller retrieves userId from request attribute
 * 4. Service layer validates course ownership using userId
 * 
 * Ownership Validation:
 * - All operations require the user to own the course containing the chapter
 * - Course ownership is verified in the service layer
 * - Users cannot access or modify chapters in courses they don't own
 * 
 * Chapter-Course Relationship:
 * - Chapters are always bound to a course
 * - Course ID is required when creating a chapter
 * - Course ownership determines access to all chapter operations
 */
@RestController
@RequestMapping("/api/chapter")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    /**
     * Creates a new chapter for a course.
     * 
     * Endpoint: POST /api/chapter
     * Authentication: Required (JWT token)
     * Authorization: User must own the course specified in chapterRequest.courseId
     * 
     * Request Flow:
     * 1. JWT token validated by interceptor
     * 2. User ID extracted from token and set in request
     * 3. ChapterRequest DTO validated
     * 4. Course ownership verified
     * 5. Chapter created and associated with course
     * 
     * Request Body (ChapterRequest):
     * - name: Chapter name (required)
     * - number: Chapter number/order (required)
     * - courseId: ID of the course this chapter belongs to (required)
     * - description: Chapter description (optional)
     * - icon: Chapter icon identifier (optional)
     * 
     * Response:
     * - 201 CREATED: Created Chapter entity
     * - 400 BAD_REQUEST: Validation error, course not found, or ownership violation
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param chapterRequest DTO containing chapter data and courseId
     * @param request HttpServletRequest containing userId from JWT token
     * @return ResponseEntity with created Chapter entity
     */
    @PostMapping
    public ResponseEntity<?> createChapter(@RequestBody ChapterRequest chapterRequest, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            Chapter created = chapterService.createChapter(chapterRequest, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Retrieves all chapters for a specific course.
     * 
     * Endpoint: GET /api/chapter?courseId={courseId}
     * Authentication: Required (JWT token)
     * Authorization: User must own the course
     * 
     * Request Flow:
     * 1. JWT token validated by interceptor
     * 2. User ID extracted from token
     * 3. Course ownership verified
     * 4. Chapters for course retrieved and returned
     * 
     * Query Parameters:
     * - courseId: ID of the course to get chapters for (required)
     * 
     * Response:
     * - 200 OK: List of Chapter entities for the course
     * - 400 BAD_REQUEST: Course not found or ownership violation
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param courseId Course ID to filter chapters by
     * @param request HttpServletRequest containing userId from JWT token
     * @return ResponseEntity with list of chapters for the course
     */
    @GetMapping
    public ResponseEntity<?> getAllChapters(@RequestParam Integer courseId, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            List<Chapter> chapters = chapterService.getAllChaptersByCourse(courseId, userId);
            return ResponseEntity.ok(chapters);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Retrieves a chapter by ID.
     * 
     * Endpoint: GET /api/chapter/{id}
     * Authentication: Required (JWT token)
     * Authorization: User must own the course containing this chapter
     * 
     * Request Flow:
     * 1. JWT token validated by interceptor
     * 2. User ID extracted from token
     * 3. Chapter retrieved by ID
     * 4. Course ownership verified
     * 5. Chapter returned if user owns the course
     * 
     * Response:
     * - 200 OK: Chapter entity
     * - 400 BAD_REQUEST: Ownership violation
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 404 NOT_FOUND: Chapter not found
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param id Chapter ID to retrieve
     * @param request HttpServletRequest containing userId from JWT token
     * @return ResponseEntity with Chapter entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getChapterById(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            Chapter chapter = chapterService.getChapterById(id, userId);
            if (chapter == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chapter not found");
            }
            return ResponseEntity.ok(chapter);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Updates a chapter with partial updates (PATCH operation).
     * 
     * Endpoint: PATCH /api/chapter/{id}
     * Authentication: Required (JWT token)
     * Authorization: User must own the course containing this chapter
     * 
     * Request Flow:
     * 1. JWT token validated by interceptor
     * 2. User ID extracted from token
     * 3. Chapter retrieved by ID
     * 4. Course ownership verified
     * 5. Partial updates applied
     * 6. Chapter saved and returned
     * 
     * Request Body (Map<String, Object>):
     * - name: Chapter name (optional)
     * - number: Chapter number (optional)
     * - description: Chapter description (optional, can be null)
     * - icon: Chapter icon (optional, can be null)
     * 
     * Note: courseId cannot be updated - chapters are permanently bound to their course.
     * 
     * Response:
     * - 200 OK: Updated Chapter entity
     * - 400 BAD_REQUEST: Validation error or ownership violation
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 404 NOT_FOUND: Chapter not found
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param id Chapter ID to update
     * @param updates Map of field names to new values (partial update)
     * @param request HttpServletRequest containing userId from JWT token
     * @return ResponseEntity with updated Chapter entity
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateChapter(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates,
            HttpServletRequest request
    ) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            Chapter updated = chapterService.updateChapter(id, updates, userId);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chapter not found");
            }
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Deletes a chapter by ID.
     * 
     * Endpoint: DELETE /api/chapter/{id}
     * Authentication: Required (JWT token)
     * Authorization: User must own the course containing this chapter
     * 
     * Request Flow:
     * 1. JWT token validated by interceptor
     * 2. User ID extracted from token
     * 3. Chapter retrieved by ID
     * 4. Course ownership verified
     * 5. Chapter deleted from database
     * 
     * Response:
     * - 200 OK: Success message
     * - 400 BAD_REQUEST: Ownership violation
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 404 NOT_FOUND: Chapter not found
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param id Chapter ID to delete
     * @param request HttpServletRequest containing userId from JWT token
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChapter(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            boolean deleted = chapterService.deleteChapter(id, userId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chapter not found");
            }
            return ResponseEntity.ok("Chapter deleted successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }
}


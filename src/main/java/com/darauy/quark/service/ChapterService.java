package com.darauy.quark.service;

import com.darauy.quark.dto.ChapterRequest;
import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.repository.ChapterRepository;
import com.darauy.quark.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service managing chapter-related business logic and operations.
 * 
 * This service handles the complete chapter lifecycle:
 * - Chapter creation with course ownership validation
 * - Chapter retrieval (single and list, filtered by course)
 * - Chapter updates with ownership validation
 * - Chapter deletion with ownership validation
 * 
 * Key Features:
 * - All operations require JWT authentication
 * - Course ownership is validated for all operations
 * - Users can only manage chapters in courses they own
 * - Chapters are always associated with a course
 * 
 * Security:
 * - User ID is extracted from JWT token (not from request)
 * - Course ownership is verified before any operation
 * - Ownership check ensures users cannot modify chapters in courses they don't own
 */
@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Creates a new chapter for a course.
     * 
     * Creation Pipeline:
     * 1. Validate required fields (name, number, courseId)
     * 2. Retrieve course by courseId
     * 3. Verify course exists
     * 4. Verify user is the course owner (ownership check)
     * 5. Build Chapter entity with required and optional fields
     * 6. Associate chapter with course
     * 7. Save and return chapter
     * 
     * @param chapterRequest DTO containing chapter data
     * @param userId User ID extracted from JWT token (for ownership validation)
     * @return Created Chapter entity
     * @throws IllegalArgumentException if required fields are missing, course not found, or user is not owner
     */
    @Transactional
    public Chapter createChapter(ChapterRequest chapterRequest, Integer userId) {
        if (chapterRequest.getName() == null || chapterRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Chapter name is required");
        }
        if (chapterRequest.getNumber() == null) {
            throw new IllegalArgumentException("Chapter number is required");
        }
        if (chapterRequest.getCourseId() == null) {
            throw new IllegalArgumentException("Course ID is required");
        }

        // Retrieve and validate course
        Optional<Course> courseOpt = courseRepository.findById(chapterRequest.getCourseId());
        if (courseOpt.isEmpty()) {
            throw new IllegalArgumentException("Course not found");
        }

        Course course = courseOpt.get();

        // Verify user is the course owner
        if (!course.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only create chapters in courses you own");
        }

        // Build chapter entity
        Chapter chapter = Chapter.builder()
                .name(chapterRequest.getName())
                .number(chapterRequest.getNumber())
                .description(chapterRequest.getDescription()) // nullable
                .icon(chapterRequest.getIcon()) // nullable
                .course(course)
                .build();

        return chapterRepository.save(chapter);
    }

    /**
     * Retrieves a chapter by ID.
     * 
     * Security: Verifies that the user owns the course that contains this chapter.
     * 
     * @param id Chapter ID to retrieve
     * @param userId User ID from JWT token (for ownership validation)
     * @return Chapter entity if found and user owns the course, null otherwise
     * @throws IllegalArgumentException if user is not the course owner
     */
    public Chapter getChapterById(Integer id, Integer userId) {
        Optional<Chapter> chapterOpt = chapterRepository.findById(id);
        if (chapterOpt.isEmpty()) {
            return null;
        }

        Chapter chapter = chapterOpt.get();
        Course course = chapter.getCourse();

        // Verify user is the course owner
        if (!course.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only view chapters in courses you own");
        }

        return chapter;
    }

    /**
     * Retrieves all chapters for a specific course.
     * 
     * Security: Verifies that the user owns the course before returning chapters.
     * 
     * @param courseId Course ID to filter chapters by
     * @param userId User ID from JWT token (for ownership validation)
     * @return List of Chapter entities for the course
     * @throws IllegalArgumentException if course not found or user is not the course owner
     */
    public List<Chapter> getAllChaptersByCourse(Integer courseId, Integer userId) {
        // Verify course exists and user owns it
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new IllegalArgumentException("Course not found");
        }

        Course course = courseOpt.get();

        // Verify user is the course owner
        if (!course.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only view chapters in courses you own");
        }

        return chapterRepository.findByCourseId(courseId);
    }

    /**
     * Updates a chapter with partial updates (PATCH operation).
     * 
     * Update Pipeline:
     * 1. Retrieve chapter by ID
     * 2. Validate chapter exists
     * 3. Verify user is the course owner (ownership check)
     * 4. Apply updates from Map (only provided fields are updated)
     * 5. Handle special cases:
     *    - Nullable fields can be set to null
     *    - Course cannot be changed (chapters are bound to courses)
     * 6. Save and return updated chapter
     * 
     * Supported update fields:
     * - name: Chapter name
     * - number: Chapter number/order
     * - description: Chapter description (nullable)
     * - icon: Chapter icon (nullable)
     * 
     * Note: courseId cannot be updated - chapters are permanently bound to their course.
     * 
     * @param id Chapter ID to update
     * @param updates Map of field names to new values (partial update)
     * @param userId User ID from JWT token (for ownership validation)
     * @return Updated Chapter entity
     * @throws IllegalArgumentException if user is not the course owner
     */
    @Transactional
    public Chapter updateChapter(Integer id, Map<String, Object> updates, Integer userId) {
        Optional<Chapter> chapterOpt = chapterRepository.findById(id);
        if (chapterOpt.isEmpty()) {
            return null;
        }

        Chapter chapter = chapterOpt.get();
        Course course = chapter.getCourse();

        // Verify user is the course owner
        if (!course.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update chapters in courses you own");
        }

        // Apply updates
        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    if (value != null) {
                        chapter.setName((String) value);
                    }
                    break;
                case "number":
                    if (value != null) {
                        chapter.setNumber((Integer) value);
                    }
                    break;
                case "description":
                    if (value != null) {
                        chapter.setDescription((String) value);
                    } else {
                        chapter.setDescription(null);
                    }
                    break;
                case "icon":
                    if (value != null) {
                        chapter.setIcon((String) value);
                    } else {
                        chapter.setIcon(null);
                    }
                    break;
                default:
                    // ignore unknown fields and courseId (cannot be changed)
                    break;
            }
        });

        return chapterRepository.save(chapter);
    }

    /**
     * Deletes a chapter by ID.
     * 
     * Deletion Pipeline:
     * 1. Retrieve chapter by ID
     * 2. Validate chapter exists
     * 3. Verify user is the course owner (ownership check)
     * 4. Delete chapter from database
     * 
     * Note: Cascade relationships (lessons, activities) are handled
     * by JPA cascade configuration in the Chapter entity.
     * 
     * @param id Chapter ID to delete
     * @param userId User ID from JWT token (for ownership validation)
     * @return true if chapter was deleted, false if chapter not found
     * @throws IllegalArgumentException if user is not the course owner
     */
    @Transactional
    public boolean deleteChapter(Integer id, Integer userId) {
        Optional<Chapter> chapterOpt = chapterRepository.findById(id);
        if (chapterOpt.isEmpty()) {
            return false;
        }

        Chapter chapter = chapterOpt.get();
        Course course = chapter.getCourse();

        // Verify user is the course owner
        if (!course.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete chapters in courses you own");
        }

        chapterRepository.delete(chapter);
        return true;
    }
}


package com.darauy.quark.service;

import com.darauy.quark.dto.CourseRequest;
import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.entity.courses.CourseTag;
import com.darauy.quark.entity.courses.CourseTagId;
import com.darauy.quark.entity.courses.Tag;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.repository.CourseRepository;
import com.darauy.quark.repository.TagRepository;
import com.darauy.quark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service managing course-related business logic and operations.
 * 
 * This service handles the complete course lifecycle:
 * - Course creation with tags and relationships
 * - Course retrieval (single and list)
 * - Course updates with ownership validation
 * - Course deletion with ownership validation
 * 
 * Key Features:
 * - User ownership is determined from JWT token (not from request body)
 * - Nullable fields (description, introduction, origin, tags) are optional
 * - Tag relationships are managed through CourseTag join entity
 * - Ownership validation ensures users can only modify their own courses
 * 
 * Course-Tag Relationship:
 * - Many-to-Many relationship via CourseTag bridge entity
 * - Tags are linked after course is saved (to get course ID)
 * - Embedded ID (CourseTagId) contains both courseId and tagId
 */
@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    /**
     * Creates a new course with optional tags and relationships.
     * 
     * Creation Pipeline:
     * 1. Validate required fields (name)
     * 2. Retrieve user from database using userId (from JWT token)
     * 3. Build Course entity with required and optional fields
     * 4. Set version to 1 (internal metadata, not user-provided)
     * 5. Handle origin course relationship if originId is provided
     * 6. Save course to get generated ID
     * 7. Create CourseTag relationships if tagIds are provided
     * 8. Save course again with tag relationships
     * 
     * Note: Tags must be linked after course is saved because CourseTagId
     * requires the course ID, which is only available after persistence.
     * 
     * @param courseRequest DTO containing course data (nullable fields are optional)
     * @param userId User ID extracted from JWT token (becomes course owner)
     * @return Created Course entity with all relationships
     * @throws IllegalArgumentException if required fields are missing or user not found
     */
    @Transactional
    public Course createCourse(CourseRequest courseRequest, Integer userId) {
        if (courseRequest.getName() == null || courseRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        // Get owner from JWT userId
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        Course course = Course.builder()
                .name(courseRequest.getName())
                .description(courseRequest.getDescription()) // nullable
                .introduction(courseRequest.getIntroduction()) // nullable
                .version(1) // Internal metadata: always start at version 1
                .owner(owner.get())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Handle origin if provided
        if (courseRequest.getOriginId() != null) {
            Optional<Course> origin = courseRepository.findById(courseRequest.getOriginId());
            origin.ifPresent(course::setOrigin);
        }

        // Save course first to get the ID
        Course saved = courseRepository.save(course);

        // Handle tags if provided (after course is saved so we have the ID)
        if (courseRequest.getTagIds() != null && !courseRequest.getTagIds().isEmpty()) {
            Set<CourseTag> courseTags = new HashSet<>();
            for (Integer tagId : courseRequest.getTagIds()) {
                Optional<Tag> tag = tagRepository.findById(tagId);
                if (tag.isPresent()) {
                    CourseTagId courseTagId = new CourseTagId();
                    courseTagId.setCourseId(saved.getId());
                    courseTagId.setTagId(tagId);
                    
                    CourseTag courseTag = CourseTag.builder()
                            .id(courseTagId)
                            .course(saved)
                            .tag(tag.get())
                            .build();
                    courseTags.add(courseTag);
                }
            }
            saved.setTags(courseTags);
            saved = courseRepository.save(saved);
        }

        return saved;
    }

    /**
     * Retrieves a course by its ID.
     * 
     * Uses a custom query with fetch join to eagerly load the owner relationship,
     * preventing LazyInitializationException during JSON serialization.
     * 
     * @param id Course ID to retrieve
     * @return Course entity if found, null otherwise
     */
    @Transactional(readOnly = true)
    public Course getCourseById(Integer id) {
        return courseRepository.findByIdWithOwner(id).orElse(null);
    }

    /**
     * Retrieves all courses from the database.
     * 
     * Uses a custom query with fetch join to eagerly load the owner relationship,
     * preventing LazyInitializationException during JSON serialization.
     * 
     * @return List of all Course entities with owner loaded
     */
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAllWithOwner();
    }

    /**
     * Updates a course with partial updates (PATCH operation).
     * 
     * Update Pipeline:
     * 1. Retrieve course by ID
     * 2. Validate course exists
     * 3. Verify user is the course owner (ownership check)
     * 4. Apply updates from Map (only provided fields are updated)
     * 5. Handle special cases:
     *    - Nullable fields can be set to null
     *    - Tag updates replace all existing tags
     *    - Origin can be set or cleared
     *    - Version is automatically incremented (internal metadata)
     * 6. Increment version (internal metadata management)
     * 7. Update timestamp
     * 8. Save and return updated course
     * 
     * Supported update fields:
     * - name: Course name
     * - description: Course description (nullable)
     * - introduction: Course introduction text (nullable)
     * - originId: ID of origin course (nullable, for course derivation)
     * - tagIds: List of tag IDs to associate with course (replaces existing tags)
     * 
     * Note: The version field is managed internally and is automatically
     * incremented on each update. User-provided version values are ignored.
     * 
     * @param id Course ID to update
     * @param updates Map of field names to new values (partial update)
     * @param userId User ID from JWT token (for ownership validation)
     * @return Updated Course entity
     * @throws IllegalArgumentException if user is not the course owner
     */
    @Transactional
    public Course updateCourse(Integer id, Map<String, Object> updates, Integer userId) {
        Optional<Course> optional = courseRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }

        Course course = optional.get();

        // Check if user is the owner
        if (!course.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own courses");
        }

        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    if (value != null) {
                        course.setName((String) value);
                    }
                    break;
                case "description":
                    if (value != null) {
                        course.setDescription((String) value);
                    } else {
                        course.setDescription(null);
                    }
                    break;
                case "introduction":
                    if (value != null) {
                        course.setIntroduction((String) value);
                    } else {
                        course.setIntroduction(null);
                    }
                    break;
                // Version is managed internally - automatically incremented on update
                // Do not allow version to be set via PATCH request
                case "version":
                    // Ignore version updates from user - it's managed internally
                    break;
                case "originId":
                    if (value != null) {
                        Integer originId = (Integer) value;
                        courseRepository.findById(originId).ifPresent(course::setOrigin);
                    } else {
                        course.setOrigin(null);
                    }
                    break;
                case "tagIds":
                    if (value != null && value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Integer> tagIds = (List<Integer>) value;
                        Set<CourseTag> courseTags = new HashSet<>();
                        for (Integer tagId : tagIds) {
                            Optional<Tag> tag = tagRepository.findById(tagId);
                            if (tag.isPresent()) {
                                CourseTagId courseTagId = new CourseTagId();
                                courseTagId.setCourseId(course.getId());
                                courseTagId.setTagId(tagId);
                                
                                CourseTag courseTag = CourseTag.builder()
                                        .id(courseTagId)
                                        .course(course)
                                        .tag(tag.get())
                                        .build();
                                courseTags.add(courseTag);
                            }
                        }
                        // Clear existing tags and set new ones
                        if (course.getTags() != null) {
                            course.getTags().clear();
                        }
                        course.setTags(courseTags);
                    }
                    break;
                default:
                    // ignore unknown fields
                    break;
            }
        });

        // Increment version on update (internal metadata management)
        course.setVersion(course.getVersion() + 1);
        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    /**
     * Deletes a course by ID.
     * 
     * Deletion Pipeline:
     * 1. Retrieve course by ID
     * 2. Validate course exists
     * 3. Verify user is the course owner (ownership check)
     * 4. Delete course from database
     * 
     * Note: Cascade relationships (tags, chapters, etc.) are handled
     * by JPA cascade configuration in the Course entity.
     * 
     * @param id Course ID to delete
     * @param userId User ID from JWT token (for ownership validation)
     * @return true if course was deleted, false if course not found
     * @throws IllegalArgumentException if user is not the course owner
     */
    @Transactional
    public boolean deleteCourse(Integer id, Integer userId) {
        Optional<Course> optional = courseRepository.findById(id);
        if (optional.isEmpty()) {
            return false;
        }

        Course course = optional.get();

        // Check if user is the owner
        if (!course.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own courses");
        }

        courseRepository.delete(course);
        return true;
    }
}

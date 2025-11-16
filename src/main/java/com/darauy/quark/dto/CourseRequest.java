package com.darauy.quark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) for course creation requests.
 * 
 * This DTO is used to receive course data from the client when creating
 * a new course. It separates the API contract from the entity model.
 * 
 * Required Fields:
 * - name: Course name (cannot be null or empty)
 * 
 * Optional Fields (nullable):
 * - description: Course description text
 * - introduction: Course introduction/overview text
 * - originId: ID of the course this course is derived from
 * - tagIds: List of tag IDs to associate with the course
 * 
 * Note: 
 * - The course owner (userId) is extracted from the JWT token
 *   in the controller, not from this DTO. This ensures security by
 *   preventing users from creating courses for other users.
 * - The version field is managed internally and defaults to 1 on creation.
 *   It is automatically incremented on each update (PATCH request).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {
    /** Course name (required, max 255 characters) */
    private String name;
    
    /** Course description (optional, nullable, max 255 characters) */
    private String description;
    
    /** Course introduction/overview text (optional, nullable, TEXT type) */
    private String introduction;
    
    /** ID of the origin course if this is a derived course (optional, nullable) */
    private Integer originId;
    
    /** List of tag IDs to associate with the course (optional, nullable) */
    private List<Integer> tagIds;
}


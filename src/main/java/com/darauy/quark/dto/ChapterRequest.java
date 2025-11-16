package com.darauy.quark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for chapter creation and update requests.
 * 
 * This DTO is used to receive chapter data from the client when creating
 * or updating a chapter. It separates the API contract from the entity model.
 * 
 * Required Fields:
 * - name: Chapter name (cannot be null or empty)
 * - number: Chapter number/order (cannot be null)
 * - courseId: ID of the course this chapter belongs to (required for creation)
 * 
 * Optional Fields (nullable):
 * - description: Chapter description text
 * - icon: Chapter icon identifier
 * 
 * Note: The course ownership is validated in the service layer by
 * checking that the user from JWT token owns the course specified by courseId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterRequest {
    /** Chapter name (required, max 255 characters) */
    private String name;
    
    /** Chapter number/order within the course (required) */
    private Integer number;
    
    /** ID of the course this chapter belongs to (required for creation) */
    private Integer courseId;
    
    /** Chapter description (optional, nullable, max 255 characters) */
    private String description;
    
    /** Chapter icon identifier (optional, nullable, max 100 characters) */
    private String icon;
}


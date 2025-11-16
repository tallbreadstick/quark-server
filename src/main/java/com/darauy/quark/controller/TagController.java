package com.darauy.quark.controller;

import com.darauy.quark.entity.courses.Tag;
import com.darauy.quark.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller handling tag-related endpoints.
 * 
 * This controller provides CRUD operations for tag management:
 * - Create: Create new tags (requires JWT authentication)
 * - Read: Retrieve tags (public for GET requests)
 * - Delete: Remove tags (requires JWT authentication)
 * 
 * Tag Usage:
 * Tags are used to categorize courses. They can be:
 * - Fetched publicly for course creation menus
 * - Created/deleted by authenticated users
 * - Associated with multiple courses (many-to-many relationship)
 * 
 * Authentication:
 * - GET endpoints are public (for course creation UI)
 * - POST and DELETE endpoints require JWT authentication
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * Creates a new tag.
     * 
     * Endpoint: POST /api/tags
     * Authentication: Required (JWT token)
     * 
     * Request Body:
     * - name: Tag name (required, must be unique, max 50 characters)
     * 
     * Response:
     * - 201 CREATED: Created Tag entity
     * - 400 BAD_REQUEST: Tag name already exists or validation error
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param tag Tag entity to create
     * @return ResponseEntity with created Tag entity
     */
    @PostMapping
    public ResponseEntity<?> createTag(@RequestBody Tag tag) {
        try {
            Tag created = tagService.createTag(tag);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Retrieves all tags.
     * 
     * Endpoint: GET /api/tags
     * Authentication: Not required (public endpoint)
     * 
     * This endpoint is public to allow course creation menus
     * to fetch available tags without authentication.
     * 
     * Response:
     * - 200 OK: List of all Tag entities
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @return ResponseEntity with list of all tags
     */
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        try {
            List<Tag> tags = tagService.getAllTags();
            return ResponseEntity.ok(tags);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a tag by ID.
     * 
     * Endpoint: GET /api/tags/{id}
     * Authentication: Not required (public endpoint)
     * 
     * Response:
     * - 200 OK: Tag entity
     * - 404 NOT_FOUND: Tag not found
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param id Tag ID to retrieve
     * @return ResponseEntity with Tag entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTagById(@PathVariable Integer id) {
        try {
            Tag tag = tagService.getTagById(id);
            if (tag == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tag not found");
            }
            return ResponseEntity.ok(tag);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Deletes a tag by ID.
     * 
     * Endpoint: DELETE /api/tags/{id}
     * Authentication: Required (JWT token)
     * 
     * Note: If the tag is associated with courses, the relationships
     * will be handled by JPA cascade configuration.
     * 
     * Response:
     * - 200 OK: Success message
     * - 401 UNAUTHORIZED: Missing or invalid JWT token
     * - 404 NOT_FOUND: Tag not found
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param id Tag ID to delete
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Integer id) {
        try {
            boolean deleted = tagService.deleteTag(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tag not found");
            }
            return ResponseEntity.ok("Tag deleted successfully");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }
}


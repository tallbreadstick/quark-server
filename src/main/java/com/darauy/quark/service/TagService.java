package com.darauy.quark.service;

import com.darauy.quark.entity.courses.Tag;
import com.darauy.quark.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service managing tag-related operations.
 * 
 * Tags are used to categorize and organize courses. This service provides
 * basic CRUD operations for tag management.
 * 
 * Tag Usage:
 * - Tags can be associated with multiple courses (many-to-many)
 * - Tags are used in course creation/editing for categorization
 * - Tag names must be unique across the system
 */
@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    /**
     * Creates a new tag.
     * 
     * Validation:
     * - Tag name is required and cannot be empty
     * - Tag name must be unique (checked against existing tags)
     * 
     * @param tag Tag entity to create
     * @return Created Tag entity with generated ID
     * @throws IllegalArgumentException if name is empty or tag already exists
     */
    public Tag createTag(Tag tag) {
        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name is required");
        }

        if (tagRepository.existsByName(tag.getName())) {
            throw new IllegalArgumentException("Tag with this name already exists");
        }

        return tagRepository.save(tag);
    }

    /**
     * Retrieves all tags from the database.
     * 
     * @return List of all Tag entities
     */
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    /**
     * Retrieves a tag by its ID.
     * 
     * @param id Tag ID to retrieve
     * @return Tag entity if found, null otherwise
     */
    public Tag getTagById(Integer id) {
        return tagRepository.findById(id).orElse(null);
    }

    /**
     * Deletes a tag by ID.
     * 
     * Note: If the tag is associated with courses, the relationships
     * will be handled by JPA cascade configuration.
     * 
     * @param id Tag ID to delete
     * @return true if tag was deleted, false if tag not found
     */
    @Transactional
    public boolean deleteTag(Integer id) {
        Optional<Tag> tag = tagRepository.findById(id);
        if (tag.isEmpty()) {
            return false;
        }
        tagRepository.delete(tag.get());
        return true;
    }
}


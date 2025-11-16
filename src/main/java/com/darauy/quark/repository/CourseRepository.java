package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    // All basic CRUD and findAll/findById are inherited from JpaRepository
    
    /**
     * Retrieves all courses with owner eagerly fetched to avoid LazyInitializationException.
     * 
     * This query uses a JOIN FETCH to eagerly load the owner relationship,
     * preventing lazy loading issues during JSON serialization.
     * 
     * @return List of all courses with owner loaded
     */
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.owner")
    List<Course> findAllWithOwner();
    
    /**
     * Retrieves a course by ID with owner eagerly fetched to avoid LazyInitializationException.
     * 
     * This query uses a JOIN FETCH to eagerly load the owner relationship,
     * preventing lazy loading issues during JSON serialization.
     * 
     * @param id Course ID to retrieve
     * @return Course with owner loaded, or empty if not found
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.owner WHERE c.id = :id")
    java.util.Optional<Course> findByIdWithOwner(Integer id);
}

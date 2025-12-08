package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByOwner(User owner);
    @Query("""
    SELECT DISTINCT c
    FROM Course c
    LEFT JOIN FETCH c.owner
    LEFT JOIN FETCH c.courseTags ct
    LEFT JOIN FETCH ct.tag t
    """)
    List<Course> fetchCoursesWithRelations();

    List<Course> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}

package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    // All basic CRUD and findAll/findById are inherited from JpaRepository
}

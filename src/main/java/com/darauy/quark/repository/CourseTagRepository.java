package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.entity.courses.CourseTag;
import com.darauy.quark.entity.courses.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface CourseTagRepository extends JpaRepository<CourseTag, Integer> {
    Set<CourseTag> findByCourse(Course course);
    void deleteByCourse(Course course);
}

package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    List<Chapter> findByCourse(Course course);
}

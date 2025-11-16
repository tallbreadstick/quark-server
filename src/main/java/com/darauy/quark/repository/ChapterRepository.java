package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    List<Chapter> findByCourseId(Integer courseId);
}


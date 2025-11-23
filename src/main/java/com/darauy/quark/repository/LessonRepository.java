package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.lesson.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    List<Lesson> findByChapter(Chapter chapter);

    // Batch method for multiple chapters
    List<Lesson> findByChapterIn(List<Chapter> chapters);
}

package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.entity.courses.lesson.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {
    List<Page> findByLesson(Lesson lesson);

    List<Page> findByLessonOrderByIdxAsc(Lesson lesson);
    // Batch method for multiple lessons
    List<Page> findByLessonIn(List<Lesson> lessons);
}

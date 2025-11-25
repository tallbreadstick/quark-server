package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {
    List<Activity> findByChapter(Chapter chapter);

    int countByChapterId(Integer chapterId);
    List<Activity> findByChapterIdOrderByIdx(Integer chapterId);
    // Batch method for multiple chapters
    List<Activity> findByChapterIn(List<Chapter> chapters);
}

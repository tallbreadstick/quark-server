package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Integer> {

    List<Chapter> findByCourse(Course course);

    // Fetch chapter with lessons only
    @Query("SELECT DISTINCT c FROM Chapter c " +
            "LEFT JOIN FETCH c.lessons " +
            "WHERE c.id = :chapterId")
    Optional<Chapter> findByIdWithLessons(@Param("chapterId") Integer chapterId);

    // Fetch chapter with activities only
    @Query("SELECT DISTINCT c FROM Chapter c " +
            "LEFT JOIN FETCH c.activities " +
            "WHERE c.id = :chapterId")
    Optional<Chapter> findByIdWithActivities(@Param("chapterId") Integer chapterId);
}

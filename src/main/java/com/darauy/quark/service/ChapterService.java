package com.darauy.quark.service;

import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.repository.ChapterRepository;
import com.darauy.quark.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;

    // Add Chapter
    public Chapter addChapter(Integer courseId, Chapter chapter, Integer userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        // Ownership check
        if (!course.getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this course");
        }

        // Determine idx
        int maxIdx = chapterRepository.findByCourse(course).stream()
                .mapToInt(Chapter::getIdx).max().orElse(0);
        chapter.setIdx(maxIdx + 1);
        chapter.setCourse(course);

        return chapterRepository.save(chapter);
    }

    // Reorder Chapters
    @Transactional
    public void reorderChapters(Integer courseId, List<Integer> chapterIds, Integer userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        if (!course.getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this course");
        }

        List<Chapter> chapters = chapterRepository.findByCourse(course);
        Map<Integer, Chapter> chapterMap = new HashMap<>();
        chapters.forEach(c -> chapterMap.put(c.getId(), c));

        for (int i = 0; i < chapterIds.size(); i++) {
            Chapter c = chapterMap.get(chapterIds.get(i));
            if (c == null) throw new NoSuchElementException("Chapter not found");
            c.setIdx(i + 1);
        }
        chapterRepository.saveAll(chapters);
    }

    // Edit Chapter
    public Chapter editChapter(Integer chapterId, Chapter updated, Integer userId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this chapter's course");
        }

        chapter.setName(updated.getName());
        chapter.setDescription(updated.getDescription());
        chapter.setIcon(updated.getIcon());

        return chapterRepository.save(chapter);
    }

    // Delete Chapter
    @Transactional
    public void deleteChapter(Integer chapterId, Integer userId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this chapter's course");
        }

        // delete cascade should be handled by JPA mappings (or manually delete items)
        chapterRepository.delete(chapter);

        // Reorder remaining chapters
        List<Chapter> remaining = chapterRepository.findByCourse(chapter.getCourse());
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setIdx(i + 1);
        }
        chapterRepository.saveAll(remaining);
    }

    // Fetch Chapter with items (lessons + activities)
    public Chapter fetchChapterWithItems(Integer chapterId, Integer userId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this chapter's course");
        }

        // Force load lessons/activities if lazy
        chapter.getLessons().size();
        chapter.getActivities().size();

        return chapter;
    }

    // TODO: Reorder chapter items (activities/lessons) similar to reorderChapters
}

package com.darauy.quark.service;

import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.repository.ChapterRepository;
import com.darauy.quark.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;

    // Add Lesson
    public Lesson addLesson(Integer chapterId, Lesson lesson, Integer userId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this chapter");
        }

        // Determine idx
        int maxIdx = lessonRepository.findByChapter(chapter).stream()
                .mapToInt(Lesson::getIdx).max().orElse(0);
        lesson.setIdx(maxIdx + 1);
        lesson.setChapter(chapter);

        // Set version
        lesson.setVersion(1);

        return lessonRepository.save(lesson);
    }

    // Edit Lesson
    public Lesson editLesson(Integer lessonId, Lesson updated, Integer userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        if (!lesson.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this lesson's chapter/course");
        }

        lesson.setName(updated.getName());
        lesson.setDescription(updated.getDescription());
        lesson.setIcon(updated.getIcon());
        lesson.setFinishMessage(updated.getFinishMessage());
        lesson.setVersion(lesson.getVersion() + 1);

        return lessonRepository.save(lesson);
    }

    // Delete Lesson
    @Transactional
    public void deleteLesson(Integer lessonId, Integer userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        Chapter chapter = lesson.getChapter();
        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this lesson's chapter/course");
        }

        lessonRepository.delete(lesson);

        // Reorder remaining lessons/activities
        List<Lesson> remaining = lessonRepository.findByChapter(chapter);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setIdx(i + 1);
        }
        lessonRepository.saveAll(remaining);
    }

    // Fetch Lesson with pages
    public Lesson fetchLesson(Integer lessonId, Integer userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        if (!lesson.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this lesson's chapter/course");
        }

        // Force load pages if lazy
        lesson.getChapter().getLessons().size(); // optional, depends on JPA mapping

        return lesson;
    }

    // TODO: Reorder lesson pages similar to reorderChapters
}

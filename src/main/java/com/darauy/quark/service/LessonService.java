package com.darauy.quark.service;

import com.darauy.quark.dto.LessonContentResponse;
import com.darauy.quark.dto.LessonContentResponse.Page;
import com.darauy.quark.dto.LessonRequest;
import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.repository.ChapterRepository;
import com.darauy.quark.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;

    // ------------------ Add Lesson ------------------
    public Lesson addLesson(Integer chapterId, LessonRequest request, Integer userId) {
        validateLessonRequest(request);

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this chapter");
        }

        int maxIdx = lessonRepository.findByChapter(chapter).stream()
                .mapToInt(Lesson::getIdx).max().orElse(0);

        Lesson lesson = Lesson.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .finishMessage(request.getFinishMessage())
                .idx(maxIdx + 1)
                .version(1)
                .chapter(chapter)
                .build();

        return lessonRepository.save(lesson);
    }

    // ------------------ Edit Lesson ------------------
    public Lesson editLesson(Integer lessonId, LessonRequest request, Integer userId) {
        validateLessonRequest(request);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        if (!lesson.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this lesson's chapter/course");
        }

        lesson.setName(request.getName());
        lesson.setDescription(request.getDescription());
        lesson.setIcon(request.getIcon());
        lesson.setFinishMessage(request.getFinishMessage());
        lesson.setVersion(lesson.getVersion() + 1);

        return lessonRepository.save(lesson);
    }

    // ------------------ Delete Lesson ------------------
    @Transactional
    public void deleteLesson(Integer lessonId, Integer userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        Chapter chapter = lesson.getChapter();
        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this lesson's chapter/course");
        }

        lessonRepository.delete(lesson);

        // Reorder remaining lessons
        List<Lesson> remaining = lessonRepository.findByChapter(chapter);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setIdx(i + 1);
        }
        lessonRepository.saveAll(remaining);
    }

    // ------------------ Fetch Lesson ------------------
    public LessonContentResponse fetchLesson(Integer lessonId, Integer userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        if (!lesson.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this lesson's chapter/course");
        }

        // Map Lesson -> LessonContentResponse
        List<Page> pages = lesson.getPages() != null ? // assuming Lesson has getPages()
                lesson.getPages().stream()
                        .sorted(Comparator.comparingInt(com.darauy.quark.entity.courses.lesson.Page::getIdx))
                        .map(p -> {
                            Page page = new Page();
                            page.setId(p.getId());
                            page.setIdx(p.getIdx());
                            return page;
                        })
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return LessonContentResponse.builder()
                .id(lesson.getId())
                .idx(lesson.getIdx())
                .name(lesson.getName())
                .description(lesson.getDescription())
                .icon(lesson.getIcon())
                .finishMessage(lesson.getFinishMessage())
                .pages(pages)
                .build();
    }

    // ------------------ Validation ------------------
    private void validateLessonRequest(LessonRequest request) {
        if (request.getName() == null || request.getName().length() < 10 || request.getName().length() > 255) {
            throw new IllegalArgumentException("Lesson name must be between 10 and 255 characters");
        }
        if (request.getDescription() != null && request.getDescription().length() > 255) {
            throw new IllegalArgumentException("Lesson description cannot exceed 255 characters");
        }
    }
}

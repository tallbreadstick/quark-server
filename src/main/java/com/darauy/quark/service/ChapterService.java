package com.darauy.quark.service;

import com.darauy.quark.dto.response.ChapterContentResponse;
import com.darauy.quark.dto.request.ChapterRequest;
import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.repository.ChapterRepository;
import com.darauy.quark.repository.CourseRepository;
import com.darauy.quark.repository.CourseProgressRepository;
import com.darauy.quark.repository.CourseSharedRepository;
import com.darauy.quark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final CourseSharedRepository courseSharedRepository;
    private final UserRepository userRepository;

    // Add Chapter using ChapterRequest instead of raw Chapter
    public Chapter addChapter(Integer courseId, ChapterRequest request, Integer userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        // Ownership check
        if (!course.getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this course");
        }

        // Determine idx
        int maxIdx = chapterRepository.findByCourse(course).stream()
                .mapToInt(Chapter::getIdx).max().orElse(0);

        Chapter chapter = Chapter.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .idx(maxIdx + 1)
                .course(course)
                .build();

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
    public Chapter editChapter(Integer chapterId, ChapterRequest updated, Integer userId) {
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
    public ChapterContentResponse fetchChapterWithItems(Integer chapterId, Integer userId) {

        // Fetch lessons first
        Chapter chapter = chapterRepository.findByIdWithLessons(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        // Fetch activities separately to avoid multiple collection fetch issue
        Chapter chapterWithActivities = chapterRepository.findByIdWithActivities(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        // Copy activities into the chapter
        chapter.setActivities(chapterWithActivities.getActivities());

        // Check ownership or enrollment
        if (!hasAccessToCourse(chapter.getCourse(), userId)) {
            throw new SecurityException("User does not have access to this chapter's course");
        }

        // Map to DTO (same as before)
        List<ChapterContentResponse.ChapterItem> items = new ArrayList<>();

        chapter.getLessons().stream()
                .sorted(Comparator.comparingInt(a -> a.getIdx()))
                .forEach(lesson -> items.add(
                        ChapterContentResponse.Lesson.lessonBuilder()
                                .id(lesson.getId())
                                .idx(lesson.getIdx())
                                .name(lesson.getName())
                                .description(lesson.getDescription())
                                .icon(lesson.getIcon())
                                .finishMessage(lesson.getFinishMessage())
                                .build()
                ));

        chapter.getActivities().stream()
                .sorted(Comparator.comparingInt(a -> a.getIdx()))
                .forEach(activity -> items.add(
                        ChapterContentResponse.Activity.activityBuilder()
                                .id(activity.getId())
                                .idx(activity.getIdx())
                                .name(activity.getName())
                                .description(activity.getDescription())
                                .icon(activity.getIcon())
                                .finishMessage(activity.getFinishMessage())
                                .ruleset(activity.getRuleset())
                                .build()
                ));

        // Sort combined list by idx
        items.sort(Comparator.comparingInt(ChapterContentResponse.ChapterItem::getIdx));

        return ChapterContentResponse.builder()
                .id(chapter.getId())
                .idx(chapter.getIdx())
                .name(chapter.getName())
                .description(chapter.getDescription())
                .icon(chapter.getIcon())
                .items(items)
                .build();
    }

    // TODO: Reorder chapter items (activities/lessons) similar to reorderChapters

    private boolean hasAccessToCourse(Course course, Integer userId) {
        boolean isOwner = course.getOwnerId().equals(userId);
        if (isOwner) return true;
        
        var user = userRepository.findById(userId);
        if (user.isEmpty()) return false;
        
        boolean isEnrolled = courseProgressRepository.findByUserAndCourse(user.get(), course).isPresent();
        return isEnrolled;
    }
}

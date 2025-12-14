package com.darauy.quark.service;

import com.darauy.quark.dto.request.PageRequest;
import com.darauy.quark.dto.response.PageContentResponse;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.entity.courses.lesson.Page;
import com.darauy.quark.repository.LessonRepository;
import com.darauy.quark.repository.PageRepository;
import com.darauy.quark.repository.CourseProgressRepository;
import com.darauy.quark.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final LessonRepository lessonRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final UserRepository userRepository;

    /** CREATE PAGE */
    public Page addPage(Integer lessonId, PageRequest req, Integer userId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        if (!lesson.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("Unauthorized");
        }

        int nextIdx = pageRepository.findByLesson(lesson).size();

        Page page = Page.builder()
                .idx(nextIdx)
                .renderer(req.getRenderer())
                .content(req.getContent())
                .lesson(lesson)
                .build();

        return pageRepository.save(page);
    }

    /** EDIT PAGE */
    public Page editPage(Integer pageId, PageRequest req, Integer userId) {

        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new NoSuchElementException("Page not found"));

        if (!page.getLesson().getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("Unauthorized");
        }

        page.setRenderer(req.getRenderer());
        page.setContent(req.getContent());

        return pageRepository.save(page);
    }

    /** DELETE PAGE */
    public void deletePage(Integer pageId, Integer userId) {

        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new NoSuchElementException("Page not found"));

        if (!page.getLesson().getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("Unauthorized");
        }

        Lesson lesson = page.getLesson();
        pageRepository.delete(page);

        // Reorder all pages cleanly
        List<Page> pages = pageRepository.findByLessonOrderByIdxAsc(lesson);
        for (int i = 0; i < pages.size(); i++) {
            pages.get(i).setIdx(i);
        }
        pageRepository.saveAll(pages);
    }

    /** FETCH PAGE CONTENT */
    public PageContentResponse fetchPage(Integer pageId, Integer userId) {

        Page page = pageRepository.findById(pageId)
            .orElseThrow(() -> new NoSuchElementException("Page not found"));

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!canAccessCourse(page.getLesson().getChapter().getCourse(), user)) {
            throw new SecurityException("Unauthorized");
        }

        return PageContentResponse.builder()
            .renderer(page.getRenderer())
            .content(page.getContent())
            .build();
    }

    /** REORDER PAGES */
    @Transactional
    public void reorderPages(Integer lessonId, List<Integer> pageIds, Integer userId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        List<Page> pages = pageRepository.findByLesson(lesson);

        if (pageIds.size() != pages.size()) {
            throw new IllegalArgumentException("Page count mismatch");
        }

        // Map each provided ID to its new idx
        for (int i = 0; i < pageIds.size(); i++) {
            Integer targetId = pageIds.get(i);

            Page p = pages.stream()
                    .filter(pg -> pg.getId().equals(targetId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Page not found: " + targetId));

            if (!p.getLesson().getChapter().getCourse().getOwnerId().equals(userId)) {
                throw new SecurityException("Unauthorized");
            }

            p.setIdx(i);
        }

        pageRepository.saveAll(pages);
    }

    private boolean canAccessCourse(com.darauy.quark.entity.courses.Course course, com.darauy.quark.entity.users.User user) {
        if (user.getUserType() == com.darauy.quark.entity.users.User.UserType.EDUCATOR) {
            return course.getOwnerId().equals(user.getId());
        }

        if (user.getUserType() == com.darauy.quark.entity.users.User.UserType.STUDENT) {
            return courseProgressRepository.findByUserAndCourse(user, course).isPresent();
        }

        return false;
    }
}

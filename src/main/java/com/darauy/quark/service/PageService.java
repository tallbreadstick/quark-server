package com.darauy.quark.service;

import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.entity.courses.lesson.Page;
import com.darauy.quark.repository.LessonRepository;
import com.darauy.quark.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final LessonRepository lessonRepository;

    /** CREATE PAGE */
    public Page addPage(Integer lessonId, Page page, Integer userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        // TODO: check that lesson.chapter.course.user.id == userId for ownership

        int nextIdx = pageRepository.findByLesson(lesson).size();
        page.setIdx(nextIdx);
        page.setLesson(lesson);
        return pageRepository.save(page);
    }

    /** EDIT PAGE */
    public Page editPage(Integer pageId, Page updated, Integer userId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new NoSuchElementException("Page not found"));

        // TODO: check ownership via page.lesson.chapter.course.user.id == userId

        page.setContent(updated.getContent());
        return pageRepository.save(page);
    }

    /** DELETE PAGE */
    public void deletePage(Integer pageId, Integer userId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new NoSuchElementException("Page not found"));

        // TODO: check ownership via page.lesson.chapter.course.user.id == userId

        Lesson lesson = page.getLesson();
        pageRepository.delete(page);

        // Reorder remaining pages
        List<Page> pages = pageRepository.findByLesson(lesson);
        for (int i = 0; i < pages.size(); i++) {
            pages.get(i).setIdx(i);
        }
        pageRepository.saveAll(pages);
    }

    /** FETCH PAGE */
    public Page fetchPage(Integer pageId, Integer userId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new NoSuchElementException("Page not found"));

        // TODO: check ownership via page.lesson.chapter.course.user.id == userId

        return page;
    }

    /** REORDER PAGES */
    public void reorderPages(Integer lessonId, List<Integer> pageIds, Integer userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        // TODO: check ownership via lesson.chapter.course.user.id == userId

        List<Page> pages = pageRepository.findByLesson(lesson);
        if (pageIds.size() != pages.size()) {
            throw new IllegalArgumentException("Page count mismatch");
        }

        for (int i = 0; i < pageIds.size(); i++) {
            int finalI = i;
            int finalI1 = i;
            Page page = pages.stream()
                    .filter(p -> p.getId().equals(pageIds.get(finalI)))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Page not found: " + pageIds.get(finalI1)));
            page.setIdx(i);
        }
        pageRepository.saveAll(pages);
    }
}

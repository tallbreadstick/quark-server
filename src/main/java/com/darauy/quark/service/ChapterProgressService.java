package com.darauy.quark.service;

import com.darauy.quark.entity.progress.ChapterProgress;
import com.darauy.quark.entity.progress.ChapterProgressId;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.repository.ChapterProgressRepository;
import com.darauy.quark.repository.UserRepository;
import com.darauy.quark.repository.ChapterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChapterProgressService {

    private final ChapterProgressRepository chapterProgressRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;

    /**
     * Initialize or get chapter progress for a user (cache for cumulative activity progress)
     * @param userId the user id
     * @param chapterId the chapter id
     * @return the ChapterProgress
     */
    @Transactional
    public ChapterProgress initializeChapterProgress(Integer userId, Integer chapterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        return chapterProgressRepository.findByUserAndChapter(user, chapter)
                .orElseGet(() -> {
                    ChapterProgress progress = new ChapterProgress(user, chapter);
                    return chapterProgressRepository.save(progress);
                });
    }

    /**
     * Get chapter progress for a user
     * @param userId the user id
     * @param chapterId the chapter id
     * @return the ChapterProgress
     */
    public ChapterProgress getChapterProgress(Integer userId, Integer chapterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        return chapterProgressRepository.findByUserAndChapter(user, chapter)
                .orElseThrow(() -> new NoSuchElementException("Chapter progress not found"));
    }

    /**
     * Get all chapter progress for a user
     * @param userId the user id
     * @return list of ChapterProgress entries
     */
    public List<ChapterProgress> getUserChapterProgress(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return chapterProgressRepository.findByUser(user);
    }

    /**
     * Delete chapter progress for a user
     * @param userId the user id
     * @param chapterId the chapter id
     */
    @Transactional
    public void deleteChapterProgress(Integer userId, Integer chapterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        ChapterProgress progress = chapterProgressRepository.findByUserAndChapter(user, chapter)
                .orElseThrow(() -> new NoSuchElementException("Chapter progress not found"));

        chapterProgressRepository.delete(progress);
    }
}

package com.darauy.quark.controller;

import com.darauy.quark.entity.progress.ChapterProgress;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.ChapterProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/progress/chapters")
@RequiredArgsConstructor
public class ChapterProgressController {

    private final ChapterProgressService chapterProgressService;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/progress/chapters/{chapterId}/initialize
     * Initialize or get chapter progress (cache for cumulative activity progress)
     * @param authHeader JWT token
     * @param chapterId chapter id
     * @return chapter progress
     */
    @PostMapping("/{chapterId}/initialize")
    public ResponseEntity<?> initializeChapterProgress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer chapterId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            ChapterProgress progress = chapterProgressService.initializeChapterProgress(userId, chapterId);
            return ResponseEntity.ok(progress);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * GET /api/progress/chapters/{chapterId}
     * Get chapter progress for user
     * @param authHeader JWT token
     * @param chapterId chapter id
     * @return chapter progress
     */
    @GetMapping("/{chapterId}")
    public ResponseEntity<?> getChapterProgress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer chapterId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            ChapterProgress progress = chapterProgressService.getChapterProgress(userId, chapterId);
            return ResponseEntity.ok(progress);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * GET /api/progress/chapters
     * Get all chapter progress for user
     * @param authHeader JWT token
     * @return list of chapter progress
     */
    @GetMapping
    public ResponseEntity<?> getUserChapterProgress(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            List<ChapterProgress> progress = chapterProgressService.getUserChapterProgress(userId);
            return ResponseEntity.ok(progress);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * DELETE /api/progress/chapters/{chapterId}
     * Delete chapter progress for user
     * @param authHeader JWT token
     * @param chapterId chapter id
     * @return success message
     */
    @DeleteMapping("/{chapterId}")
    public ResponseEntity<?> deleteChapterProgress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer chapterId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            chapterProgressService.deleteChapterProgress(userId, chapterId);
            return ResponseEntity.ok("Chapter progress deleted");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

package com.darauy.quark.controller;

import com.darauy.quark.dto.ChapterContentResponse;
import com.darauy.quark.dto.ChapterRequest;
import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;
    private final JwtUtil jwtUtil;

    // ------------------ Add Chapter ------------------
    @PostMapping("/course/{courseId}/chapter")
    public ResponseEntity<?> addChapter(
            @PathVariable Integer courseId,
            @RequestBody ChapterRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            Chapter saved = chapterService.addChapter(courseId, request, userId);
            return ResponseEntity.ok("Chapter added to course");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Reorder Chapters ------------------
    @PatchMapping("/course/{courseId}")
    public ResponseEntity<?> reorderChapters(
            @PathVariable Integer courseId,
            @RequestBody List<Integer> chapterIds,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            chapterService.reorderChapters(courseId, chapterIds, userId);
            return ResponseEntity.ok("Chapter indexing reordered");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Edit Chapter ------------------
    @PutMapping("/chapter/{chapterId}")
    public ResponseEntity<?> editChapter(
            @PathVariable Integer chapterId,
            @RequestBody ChapterRequest updated,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            Chapter saved = chapterService.editChapter(chapterId, updated, userId);
            return ResponseEntity.ok("Chapter edited successfully");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Delete Chapter ------------------
    @DeleteMapping("/chapter/{chapterId}")
    public ResponseEntity<?> deleteChapter(
            @PathVariable Integer chapterId,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            chapterService.deleteChapter(chapterId, userId);
            return ResponseEntity.ok("Chapter removed from course");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Fetch Chapter With Items ------------------
    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<?> fetchChapterWithItems(
            @PathVariable Integer chapterId,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            ChapterContentResponse chapter = chapterService.fetchChapterWithItems(chapterId, userId);
            return ResponseEntity.ok(chapter);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}

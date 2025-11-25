package com.darauy.quark.controller;

import com.darauy.quark.dto.LessonContentResponse;
import com.darauy.quark.dto.LessonRequest;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final JwtUtil jwtUtil;

    // ------------------ Add Lesson ------------------
    @PostMapping("/chapter/{chapterId}/lesson")
    public ResponseEntity<?> addLesson(
            @PathVariable Integer chapterId,
            @RequestBody LessonRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = extractUserIdFromToken(authorization);
            Lesson saved = lessonService.addLesson(chapterId, request, userId);
            return ResponseEntity.ok("Lesson added to chapter");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Edit Lesson ------------------
    @PutMapping("/lesson/{lessonId}")
    public ResponseEntity<?> editLesson(
            @PathVariable Integer lessonId,
            @RequestBody LessonRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = extractUserIdFromToken(authorization);
            Lesson saved = lessonService.editLesson(lessonId, request, userId);
            return ResponseEntity.ok("Lesson updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Delete Lesson ------------------
    @DeleteMapping("/lesson/{lessonId}")
    public ResponseEntity<?> deleteLesson(
            @PathVariable Integer lessonId,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = extractUserIdFromToken(authorization);
            lessonService.deleteLesson(lessonId, userId);
            return ResponseEntity.ok("Lesson removed from chapter");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Fetch Lesson ------------------
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<?> fetchLesson(
            @PathVariable Integer lessonId,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = extractUserIdFromToken(authorization);
            LessonContentResponse lesson = lessonService.fetchLesson(lessonId, userId);
            return ResponseEntity.ok(lesson);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Helper ------------------
    private Integer extractUserIdFromToken(String authorization) {
        return jwtUtil.extractUserIdFromHeader(authorization);
    }
}

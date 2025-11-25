package com.darauy.quark.controller;

import com.darauy.quark.dto.request.PageRequest;
import com.darauy.quark.dto.response.PageContentResponse;
import com.darauy.quark.entity.courses.lesson.Page;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.PageService;
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
public class PageController {

    private final PageService pageService;
    private final JwtUtil jwtUtil;

    // --------------------------------------------------------
    // CREATE PAGE
    // --------------------------------------------------------
    @PostMapping("/lesson/{lessonId}/page")
    public ResponseEntity<?> addPage(
            @PathVariable Integer lessonId,
            @RequestBody PageRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            Page saved = pageService.addPage(lessonId, request, userId);
            return ResponseEntity.ok("Page added to lesson");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------------------------------------------
    // EDIT PAGE
    // --------------------------------------------------------
    @PutMapping("/page/{pageId}")
    public ResponseEntity<?> editPage(
            @PathVariable Integer pageId,
            @RequestBody PageRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            pageService.editPage(pageId, request, userId);
            return ResponseEntity.ok("Page updated successfully");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------------------------------------------
    // DELETE PAGE
    // --------------------------------------------------------
    @DeleteMapping("/page/{pageId}")
    public ResponseEntity<?> deletePage(
            @PathVariable Integer pageId,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            pageService.deletePage(pageId, userId);
            return ResponseEntity.ok("Page removed from lesson");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------------------------------------------
    // REORDER PAGES
    // --------------------------------------------------------
    @PatchMapping("/lesson/{lessonId}")
    public ResponseEntity<?> reorderPages(
            @PathVariable Integer lessonId,
            @RequestBody List<Integer> orderedPageIds,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            pageService.reorderPages(lessonId, orderedPageIds, userId);
            return ResponseEntity.ok("Page indexing reordered");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------------------------------------------
    // FETCH PAGE CONTENT
    // --------------------------------------------------------
    @GetMapping("/page/{pageId}")
    public ResponseEntity<?> fetchPage(
            @PathVariable Integer pageId,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authorization);
            PageContentResponse response = pageService.fetchPage(pageId, userId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}

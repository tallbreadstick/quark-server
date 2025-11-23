package com.darauy.quark.controller;

import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final JwtUtil jwtUtil;

    // ------------------ Add Activity ------------------
    @PostMapping("/chapter/{chapterId}/activity")
    public ResponseEntity<?> addActivity(
            @PathVariable Integer chapterId,
            @RequestBody Activity activity,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = extractUserIdFromToken(authorization);
            Activity saved = activityService.addActivity(chapterId, activity, userId);
            return ResponseEntity.ok(saved);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Edit Activity ------------------
    @PatchMapping("/activity/{activityId}")
    public ResponseEntity<?> editActivity(
            @PathVariable Integer activityId,
            @RequestBody Activity updated,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = extractUserIdFromToken(authorization);
            Activity saved = activityService.editActivity(activityId, updated, userId);
            return ResponseEntity.ok(saved);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Delete Activity ------------------
    @DeleteMapping("/activity/{activityId}")
    public ResponseEntity<?> deleteActivity(
            @PathVariable Integer activityId,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = extractUserIdFromToken(authorization);
            activityService.deleteActivity(activityId, userId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------ Fetch Activity ------------------
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<?> fetchActivity(
            @PathVariable Integer activityId,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            Integer userId = extractUserIdFromToken(authorization);
            Activity activity = activityService.fetchActivity(activityId, userId);
            return ResponseEntity.ok(activity);
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

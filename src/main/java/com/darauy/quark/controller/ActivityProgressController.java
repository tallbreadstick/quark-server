package com.darauy.quark.controller;

import com.darauy.quark.entity.progress.ActivityProgress;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.ActivityProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/progress/activities")
@RequiredArgsConstructor
public class ActivityProgressController {

    private final ActivityProgressService activityProgressService;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/progress/activities/{activityId}/initialize
     * Initialize activity progress (tracks section completion)
     * @param authHeader JWT token
     * @param activityId activity id
     * @return activity progress
     */
    @PostMapping("/{activityId}/initialize")
    public ResponseEntity<?> initializeActivityProgress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer activityId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            ActivityProgress progress = activityProgressService.initializeActivityProgress(userId, activityId);
            return ResponseEntity.ok(progress);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * PUT /api/progress/activities/{activityId}/completed-sections
     * Update completed sections count for an activity
     * @param authHeader JWT token
     * @param activityId activity id
     * @param completedSections number of completed sections
     * @return updated activity progress
     */
    @PutMapping("/{activityId}/completed-sections")
    public ResponseEntity<?> updateCompletedSections(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer activityId,
            @RequestParam Integer completedSections
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            ActivityProgress progress = activityProgressService.updateCompletedSections(userId, activityId, completedSections);
            return ResponseEntity.ok(progress);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * POST /api/progress/activities/{activityId}/increment-section
     * Increment completed sections by 1
     * @param authHeader JWT token
     * @param activityId activity id
     * @return updated activity progress
     */
    @PostMapping("/{activityId}/increment-section")
    public ResponseEntity<?> incrementCompletedSections(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer activityId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            ActivityProgress progress = activityProgressService.incrementCompletedSections(userId, activityId);
            return ResponseEntity.ok(progress);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * GET /api/progress/activities/{activityId}
     * Get activity progress for user
     * @param authHeader JWT token
     * @param activityId activity id
     * @return activity progress
     */
    @GetMapping("/{activityId}")
    public ResponseEntity<?> getActivityProgress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer activityId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            ActivityProgress progress = activityProgressService.getActivityProgress(userId, activityId);
            return ResponseEntity.ok(progress);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * GET /api/progress/activities
     * Get all activity progress for user
     * @param authHeader JWT token
     * @return list of activity progress
     */
    @GetMapping
    public ResponseEntity<?> getUserActivityProgress(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            List<ActivityProgress> progress = activityProgressService.getUserActivityProgress(userId);
            return ResponseEntity.ok(progress);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * DELETE /api/progress/activities/{activityId}
     * Delete activity progress for user
     * @param authHeader JWT token
     * @param activityId activity id
     * @return success message
     */
    @DeleteMapping("/{activityId}")
    public ResponseEntity<?> deleteActivityProgress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer activityId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            activityProgressService.deleteActivityProgress(userId, activityId);
            return ResponseEntity.ok("Activity progress deleted");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

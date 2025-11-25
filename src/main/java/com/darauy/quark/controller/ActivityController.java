package com.darauy.quark.controller;

import com.darauy.quark.dto.request.ActivityRequest;
import com.darauy.quark.dto.response.ActivityContentResponse;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ActivityRequest request
    ) {
        Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
        activityService.addActivity(chapterId, userId, request);
        return ResponseEntity.ok("Activity added to chapter");
    }

    // ------------------ Edit Activity ------------------
    @PutMapping("/activity/{activityId}")
    public ResponseEntity<?> editActivity(
            @PathVariable Integer activityId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ActivityRequest request
    ) {
        Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
        activityService.editActivity(activityId, userId, request);
        return ResponseEntity.ok("Activity updated");
    }

    // ------------------ Delete Activity ------------------
    @DeleteMapping("/activity/{activityId}")
    public ResponseEntity<?> deleteActivity(
            @PathVariable Integer activityId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
        activityService.deleteActivity(activityId, userId);
        return ResponseEntity.ok("Activity deleted");
    }

    // ------------------ Fetch Activity ------------------
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<ActivityContentResponse> fetchActivity(
            @PathVariable Integer activityId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
        ActivityContentResponse response = activityService.fetchActivity(activityId, userId);
        return ResponseEntity.ok(response);
    }
}

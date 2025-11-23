package com.darauy.quark.controller;

import com.darauy.quark.dto.SectionRequest;
import com.darauy.quark.entity.courses.activity.Section;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.repository.UserRepository;
import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.SectionService;
import jakarta.validation.Valid;
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
public class SectionController {

    private final SectionService sectionService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /** CREATE SECTION */
    @PostMapping("/activity/{activityId}/section")
    public ResponseEntity<?> createSection(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer activityId,
            @RequestBody @Valid SectionRequest request
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            Section created = sectionService.createSection(user, activityId, request);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** EDIT SECTION */
    @PutMapping("/section/{sectionId}")
    public ResponseEntity<?> editSection(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer sectionId,
            @RequestBody @Valid SectionRequest request
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            Section edited = sectionService.editSection(user, sectionId, request);
            return ResponseEntity.ok(edited);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** DELETE SECTION */
    @DeleteMapping("/section/{sectionId}")
    public ResponseEntity<?> deleteSection(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer sectionId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            sectionService.deleteSection(user, sectionId);
            return ResponseEntity.ok("Section deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** REORDER SECTIONS */
    @PatchMapping("/activity/{activityId}/section/reorder")
    public ResponseEntity<?> reorderSections(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer activityId,
            @RequestBody List<Integer> sectionIds
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            List<Section> reordered = sectionService.reorderSections(user, activityId, sectionIds);
            return ResponseEntity.ok(reordered);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** FETCH SECTION */
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<?> fetchSection(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer sectionId
    ) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            Section section = sectionService.fetchSection(user, sectionId);
            return ResponseEntity.ok(section);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

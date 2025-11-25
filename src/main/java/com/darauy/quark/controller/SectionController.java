package com.darauy.quark.controller;

import com.darauy.quark.dto.request.SectionRequest;
import com.darauy.quark.dto.response.SectionContentResponse;
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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
            User user = getUserFromHeader(authHeader);
            Section section = sectionService.createSection(user, activityId, request);
            return ResponseEntity.ok("Section added to activity");
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
            User user = getUserFromHeader(authHeader);
            Section section = sectionService.editSection(user, sectionId, request);
            return ResponseEntity.ok("Section updated successfully");
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
            User user = getUserFromHeader(authHeader);
            sectionService.deleteSection(user, sectionId);
            return ResponseEntity.ok("Section removed from activity");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /** REORDER SECTIONS */
    @PatchMapping("/activity/{activityId}")
    public ResponseEntity<?> reorderSections(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer activityId,
            @RequestBody List<Integer> sectionIds
    ) {
        try {
            User user = getUserFromHeader(authHeader);
            List<Section> reordered = sectionService.reorderSections(user, activityId, sectionIds);
//            List<SectionContentResponse> responseList = reordered.stream()
//                    .map(sectionService::toResponse)
//                    .collect(Collectors.toList());
            return ResponseEntity.ok("Section indexing reordered");
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
            User user = getUserFromHeader(authHeader);
            Section section = sectionService.fetchSection(user, sectionId);
            return ResponseEntity.ok(sectionService.toResponse(section));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private User getUserFromHeader(String authHeader) {
        Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
        return userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}

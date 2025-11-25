package com.darauy.quark.controller;

import com.darauy.quark.security.JwtUtil;
import com.darauy.quark.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    public ProfileController(ProfileService profileService, JwtUtil jwtUtil) {
        this.profileService = profileService;
        this.jwtUtil = jwtUtil;
    }

    // --- Upload profile picture ---
    @PostMapping
    public ResponseEntity<?> uploadProfilePicture(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("image") MultipartFile image) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            profileService.uploadProfilePicture(userId, image);
            return ResponseEntity.ok("Profile image updated");
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg.contains("too large")) return ResponseEntity.status(413).body(msg);
            else if (msg.contains("not found")) return ResponseEntity.status(404).body(msg);
            else return ResponseEntity.badRequest().body(msg);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // --- Fetch profile picture ---
    @GetMapping("/{userId}")
    public ResponseEntity<?> fetchProfilePicture(@PathVariable Integer userId) {
        try {
            String base64Image = profileService.fetchProfilePictureBase64(userId);
            if (base64Image == null) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(base64Image);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // --- Clear profile picture ---
    @DeleteMapping
    public ResponseEntity<?> clearProfilePicture(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            profileService.clearProfilePicture(userId);
            return ResponseEntity.ok("Profile image cleared");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // --- Update bio ---
    @PostMapping("/bio")
    public ResponseEntity<?> updateBio(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody String bio) {
        try {
            Integer userId = jwtUtil.extractUserIdFromHeader(authHeader);
            profileService.updateBio(userId, bio);
            return ResponseEntity.ok("User bio updated");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // --- Fetch bio ---
    @GetMapping("/bio/{userId}")
    public ResponseEntity<?> fetchBio(@PathVariable Integer userId) {
        try {
            String bio = profileService.fetchBio(userId);
            return ResponseEntity.ok(bio);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}

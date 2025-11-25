package com.darauy.quark.controller;

import com.darauy.quark.dto.AuthResponse;
import com.darauy.quark.dto.LoginRequest;
import com.darauy.quark.dto.RegisterRequest;
import com.darauy.quark.dto.UserResponse;
import com.darauy.quark.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> fetchUsers(@RequestParam String identifier) {
        try {
            List<UserResponse> users = authService.searchUsers(identifier);
            return ResponseEntity.ok(users);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}

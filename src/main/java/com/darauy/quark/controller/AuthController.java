package com.darauy.quark.controller;

import com.darauy.quark.dto.AuthResponse;
import com.darauy.quark.dto.LoginRequest;
import com.darauy.quark.dto.RegisterRequest;
import com.darauy.quark.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller handling user authentication endpoints.
 * 
 * This controller provides public endpoints for user authentication:
 * - Login: Authenticate existing users
 * - Register: Create new user accounts
 * 
 * Request Flow:
 * 1. Client sends request with credentials/user data
 * 2. Controller validates request format
 * 3. Service layer processes authentication/registration
 * 4. JWT token is generated on successful authentication
 * 5. Response includes user details and JWT token
 * 
 * Note: These endpoints are public and do not require JWT authentication.
 * The JWT token is returned in the response for use in subsequent requests.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * Endpoint: POST /api/auth/login
     * 
     * Request Body:
     * - identifier: Username or email address
     * - password: User password (plaintext)
     * 
     * Response:
     * - 200 OK: AuthResponse with user details and JWT token
     * - 400 BAD_REQUEST: Invalid credentials or user not found
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param req LoginRequest containing identifier and password
     * @return ResponseEntity with AuthResponse containing JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            AuthResponse res = authService.login(req);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    /**
     * Registers a new user account and returns a JWT token.
     * 
     * Endpoint: POST /api/auth/register
     * 
     * Request Body:
     * - username: Unique username (max 30 characters)
     * - email: Unique email address
     * - password: User password (will be hashed)
     * - userType: "Educator" or "Learner"
     * 
     * Response:
     * - 201 CREATED: AuthResponse with user details and JWT token
     * - 400 BAD_REQUEST: Username/email already exists or validation error
     * - 500 INTERNAL_SERVER_ERROR: Server error
     * 
     * @param req RegisterRequest containing user registration data
     * @return ResponseEntity with AuthResponse containing JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            AuthResponse res = authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }
}

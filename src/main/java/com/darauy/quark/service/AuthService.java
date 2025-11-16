package com.darauy.quark.service;

import com.darauy.quark.dto.AuthResponse;
import com.darauy.quark.dto.LoginRequest;
import com.darauy.quark.dto.RegisterRequest;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service handling user authentication and registration operations.
 * 
 * This service manages the authentication pipeline:
 * 1. User login: Validates credentials and generates JWT token
 * 2. User registration: Creates new user account and generates JWT token
 * 
 * Authentication Flow:
 * - Login: Username/Email + Password -> Validate -> Generate JWT -> Return AuthResponse
 * - Register: User details -> Validate uniqueness -> Hash password -> Save -> Generate JWT -> Return AuthResponse
 * 
 * Security:
 * - Passwords are hashed using BCrypt before storage
 * - JWT tokens are generated for successful authentications
 * - User credentials are validated against database records
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Authenticates a user and generates a JWT token.
     * 
     * Authentication Pipeline:
     * 1. Look up user by username or email (identifier can be either)
     * 2. Verify user exists
     * 3. Compare provided password with stored BCrypt hash
     * 4. Generate JWT token with user ID
     * 5. Return AuthResponse with user details and token
     * 
     * @param req LoginRequest containing identifier (username/email) and password
     * @return AuthResponse with user details, JWT token, and expiration time
     * @throws IllegalArgumentException if user not found or credentials are invalid
     */
    public AuthResponse login(LoginRequest req) {
        Optional<User> opt = userRepository.findByUsernameOrEmail(req.getIdentifier(), req.getIdentifier());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId());
        Long expiration = jwtService.getExpiration();

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userType(user.getUserType())
                .message("Login successful")
                .token(token)
                .expiration(expiration)
                .build();
    }

    /**
     * Registers a new user account and generates a JWT token.
     * 
     * Registration Pipeline:
     * 1. Validate username uniqueness (check if already exists)
     * 2. Validate email uniqueness (check if already exists)
     * 3. Hash password using BCrypt
     * 4. Create User entity with hashed password
     * 5. Save user to database
     * 6. Generate JWT token with new user ID
     * 7. Return AuthResponse with user details and token
     * 
     * @param req RegisterRequest containing username, email, password, and userType
     * @return AuthResponse with user details, JWT token, and expiration time
     * @throws IllegalArgumentException if username or email already exists
     */
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .userType(req.getUserType())
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getId());
        Long expiration = jwtService.getExpiration();

        return AuthResponse.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .userType(saved.getUserType())
                .message("Account created")
                .token(token)
                .expiration(expiration)
                .build();
    }
}

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

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse login(LoginRequest req) {
        Optional<User> opt = userRepository.findByUsernameOrEmail(req.getIdentifier(), req.getIdentifier());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userType(user.getUserType())
                .message("Login successful")
                .build();
    }

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

        return AuthResponse.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .userType(saved.getUserType())
                .message("Account created")
                .build();
    }
}

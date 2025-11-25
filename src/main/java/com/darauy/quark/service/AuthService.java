package com.darauy.quark.service;

import com.darauy.quark.dto.response.AuthResponse;
import com.darauy.quark.dto.request.LoginRequest;
import com.darauy.quark.dto.request.RegisterRequest;
import com.darauy.quark.dto.response.UserResponse;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.repository.UserRepository;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final Argon2 argon2 = Argon2Factory.create();

    @Transactional
    public void register(RegisterRequest request) {
        // Check username/email uniqueness
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash password
        String hash = argon2.hash(2, 65536, 1, request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(hash)
                .userType(request.getUserType())
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> optionalUser = request.getIdentifier().contains("@")
                ? userRepository.findByEmail(request.getIdentifier())
                : userRepository.findByUsername(request.getIdentifier());

        if (optionalUser.isEmpty()) throw new IllegalArgumentException("Invalid credentials");

        User user = optionalUser.get();
        if (!argon2.verify(user.getPassword(), request.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        Instant expiration = Instant.now().plus(24, ChronoUnit.HOURS);
        String token = Jwts.builder()
                .claim("user_id", user.getId())
                .claim("user_type", user.getUserType().name())
                .setExpiration(Date.from(expiration))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();

        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }

    public List<UserResponse> searchUsers(String identifier) {
        return userRepository.searchTop10ByIdentifier(identifier)
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getUserType()
                ))
                .toList();
    }
}

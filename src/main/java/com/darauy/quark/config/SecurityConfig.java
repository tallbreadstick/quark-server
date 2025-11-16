package com.darauy.quark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration class.
 * 
 * This configuration provides password encoding beans for the application.
 * Currently, only password encoding is configured here. JWT authentication
 * is handled separately via JwtInterceptor.
 * 
 * Password Encoding:
 * - BCryptPasswordEncoder is used for hashing passwords
 * - BCrypt automatically handles salt generation
 * - Default strength is 10 (2^10 = 1024 rounds)
 * 
 * Note: This is a minimal security configuration. Full Spring Security
 * is not enabled - authentication is handled via JWT interceptor.
 */
@Configuration
public class SecurityConfig {

    /**
     * Provides BCrypt password encoder bean.
     * 
     * BCrypt is used to hash passwords before storing in database.
     * It automatically generates a unique salt for each password,
     * making it resistant to rainbow table attacks.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

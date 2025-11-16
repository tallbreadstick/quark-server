package com.darauy.quark.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Service responsible for JSON Web Token (JWT) generation, validation, and parsing.
 * 
 * This service handles the complete JWT lifecycle:
 * - Token generation with user ID as the subject
 * - Token validation (signature and expiration checks)
 * - Extraction of claims (user ID, expiration date)
 * 
 * The JWT token contains:
 * - Subject: User ID (Integer converted to String)
 * - Issued At: Current timestamp
 * - Expiration: Current timestamp + configured expiration time
 * 
 * Tokens are signed using HMAC-SHA256 algorithm with a secret key
 * configured in application.properties.
 */
@Service
public class JwtService {

    /**
     * Secret key used for signing and verifying JWT tokens.
     * Must be at least 256 bits (32 characters) for HMAC-SHA256.
     * Configured via application.properties, with a default fallback value.
     */
    @Value("${jwt.secret:your-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-algorithm}")
    private String secret;

    /**
     * Token expiration time in milliseconds.
     * Default: 86400000ms (24 hours).
     * Configured via application.properties.
     */
    @Value("${jwt.expiration:86400000}") // 24 hours default
    private Long expiration;

    /**
     * Generates a SecretKey from the configured secret string.
     * This key is used for both signing and verifying tokens.
     * 
     * @return SecretKey instance for HMAC-SHA256 operations
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a new JWT token for the given user ID.
     * 
     * Token structure:
     * - Subject: User ID (as String)
     * - Issued At: Current timestamp
     * - Expiration: Current timestamp + expiration milliseconds
     * - Signature: HMAC-SHA256 signed with secret key
     * 
     * @param userId The user ID to embed in the token as the subject
     * @return Compact JWT token string (Base64 encoded)
     */
    public String generateToken(Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the user ID from a JWT token.
     * The user ID is stored as the token's subject claim.
     * 
     * @param token The JWT token string
     * @return User ID extracted from token subject
     * @throws NumberFormatException if subject cannot be parsed as Integer
     */
    public Integer getUserIdFromToken(String token) {
        return Integer.parseInt(getClaimFromToken(token, Claims::getSubject));
    }

    /**
     * Extracts the expiration date from a JWT token.
     * 
     * @param token The JWT token string
     * @return Expiration date from token claims
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract a specific claim from a token.
     * Uses a function to map Claims to the desired type.
     * 
     * @param token The JWT token string
     * @param claimsResolver Function to extract specific claim from Claims object
     * @return The extracted claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and validates a JWT token, returning all claims.
     * This method verifies the token signature using the secret key.
     * 
     * @param token The JWT token string to parse
     * @return Claims object containing all token claims
     * @throws Exception if token is malformed or signature is invalid
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates a JWT token by checking:
     * 1. Token signature (verifies it was signed with our secret key)
     * 2. Token expiration (ensures it hasn't expired)
     * 
     * @param token The JWT token string to validate
     * @return true if token is valid and not expired, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a token has expired by comparing expiration date with current time.
     * 
     * @param claims The token claims containing expiration date
     * @return true if token is expired, false otherwise
     */
    private Boolean isTokenExpired(Claims claims) {
        final Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * Returns the configured token expiration time in milliseconds.
     * 
     * @return Expiration time in milliseconds
     */
    public Long getExpiration() {
        return expiration;
    }
}


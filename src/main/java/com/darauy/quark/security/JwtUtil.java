package com.darauy.quark.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret; // non-static now

    public Integer extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return extractUserId(token);
    }

    public Integer extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            Object userIdObj = claims.get("user_id");
            if (userIdObj instanceof Integer) return (Integer) userIdObj;
            if (userIdObj instanceof Number) return ((Number) userIdObj).intValue();
            throw new IllegalArgumentException("Invalid token user_id");
        } catch (SignatureException ex) {
            throw new IllegalArgumentException("Invalid token signature");
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid token");
        }
    }
}

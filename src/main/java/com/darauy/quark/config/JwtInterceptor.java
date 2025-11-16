package com.darauy.quark.config;

import com.darauy.quark.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that validates JWT tokens for protected endpoints.
 * 
 * This interceptor runs before controller methods are invoked and:
 * 1. Identifies public endpoints (auth, GET requests to tags/courses)
 * 2. Validates JWT tokens for protected endpoints
 * 3. Extracts user ID from token and sets it as request attribute
 * 4. Blocks requests with invalid or missing tokens
 * 
 * Authentication Pipeline:
 * 1. Request arrives at interceptor
 * 2. Check if endpoint is public (skip validation if public)
 * 3. Extract Authorization header (Bearer token)
 * 4. Validate token signature and expiration
 * 5. Extract user ID from token
 * 6. Set userId as request attribute for controller access
 * 7. Allow request to proceed or return 401 Unauthorized
 * 
 * Public Endpoints (no JWT required):
 * - /api/auth/** (all authentication endpoints)
 * - GET /api/tags (fetch all tags for course creation)
 * - GET /api/tags/{id} (fetch tag by ID)
 * - GET /api/course (fetch all courses)
 * - GET /api/course/{id} (fetch course by ID)
 * 
 * Protected Endpoints (JWT required):
 * - POST /api/tags (create tag)
 * - DELETE /api/tags/{id} (delete tag)
 * - POST /api/course (create course)
 * - PATCH /api/course/{id} (update course)
 * - DELETE /api/course/{id} (delete course)
 * - POST /api/chapter (create chapter)
 * - GET /api/chapter (get all chapters for a course)
 * - GET /api/chapter/{id} (get chapter by ID)
 * - PATCH /api/chapter/{id} (update chapter)
 * - DELETE /api/chapter/{id} (delete chapter)
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    /**
     * Intercepts requests before controller methods are invoked.
     * 
     * This method:
     * 1. Checks if the endpoint is public (allows without validation)
     * 2. For protected endpoints, validates JWT token
     * 3. Extracts user ID and sets it as request attribute
     * 4. Returns false to block request if validation fails
     * 
     * @param request HttpServletRequest containing Authorization header
     * @param response HttpServletResponse for error responses
     * @param handler Handler method that will be invoked
     * @return true to allow request, false to block request
     * @throws Exception if error occurs during processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip JWT validation for public endpoints
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Public endpoints: auth endpoints and GET requests to tags
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        
        // Allow GET requests to /api/tags without authentication (for course creation menu)
        if (path.startsWith("/api/tags") && "GET".equals(method)) {
            return true;
        }
        
        // Allow GET requests to /api/course without authentication (for viewing courses)
        if (path.startsWith("/api/course") && "GET".equals(method)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtService.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
                return false;
            }
            Integer userId = jwtService.getUserIdFromToken(token);
            request.setAttribute("userId", userId);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return false;
        }

        return true;
    }
}


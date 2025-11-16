package com.darauy.quark.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class for Spring MVC.
 * 
 * This configuration class sets up:
 * 1. CORS (Cross-Origin Resource Sharing) settings
 *    - Allows requests from all origins (for testing)
 *    - Enables credentials (cookies, authorization headers)
 *    - Allows all HTTP methods and headers
 * 
 * 2. Request Interceptors
 *    - Registers JwtInterceptor for all /api/** endpoints
 *    - Interceptor validates JWT tokens before controller execution
 * 
 * CORS Configuration:
 * - All origins allowed for testing (allowedOriginPatterns: "*")
 * - Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
 * - All headers allowed
 * - Credentials enabled for JWT token transmission
 * - Preflight cache: 3600 seconds
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * Configures CORS settings for API endpoints.
     * 
     * Allows cross-origin requests from any origin for testing purposes.
     * This configuration enables all origins, methods, and headers to prevent
     * CORS issues during development and testing.
     * 
     * Note: For production, consider restricting allowedOrigins to specific domains.
     * 
     * @param registry CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")  // Allows all origins for testing
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // Cache preflight requests for 1 hour
    }

    /**
     * Registers the JWT authentication interceptor.
     * 
     * The interceptor will be applied to all /api/** endpoints,
     * validating JWT tokens before controller methods are invoked.
     * 
     * @param registry Interceptor registry to add interceptor to
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**");
    }
}


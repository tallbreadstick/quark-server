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
 *    - Allows requests from frontend (localhost:5173)
 *    - Enables credentials (cookies, authorization headers)
 *    - Allows all HTTP methods and headers
 * 
 * 2. Request Interceptors
 *    - Registers JwtInterceptor for all /api/** endpoints
 *    - Interceptor validates JWT tokens before controller execution
 * 
 * CORS Configuration:
 * - Frontend origin: http://localhost:5173 (Vite dev server)
 * - Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
 * - Credentials enabled for JWT token transmission
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * Configures CORS settings for API endpoints.
     * 
     * Allows cross-origin requests from the frontend application
     * running on localhost:5173 (typical Vite dev server port).
     * 
     * @param registry CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
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


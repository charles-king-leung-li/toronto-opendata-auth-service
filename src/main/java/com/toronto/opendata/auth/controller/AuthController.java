package com.toronto.opendata.auth.controller;

import com.toronto.opendata.auth.dto.JwtResponse;
import com.toronto.opendata.auth.dto.LoginRequest;
import com.toronto.opendata.auth.dto.MessageResponse;
import com.toronto.opendata.auth.dto.RegisterRequest;
import com.toronto.opendata.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user registration, login, and token refresh
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration request for username: {}", registerRequest.getUsername());
        
        try {
            JwtResponse response = authService.register(registerRequest);
            log.info("User registered successfully: {}", registerRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Registration failed for username: {}", registerRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Authenticate user and generate JWT tokens
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request for username: {}", loginRequest.getUsername());
        
        try {
            JwtResponse response = authService.login(loginRequest);
            log.info("User logged in successfully: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Login failed for username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid username or password"));
        }
    }
    
    /**
     * Refresh access token using refresh token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Token refresh request received");
        
        try {
            // Extract token from Bearer header
            String refreshToken = authHeader.substring(7);
            
            JwtResponse response = authService.refreshToken(refreshToken);
            log.info("Token refreshed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(new MessageResponse("Auth service is running"));
    }
}

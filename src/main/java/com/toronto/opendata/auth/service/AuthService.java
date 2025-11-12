package com.toronto.opendata.auth.service;

import com.toronto.opendata.auth.dto.JwtResponse;
import com.toronto.opendata.auth.dto.LoginRequest;
import com.toronto.opendata.auth.dto.RegisterRequest;
import com.toronto.opendata.auth.entity.Role;
import com.toronto.opendata.auth.entity.User;
import com.toronto.opendata.auth.repository.RoleRepository;
import com.toronto.opendata.auth.repository.UserRepository;
import com.toronto.opendata.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Register a new user
     */
    @Transactional
    public User register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        
        // Assign roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            }
        } else {
            // Default role: USER
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default USER role not found"));
            roles.add(userRole);
        }
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * Login user and generate JWT token
     */
    public JwtResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate tokens
        String token = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getUsername());
        
        // Update last login
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User logged in successfully: {}", request.getUsername());
        
        return new JwtResponse(token, refreshToken, user.getUsername(), user.getEmail());
    }
    
    /**
     * Refresh JWT token
     */
    public JwtResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        // Get username from token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        // Load user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, null
        );
        
        // Generate new tokens
        String newToken = jwtTokenProvider.generateToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        
        log.info("Token refreshed successfully for user: {}", username);
        
        return new JwtResponse(newToken, newRefreshToken, user.getUsername(), user.getEmail());
    }
}

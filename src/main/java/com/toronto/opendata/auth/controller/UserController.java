package com.toronto.opendata.auth.controller;

import com.toronto.opendata.auth.dto.MessageResponse;
import com.toronto.opendata.auth.entity.User;
import com.toronto.opendata.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User Management Controller
 * Handles CRUD operations for users
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private final UserService userService;
    
    /**
     * Get all users (Admin only)
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("User not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get current authenticated user
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        log.info("Fetching current user");
        
        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Current user not found");
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update user
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        log.info("Updating user with ID: {}", id);
        
        try {
            User updatedUser = userService.updateUser(id, userUpdate);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            log.error("Failed to update user with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Change user password
     * POST /api/users/{id}/change-password
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordRequest) {
        log.info("Changing password for user ID: {}", id);
        
        try {
            String oldPassword = passwordRequest.get("oldPassword");
            String newPassword = passwordRequest.get("newPassword");
            
            userService.changePassword(id, oldPassword, newPassword);
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } catch (RuntimeException e) {
            log.error("Failed to change password for user ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Enable/disable user (Admin only)
     * POST /api/users/{id}/enabled
     */
    @PostMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setUserEnabled(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        log.info("Setting enabled status for user ID: {}", id);
        
        try {
            boolean enabled = request.get("enabled");
            User user = userService.setUserEnabled(id, enabled);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Failed to set enabled status for user ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Lock/unlock user account (Admin only)
     * POST /api/users/{id}/locked
     */
    @PostMapping("/{id}/locked")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setUserLocked(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        log.info("Setting locked status for user ID: {}", id);
        
        try {
            boolean locked = request.get("locked");
            User user = userService.setUserLocked(id, locked);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Failed to set locked status for user ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Assign role to user (Admin only)
     * POST /api/users/{userId}/roles/{roleId}
     */
    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        
        try {
            User user = userService.assignRole(userId, roleId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Failed to assign role {} to user {}", roleId, userId, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Remove role from user (Admin only)
     * DELETE /api/users/{userId}/roles/{roleId}
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        
        try {
            User user = userService.removeRole(userId, roleId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Failed to remove role {} from user {}", roleId, userId, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Delete user (Admin only)
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Failed to delete user with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}

package com.toronto.opendata.auth.controller;

import com.toronto.opendata.auth.dto.MessageResponse;
import com.toronto.opendata.auth.entity.Permission;
import com.toronto.opendata.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Permission Management Controller
 * Handles CRUD operations for permissions
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    /**
     * Get all permissions
     * GET /api/permissions
     */
    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        log.info("Fetching all permissions");
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get permission by ID
     * GET /api/permissions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPermissionById(@PathVariable @NonNull Long id) {
        log.info("Fetching permission with ID: {}", id);
        
        try {
            Permission permission = permissionService.getPermissionById(id);
            return ResponseEntity.ok(permission);
        } catch (RuntimeException e) {
            log.error("Permission not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get permissions by resource
     * GET /api/permissions/resource/{resource}
     */
    @GetMapping("/resource/{resource}")
    public ResponseEntity<List<Permission>> getPermissionsByResource(@PathVariable @NonNull String resource) {
        log.info("Fetching permissions for resource: {}", resource);
        List<Permission> permissions = permissionService.getPermissionsByResource(resource);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get permissions by action
     * GET /api/permissions/action/{action}
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<List<Permission>> getPermissionsByAction(@PathVariable @NonNull String action) {
        log.info("Fetching permissions for action: {}", action);
        List<Permission> permissions = permissionService.getPermissionsByAction(action);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get permission by resource and action
     * GET /api/permissions/resource/{resource}/action/{action}
     */
    @GetMapping("/resource/{resource}/action/{action}")
    public ResponseEntity<?> getPermissionByResourceAndAction(
            @PathVariable @NonNull String resource,
            @PathVariable @NonNull String action) {
        log.info("Fetching permission for resource: {} and action: {}", resource, action);
        
        try {
            Permission permission = permissionService.getPermissionByResourceAndAction(resource, action);
            return ResponseEntity.ok(permission);
        } catch (RuntimeException e) {
            log.error("Permission not found for resource: {} and action: {}", resource, action);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create new permission
     * POST /api/permissions
     */
    @PostMapping
    public ResponseEntity<?> createPermission(@RequestBody @NonNull Map<String, String> request) {
        log.info("Creating new permission");
        
        try {
            String resource = request.get("resource");
            String action = request.get("action");
            String description = request.get("description");
            
            Permission permission = permissionService.createPermission(resource, action, description);
            return ResponseEntity.ok(permission);
        } catch (RuntimeException e) {
            log.error("Failed to create permission", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Update permission
     * PUT /api/permissions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePermission(
            @PathVariable @NonNull Long id,
            @RequestBody @NonNull Map<String, String> request) {
        log.info("Updating permission with ID: {}", id);
        
        try {
            String resource = request.get("resource");
            String action = request.get("action");
            String description = request.get("description");
            
            Permission permission = permissionService.updatePermission(id, resource, action, description);
            return ResponseEntity.ok(permission);
        } catch (RuntimeException e) {
            log.error("Failed to update permission with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Delete permission
     * DELETE /api/permissions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePermission(@PathVariable @NonNull Long id) {
        log.info("Deleting permission with ID: {}", id);
        
        try {
            permissionService.deletePermission(id);
            return ResponseEntity.ok(new MessageResponse("Permission deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Failed to delete permission with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Check if permission exists
     * GET /api/permissions/exists
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> permissionExists(
            @RequestParam @NonNull String resource,
            @RequestParam @NonNull String action) {
        log.info("Checking if permission exists for resource: {} and action: {}", resource, action);
        
        boolean exists = permissionService.permissionExists(resource, action);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}

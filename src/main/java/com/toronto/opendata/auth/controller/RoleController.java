package com.toronto.opendata.auth.controller;

import com.toronto.opendata.auth.dto.MessageResponse;
import com.toronto.opendata.auth.entity.Permission;
import com.toronto.opendata.auth.entity.Role;
import com.toronto.opendata.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Role Management Controller
 * Handles CRUD operations for roles
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * Get all roles
     * GET /api/roles
     */
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("Fetching all roles");
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Get role by ID
     * GET /api/roles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        log.info("Fetching role with ID: {}", id);
        
        try {
            Role role = roleService.getRoleById(id);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            log.error("Role not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get role by name
     * GET /api/roles/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getRoleByName(@PathVariable String name) {
        log.info("Fetching role with name: {}", name);
        
        try {
            Role role = roleService.getRoleByName(name);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            log.error("Role not found with name: {}", name);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create new role
     * POST /api/roles
     */
    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Map<String, String> request) {
        log.info("Creating new role");
        
        try {
            String name = request.get("name");
            String description = request.get("description");
            
            Role role = roleService.createRole(name, description);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            log.error("Failed to create role", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Update role
     * PUT /api/roles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("Updating role with ID: {}", id);
        
        try {
            String name = request.get("name");
            String description = request.get("description");
            
            Role role = roleService.updateRole(id, name, description);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            log.error("Failed to update role with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Delete role
     * DELETE /api/roles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        log.info("Deleting role with ID: {}", id);
        
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(new MessageResponse("Role deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Failed to delete role with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Assign permission to role
     * POST /api/roles/{roleId}/permissions/{permissionId}
     */
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<?> assignPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);
        
        try {
            Role role = roleService.assignPermission(roleId, permissionId);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            log.error("Failed to assign permission {} to role {}", permissionId, roleId, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Remove permission from role
     * DELETE /api/roles/{roleId}/permissions/{permissionId}
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<?> removePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);
        
        try {
            Role role = roleService.removePermission(roleId, permissionId);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            log.error("Failed to remove permission {} from role {}", permissionId, roleId, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get all permissions for a role
     * GET /api/roles/{id}/permissions
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<?> getRolePermissions(@PathVariable Long id) {
        log.info("Fetching permissions for role ID: {}", id);
        
        try {
            List<Permission> permissions = roleService.getRolePermissions(id);
            return ResponseEntity.ok(permissions);
        } catch (RuntimeException e) {
            log.error("Failed to fetch permissions for role ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}

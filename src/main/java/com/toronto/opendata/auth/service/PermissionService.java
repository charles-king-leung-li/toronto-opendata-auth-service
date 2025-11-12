package com.toronto.opendata.auth.service;

import com.toronto.opendata.auth.entity.Permission;
import com.toronto.opendata.auth.repository.PermissionRepository;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    
    /**
     * Get all permissions
     */
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        log.debug("Fetching all permissions");
        return permissionRepository.findAll();
    }
    
    /**
     * Get permission by ID
     */
    @Transactional(readOnly = true)
    public Permission getPermissionById(@NonNull Long id) {
        log.debug("Fetching permission with ID: {}", id);
        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
    }
    
    /**
     * Get permissions by resource
     */
    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByResource(@NonNull String resource) {
        log.debug("Fetching permissions for resource: {}", resource);
        return permissionRepository.findByResource(resource);
    }
    
    /**
     * Get permissions by action
     */
    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByAction(String action) {
        log.debug("Fetching permissions for action: {}", action);
        return permissionRepository.findByAction(action);
    }
    
    /**
     * Get permission by resource and action
     */
    @Transactional(readOnly = true)
    public Permission getPermissionByResourceAndAction(String resource, String action) {
        log.debug("Fetching permission for resource: {} and action: {}", resource, action);
        return permissionRepository.findByResourceAndAction(resource, action)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Permission not found for resource: %s and action: %s", resource, action)));
    }
    
    /**
     * Create new permission
     */
    @Transactional
    public Permission createPermission(String resource, String action, String description) {
        log.info("Creating new permission - resource: {}, action: {}", resource, action);
        
        // Check if permission already exists
        if (permissionRepository.findByResourceAndAction(resource, action).isPresent()) {
            throw new RuntimeException(
                    String.format("Permission already exists for resource: %s and action: %s", resource, action));
        }
        
        Permission permission = new Permission();
        permission.setResource(resource);
        permission.setAction(action);
        permission.setDescription(description);
        
        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission created successfully: {}:{}", savedPermission.getResource(), savedPermission.getAction());
        
        return savedPermission;
    }
    
    /**
     * Update permission
     */
    @Transactional
    public Permission updatePermission(@NonNull Long id, String resource, String action, String description) {
        log.info("Updating permission with ID: {}", id);
        
        Permission permission = getPermissionById(id);
        
        // Check if resource/action combination is being changed and if it already exists
        if ((resource != null && !resource.equals(permission.getResource())) ||
            (action != null && !action.equals(permission.getAction()))) {
            
            String newResource = resource != null ? resource : permission.getResource();
            String newAction = action != null ? action : permission.getAction();
            
            if (permissionRepository.findByResourceAndAction(newResource, newAction).isPresent()) {
                throw new RuntimeException(
                        String.format("Permission already exists for resource: %s and action: %s", newResource, newAction));
            }
            
            if (resource != null) {
                permission.setResource(resource);
            }
            if (action != null) {
                permission.setAction(action);
            }
        }
        
        if (description != null) {
            permission.setDescription(description);
        }
        
        @SuppressWarnings("null")
        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission updated successfully: {}:{}", 
                savedPermission.getResource(), savedPermission.getAction());
        
        return savedPermission;
    }
    
    /**
     * Delete permission
     */
    @Transactional
    public void deletePermission(Long id) {
        log.info("Deleting permission with ID: {}", id);
        
        @SuppressWarnings("null")
        Permission permission = getPermissionById(id);
        
        // Remove all role associations
        permission.getRoles().clear();
        
        permissionRepository.delete(permission);
        log.info("Permission deleted successfully: {}:{}", 
                permission.getResource(), permission.getAction());
    }
    
    /**
     * Check if a permission exists
     */
    @Transactional(readOnly = true)
    public boolean permissionExists(String resource, String action) {
        return permissionRepository.findByResourceAndAction(resource, action).isPresent();
    }
}

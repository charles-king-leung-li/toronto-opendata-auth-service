package com.toronto.opendata.auth.service;

import com.toronto.opendata.auth.entity.Permission;
import com.toronto.opendata.auth.entity.Role;
import com.toronto.opendata.auth.repository.PermissionRepository;
import com.toronto.opendata.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    /**
     * Get all roles
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll();
    }
    
    /**
     * Get role by ID
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        log.debug("Fetching role with ID: {}", id);
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }
    
    /**
     * Get role by name
     */
    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        log.debug("Fetching role with name: {}", name);
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + name));
    }
    
    /**
     * Create new role
     */
    @Transactional
    public Role createRole(String name, String description) {
        log.info("Creating new role: {}", name);
        
        // Check if role already exists
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("Role already exists with name: " + name);
        }
        
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        
        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully: {}", savedRole.getName());
        
        return savedRole;
    }
    
    /**
     * Update role
     */
    @Transactional
    public Role updateRole(Long id, String name, String description) {
        log.info("Updating role with ID: {}", id);
        
        Role role = getRoleById(id);
        
        // Check if name is being changed and if new name already exists
        if (name != null && !name.equals(role.getName())) {
            if (roleRepository.existsByName(name)) {
                throw new RuntimeException("Role already exists with name: " + name);
            }
            role.setName(name);
        }
        
        if (description != null) {
            role.setDescription(description);
        }
        
        @SuppressWarnings("null")
        Role savedRole = roleRepository.save(role);
        log.info("Role updated successfully: {}", savedRole.getName());
        
        return savedRole;
    }
    
    /**
     * Delete role
     */
    @Transactional
    public void deleteRole(Long id) {
        log.info("Deleting role with ID: {}", id);
        
        Role role = getRoleById(id);
        
        // Remove all user associations
        role.getUsers().clear();
        
        // Remove all permission associations
        role.getPermissions().clear();
        
        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", role.getName());
    }
    
    /**
     * Assign permission to role
     */
    @Transactional
    public Role assignPermission(Long roleId, Long permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);
        
        Role role = getRoleById(roleId);
        @SuppressWarnings("null")
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));
        
        role.getPermissions().add(permission);
        
        Role savedRole = roleRepository.save(role);
        log.info("Permission assigned successfully to role: {}", role.getName());
        
        return savedRole;
    }
    
    /**
     * Remove permission from role
     */
    @Transactional
    public Role removePermission(Long roleId, Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);
        
        Role role = getRoleById(roleId);
        @SuppressWarnings("null")
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));
        
        role.getPermissions().remove(permission);
        
        Role savedRole = roleRepository.save(role);
        log.info("Permission removed successfully from role: {}", role.getName());
        
        return savedRole;
    }
    
    /**
     * Get all permissions for a role
     */
    @Transactional(readOnly = true)
    public List<Permission> getRolePermissions(Long roleId) {
        log.debug("Fetching permissions for role ID: {}", roleId);
        
        Role role = getRoleById(roleId);
        return role.getPermissions().stream().toList();
    }
}

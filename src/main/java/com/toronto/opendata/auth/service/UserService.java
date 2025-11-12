package com.toronto.opendata.auth.service;

import com.toronto.opendata.auth.entity.Role;
import com.toronto.opendata.auth.entity.User;
import com.toronto.opendata.auth.repository.RoleRepository;
import com.toronto.opendata.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }
    
    /**
     * Get user by ID
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        log.debug("Fetching user with username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    /**
     * Update user
     */
    @Transactional
    public User updateUser(Long id, User userUpdate) {
        log.info("Updating user with ID: {}", id);
        
        User user = getUserById(id);
        
        if (userUpdate.getFirstName() != null) {
            user.setFirstName(userUpdate.getFirstName());
        }
        if (userUpdate.getLastName() != null) {
            user.setLastName(userUpdate.getLastName());
        }
        if (userUpdate.getEmail() != null && !userUpdate.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userUpdate.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(userUpdate.getEmail());
        }
        
        @SuppressWarnings("null")
        User savedUser = userRepository.save(user);
        log.info("User updated successfully: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("Changing password for user ID: {}", id);
        
        User user = getUserById(id);
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", user.getUsername());
    }
    
    /**
     * Enable/disable user
     */
    @Transactional
    public User setUserEnabled(Long id, boolean enabled) {
        log.info("Setting user {} enabled status to: {}", id, enabled);
        
        User user = getUserById(id);
        user.setEnabled(enabled);
        
        return userRepository.save(user);
    }
    
    /**
     * Lock/unlock user account
     */
    @Transactional
    public User setUserLocked(Long id, boolean locked) {
        log.info("Setting user {} locked status to: {}", id, locked);
        
        User user = getUserById(id);
        user.setAccountNonLocked(!locked);
        
        return userRepository.save(user);
    }
    
    /**
     * Assign role to user
     */
    @Transactional
    public User assignRole(Long userId, Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        
        User user = getUserById(userId);
        @SuppressWarnings("null")
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        user.getRoles().add(role);
        
        return userRepository.save(user);
    }
    
    /**
     * Remove role from user
     */
    @Transactional
    public User removeRole(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        
        User user = getUserById(userId);
        @SuppressWarnings("null")
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        user.getRoles().remove(role);
        
        return userRepository.save(user);
    }
    
    /**
     * Delete user
     */
    @SuppressWarnings("null")
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        User user = getUserById(id);
        userRepository.delete(user);
        
        log.info("User deleted successfully: {}", user.getUsername());
    }
}

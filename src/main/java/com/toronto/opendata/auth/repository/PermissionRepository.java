package com.toronto.opendata.auth.repository;

import com.toronto.opendata.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByName(String name);
    
    List<Permission> findByResource(String resource);
    
    List<Permission> findByAction(String action);
    
    Optional<Permission> findByResourceAndAction(String resource, String action);
    
    Boolean existsByName(String name);
}

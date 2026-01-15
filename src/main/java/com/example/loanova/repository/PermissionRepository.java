package com.example.loanova.repository;

import com.example.loanova.entity.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
  Optional<Permission> findByPermissionName(String permissionName);
}

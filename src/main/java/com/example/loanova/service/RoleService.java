package com.example.loanova.service;

import com.example.loanova.dto.request.RoleRequest;
import com.example.loanova.dto.request.RoleUpdateRequest;
import com.example.loanova.dto.response.RoleResponse;
import com.example.loanova.entity.Permission;
import com.example.loanova.entity.Role;
import com.example.loanova.exception.DuplicateResourceException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.PermissionRepository;
import com.example.loanova.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  public RoleService(
      RoleRepository roleRepository,
      PermissionRepository permissionRepository) {
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
  }

  /**
   * Mendapatkan semua Role yang ada di sistem (auto exclude deleted via @Where)
   */
  public List<RoleResponse> getAllRoles() {
    return roleRepository.findAll().stream().map(this::toResponse).toList();
  }

  /**
   * Menambahkan Role baru ke dalam sistem
   */
  @Transactional
  public RoleResponse createRole(RoleRequest request) {
    if (roleRepository.existsByRoleName(request.getRoleName())) {
      throw new DuplicateResourceException(
          "Role name "
              + request.getRoleName()
              + " sudah ada, anda tidak bisa menambahkan role yang sama");
    }

    Set<Permission> permissions = new HashSet<>();
    if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
      permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
    }

    Role role = Role.builder()
        .roleName(request.getRoleName())
        .roleDescription(request.getRoleDescription())
        .permissions(permissions)
        .build();
    return toResponse(roleRepository.save(role));
  }

  /**
   * Mengupdate deskripsi dan hak akses data role
   */
  @Transactional
  public RoleResponse updateRole(Long id, RoleUpdateRequest request) {
    Role role = roleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Role dengan id " + id + " tidak ditemukan"));

    role.setRoleDescription(request.getRoleDescription());

    if (request.getPermissionIds() != null) {
      Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
      role.setPermissions(permissions);
    }

    return toResponse(roleRepository.save(role));
  }

  /**
   * Soft delete - menandai role sebagai deleted tanpa menghapus dari database
   */
  public void deleteRole(Long id) {
    Role role = roleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Maaf, tidak ada data role dengan id " + id));
    role.softDelete();
    roleRepository.save(role);
  }

  /**
   * Method helper untuk membantu mapping Entity ke DTO
   */
  private RoleResponse toResponse(Role role) {
    Set<String> permissionNames = new HashSet<>();
    if (role.getPermissions() != null) {
      permissionNames = role.getPermissions().stream()
          .map(Permission::getPermissionName)
          .collect(Collectors.toSet());
    }

    return RoleResponse.builder()
        .id(role.getId())
        .roleName(role.getRoleName())
        .roleDescription(role.getRoleDescription())
        .permissions(permissionNames)
        .build();
  }
}

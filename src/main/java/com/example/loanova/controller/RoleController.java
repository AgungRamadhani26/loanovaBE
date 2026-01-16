package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.request.RoleRequest;
import com.example.loanova.dto.request.RoleUpdateRequest;
import com.example.loanova.dto.response.RoleResponse;
import com.example.loanova.service.RoleService;
import com.example.loanova.util.ResponseUtil;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

  private final RoleService roleService;

  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  // GET ALL ROLES
  // Yang bisa akses hanya SUPERADMIN
  @PreAuthorize("hasAuthority('ROLE:READ')")
  @GetMapping
  public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
    List<RoleResponse> roles = roleService.getAllRoles();
    return ResponseUtil.ok(roles, "Berhasil mengambil daftar role");
  }

  // CREATE ROLE
  // Yang bisa akses hanya SUPERADMIN
  @PreAuthorize("hasAuthority('ROLE:CREATE')")
  @PostMapping
  public ResponseEntity<ApiResponse<RoleResponse>> createRole(
      @Valid @RequestBody RoleRequest request) {
    RoleResponse role = roleService.createRole(request);
    return ResponseUtil.created(role, "Berhasil membuat role baru");
  }

  // UPDATE ROLE
  // Yang bisa akses hanya SUPERADMIN
  @PreAuthorize("hasAuthority('ROLE:UPDATE')")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
      @PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
    RoleResponse role = roleService.updateRole(id, request);
    return ResponseUtil.ok(role, "Berhasil memperbarui data role dan hak akses");
  }

  // DELETE ROLE
  // Yang bisa akses hanya SUPERADMIN
  @PreAuthorize("hasAuthority('ROLE:DELETE')")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
    roleService.deleteRole(id);
    return ResponseUtil.ok(null, "Berhasil menghapus role");
  }
}

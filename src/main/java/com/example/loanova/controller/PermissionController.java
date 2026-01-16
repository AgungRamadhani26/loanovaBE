package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.response.PermissionResponse;
import com.example.loanova.service.PermissionService;
import com.example.loanova.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * GET ALL PERMISSIONS
     * Hanya SUPERADMIN yang bisa melihat daftar semua hak akses yang ada di sistem.
     */
    @PreAuthorize("hasAuthority('PERMISSION:READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseUtil.ok(permissions, "Berhasil mengambil daftar hak akses");
    }
}

package com.example.loanova.seeder;

import com.example.loanova.entity.Permission;
import com.example.loanova.entity.Role;
import com.example.loanova.repository.PermissionRepository;
import com.example.loanova.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedPermissions();
    }

    private void seedPermissions() {
        // 1. Define Permissions
        List<Permission> permissions = Arrays.asList(
                createPermissionIfNotFound("USER:READ", "Melihat daftar user"),
                createPermissionIfNotFound("USER:CREATE", "Menambah user baru"),
                createPermissionIfNotFound("USER:UPDATE", "Mengubah data user"),
                createPermissionIfNotFound("USER:DELETE", "Menghapus user (soft-delete)"),
                createPermissionIfNotFound("BRANCH:READ", "Melihat daftar cabang"),
                createPermissionIfNotFound("BRANCH:CREATE", "Menambah cabang baru"),
                createPermissionIfNotFound("LOAN:SUBMIT", "Mengajukan pinjaman (Customer)"),
                createPermissionIfNotFound("LOAN:READ", "Melihat detail pinjaman"),
                createPermissionIfNotFound("LOAN:REVIEW", "Review pinjaman (Marketing)"),
                createPermissionIfNotFound("LOAN:APPROVE", "Menyetujui pinjaman (BM)"),
                createPermissionIfNotFound("LOAN:DISBURSE", "Mencairkan pinjaman (Backoffice)")
        );

        // 2. Assign Permissions to Roles
        assignPermissionsToRole("SUPERADMIN", new HashSet<>(permissions)); // Superadmin punya semua

        assignPermissionsToRole("MARKETING", new HashSet<>(Arrays.asList(
                getPermission("LOAN:READ"),
                getPermission("LOAN:REVIEW"),
                getPermission("BRANCH:READ")
        )));

        assignPermissionsToRole("BRANCHMANAGER", new HashSet<>(Arrays.asList(
                getPermission("LOAN:READ"),
                getPermission("LOAN:APPROVE"),
                getPermission("BRANCH:READ")
        )));

        assignPermissionsToRole("BACKOFFICE", new HashSet<>(Arrays.asList(
                getPermission("LOAN:READ"),
                getPermission("LOAN:DISBURSE")
        )));

        assignPermissionsToRole("CUSTOMER", new HashSet<>(Arrays.asList(
                getPermission("LOAN:SUBMIT"),
                getPermission("LOAN:READ")
        )));
    }

    private Permission createPermissionIfNotFound(String name, String description) {
        return permissionRepository.findByPermissionName(name)
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder()
                                .permissionName(name)
                                .permissionDescription(description)
                                .build()
                ));
    }

    private Permission getPermission(String name) {
        return permissionRepository.findByPermissionName(name)
                .orElseThrow(() -> new RuntimeException("Permission " + name + " not found"));
    }

    private void assignPermissionsToRole(String roleName, Set<Permission> permissions) {
        roleRepository.findByRoleName(roleName).ifPresent(role -> {
            role.setPermissions(permissions);
            roleRepository.save(role);
        });
    }
}

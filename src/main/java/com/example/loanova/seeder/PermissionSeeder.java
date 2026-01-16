package com.example.loanova.seeder;

import com.example.loanova.entity.Permission;
import com.example.loanova.repository.PermissionRepository;
import com.example.loanova.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionSeeder(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedPermissions();
    }

    private void seedPermissions() {
        // 1. Define All Granular Permissions
        List<Permission> allPermissionsList = Arrays.asList(
                // AUTH Management
                createPermissionIfNotFound("AUTH:LOGOUT", "Melakukan logout"),
                createPermissionIfNotFound("AUTH:CHANGE_PASSWORD", "Mengubah password sendiri"),

                // USER Management
                createPermissionIfNotFound("USER:READ", "Melihat daftar user"),
                createPermissionIfNotFound("USER:DETAILS", "Melihat detail user"),
                createPermissionIfNotFound("USER:CREATE", "Menambah user baru"),
                createPermissionIfNotFound("USER:UPDATE", "Mengubah data user"),
                createPermissionIfNotFound("USER:DELETE", "Menghapus user"),

                // ROLE Management
                createPermissionIfNotFound("ROLE:READ", "Melihat daftar role"),
                createPermissionIfNotFound("ROLE:CREATE", "Menambah role baru"),
                createPermissionIfNotFound("ROLE:UPDATE", "Mengubah deskripsi role"),
                createPermissionIfNotFound("ROLE:DELETE", "Menghapus role"),

                // PERMISSION Management (Discovery)
                createPermissionIfNotFound("PERMISSION:READ", "Melihat daftar hak akses (dictionary)"),

                // BRANCH Management
                createPermissionIfNotFound("BRANCH:READ", "Melihat daftar cabang"),
                createPermissionIfNotFound("BRANCH:CREATE", "Menambah cabang baru"),
                createPermissionIfNotFound("BRANCH:UPDATE", "Mengubah data cabang"),
                createPermissionIfNotFound("BRANCH:DELETE", "Menghapus cabang"),
                createPermissionIfNotFound("BRANCH:RESTORE", "Memulihkan cabang yang dihapus"),

                // LOAN Management
                createPermissionIfNotFound("LOAN:SUBMIT", "Mengajukan pinjaman"),
                createPermissionIfNotFound("LOAN:READ_MY", "Melihat pengajuan sendiri"),
                createPermissionIfNotFound("LOAN:DETAILS", "Melihat detail pengajuan"),
                createPermissionIfNotFound("LOAN:HISTORY", "Melihat history status pengajuan"),
                createPermissionIfNotFound("LOAN:LIST_PENDING_REVIEW", "Melihat daftar pending review (Marketing)"),
                createPermissionIfNotFound("LOAN:REVIEW", "Melakukan review pengajuan (Marketing)"),
                createPermissionIfNotFound("LOAN:LIST_WAITING_APPROVAL", "Melihat daftar waiting approval (BM)"),
                createPermissionIfNotFound("LOAN:APPROVE", "Melakukan approval pengajuan (BM)"),
                createPermissionIfNotFound("LOAN:LIST_WAITING_DISBURSE", "Melihat daftar waiting disbursement (Backoffice)"),
                createPermissionIfNotFound("LOAN:DISBURSE", "Melakukan pencairan pinjaman (Backoffice)"),
                createPermissionIfNotFound("LOAN:REJECT_BACKOFFICE", "Menolak pencairan pinjaman (Backoffice)"),

                // PLAFOND Management
                createPermissionIfNotFound("PLAFOND:READ", "Melihat daftar plafond"),
                createPermissionIfNotFound("PLAFOND:DETAILS", "Melihat detail plafond"),
                createPermissionIfNotFound("PLAFOND:CREATE", "Menambah plafond baru"),
                createPermissionIfNotFound("PLAFOND:UPDATE", "Mengubah data plafond"),
                createPermissionIfNotFound("PLAFOND:DELETE", "Menghapus plafond"),
                createPermissionIfNotFound("PLAFOND:RESTORE", "Memulihkan plafond"),

                // USER PLAFOND Assignment
                createPermissionIfNotFound("USER_PLAFOND:ASSIGN", "Assign plafond ke user"),
                createPermissionIfNotFound("USER_PLAFOND:READ", "Melihat plafond aktif user"),

                // PROFILE Management
                createPermissionIfNotFound("PROFILE:COMPLETE", "Melengkapi data profil"),
                createPermissionIfNotFound("PROFILE:UPDATE", "Mengubah data profil"),
                createPermissionIfNotFound("PROFILE:READ_MY", "Melihat profil sendiri")
        );

        // Permissions for EVERY authenticated user (Roles that need base system access)
        List<String> commonBasePermissions = Arrays.asList(
                "AUTH:LOGOUT", "AUTH:CHANGE_PASSWORD"
        );

        // 2. Assign Permissions to Roles strictly following class comments

        // SUPERADMIN: Based on comments, SUPERADMIN is for administrative tasks
        assignPermissionsToRole("SUPERADMIN", getPermissionsSet(combine(commonBasePermissions, Arrays.asList(
                "USER:READ", "USER:DETAILS", "USER:CREATE", "USER:UPDATE", "USER:DELETE",
                "ROLE:READ", "ROLE:CREATE", "ROLE:UPDATE", "ROLE:DELETE",
                "PERMISSION:READ",
                "BRANCH:READ", "BRANCH:CREATE", "BRANCH:UPDATE", "BRANCH:DELETE", "BRANCH:RESTORE",
                "PLAFOND:READ", "PLAFOND:DETAILS", "PLAFOND:CREATE", "PLAFOND:UPDATE", "PLAFOND:DELETE", "PLAFOND:RESTORE",
                "USER_PLAFOND:ASSIGN", "USER_PLAFOND:READ"
        ))));

        // MARKETING:
        assignPermissionsToRole("MARKETING", getPermissionsSet(combine(commonBasePermissions, Arrays.asList(
                "BRANCH:READ", "LOAN:DETAILS", "LOAN:HISTORY", "LOAN:LIST_PENDING_REVIEW", "LOAN:REVIEW"
        ))));

        // BRANCHMANAGER:
        assignPermissionsToRole("BRANCHMANAGER", getPermissionsSet(combine(commonBasePermissions, Arrays.asList(
                "BRANCH:READ", "LOAN:DETAILS", "LOAN:HISTORY", "LOAN:LIST_WAITING_APPROVAL", "LOAN:APPROVE"
        ))));

        // BACKOFFICE:
        assignPermissionsToRole("BACKOFFICE", getPermissionsSet(combine(commonBasePermissions, Arrays.asList(
                "LOAN:DETAILS", "LOAN:HISTORY", "LOAN:LIST_WAITING_DISBURSE", "LOAN:DISBURSE", "LOAN:REJECT_BACKOFFICE",
                "USER_PLAFOND:ASSIGN", "USER_PLAFOND:READ"
        ))));

        // CUSTOMER:
        assignPermissionsToRole("CUSTOMER", getPermissionsSet(combine(commonBasePermissions, Arrays.asList(
                "LOAN:SUBMIT", "LOAN:READ_MY", "LOAN:DETAILS", "LOAN:HISTORY",
                "PROFILE:COMPLETE", "PROFILE:UPDATE", "PROFILE:READ_MY"
        ))));
    }

    private List<String> combine(List<String> list1, List<String> list2) {
        List<String> result = new java.util.ArrayList<>(list1);
        result.addAll(list2);
        return result;
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

    private Set<Permission> getPermissionsSet(List<String> names) {
        return names.stream()
                .map(name -> permissionRepository.findByPermissionName(name)
                        .orElseThrow(() -> new RuntimeException("Permission " + name + " not found")))
                .collect(Collectors.toSet());
    }

    private void assignPermissionsToRole(String roleName, Set<Permission> permissions) {
        roleRepository.findByRoleName(roleName).ifPresent(role -> {
            role.setPermissions(permissions);
            roleRepository.save(role);
        });
    }
}

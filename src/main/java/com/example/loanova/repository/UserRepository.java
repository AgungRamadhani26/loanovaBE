package com.example.loanova.repository;

import com.example.loanova.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  @Query(value = "select count(*) from users where username = :username", nativeQuery = true)
  long countUsernameNative(@Param("username") String username);

  @Query(value = "select count(*) from users where email = :email", nativeQuery = true)
  long countEmailNative(@Param("email") String email);

  default boolean existsByUsername(String username) {
    return countUsernameNative(username) > 0;
  }

  default boolean existsByEmail(String email) {
    return countEmailNative(email) > 0;
  }

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  /**
   * Cek apakah ada user AKTIF yang masih terhubung ke cabang tertentu.
   * Digunakan untuk validasi 'Safe-Delete' pada Branch.
   */
  @Query(value = "SELECT COUNT(*) FROM users WHERE branch_id = :branchId AND is_active = 1 AND deleted_at IS NULL", nativeQuery = true)
  long countByBranchIdAndIsActiveTrueNative(@Param("branchId") Long branchId);

  default boolean existsByBranchIdAndIsActiveTrue(Long branchId) {
    return countByBranchIdAndIsActiveTrueNative(branchId) > 0;
  }

  /**
   * Cek apakah ada user (aktif maupun non-aktif/deleted) yang masih menggunakan
   * Role tertentu.
   * Digunakan untuk validasi 'Safe-Delete' pada Role.
   */
  @Query(value = "SELECT COUNT(*) FROM user_roles WHERE role_id = :roleId", nativeQuery = true)
  long countByRolesIdNative(@Param("roleId") Long roleId);

  default boolean existsByRolesId(Long roleId) {
    return countByRolesIdNative(roleId) > 0;
  }

  /**
   * Menghitung jumlah user aktif yang memiliki role tertentu.
   * Penting untuk proteksi 'Admin Terakhir' di sistem.
   */
  @Query(value = "SELECT COUNT(u.id) FROM users u " +
      "JOIN user_roles ur ON u.id = ur.user_id " +
      "JOIN roles r ON ur.role_id = r.id " +
      "WHERE r.role_name = :roleName AND u.is_active = 1 AND u.deleted_at IS NULL", nativeQuery = true)
  long countByRolesRoleNameAndIsActiveTrue(@Param("roleName") String roleName);

  /**
   * Cari user berdasarkan Google ID (untuk Google Sign-In).
   * Digunakan untuk login dengan akun Google yang sudah terdaftar.
   */
  Optional<User> findByGoogleId(String googleId);
}

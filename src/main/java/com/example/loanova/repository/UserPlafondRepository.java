package com.example.loanova.repository;

import com.example.loanova.entity.User;
import com.example.loanova.entity.UserPlafond;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * USER PLAFOND REPOSITORY - Interface untuk operasi database pada entity
 * UserPlafond.
 */
@Repository
public interface UserPlafondRepository extends JpaRepository<UserPlafond, Long> {

   /**
    * Mencari user plafond terbaru yang aktif dari seorang user. Digunakan untuk
    * menentukan plafond
    * yang dapat digunakan oleh customer.
    *
    * @param user     Entity user
    * @param isActive Status aktif
    * @return Optional UserPlafond
    */
   Optional<UserPlafond> findFirstByUserAndIsActiveTrueOrderByAssignedAtDesc(User user);

   /**
    * Mencari user plafond berdasarkan user dan status aktif.
    *
    * @param user     Entity user
    * @param isActive Status aktif
    * @return Optional UserPlafond
    */
   Optional<UserPlafond> findByUserAndIsActive(User user, Boolean isActive);

   /**
    * Cek apakah plafond tertentu masih digunakan oleh user.
    */
   @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) > 0 FROM user_plafonds WHERE plafond_id = :plafondId", nativeQuery = true)
   boolean existsByPlafondId(@org.springframework.data.repository.query.Param("plafondId") Long plafondId);
}

package com.example.loanova.repository;

import com.example.loanova.entity.User;
import com.example.loanova.entity.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** USER PROFILE REPOSITORY - Interface untuk operasi database pada entity UserProfile. */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

  /**
   * Mencari profil berdasarkan entity User. Karena hubungan OneToOne, satu user hanya memiliki satu
   * profil.
   *
   * @param user Entity user
   * @return Optional UserProfile
   */
  Optional<UserProfile> findByUser(User user);

  /** Cek apakah NIK sudah digunakan. */
  boolean existsByNik(String nik);

  Optional<UserProfile> findByNik(String nik);

  /** Cek apakah Nomor Telepon sudah digunakan. */
  boolean existsByPhoneNumber(String phoneNumber);

  Optional<UserProfile> findByPhoneNumber(String phoneNumber);

  /** Cek apakah NPWP sudah digunakan. */
  boolean existsByNpwpNumber(String npwpNumber);

  Optional<UserProfile> findByNpwpNumber(String npwpNumber);
}

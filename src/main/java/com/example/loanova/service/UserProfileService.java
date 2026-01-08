package com.example.loanova.service;

import com.example.loanova.dto.request.UserProfileCompleteRequest;
import com.example.loanova.dto.request.UserProfileUpdateRequest;
import com.example.loanova.dto.response.UserProfileResponse;
import com.example.loanova.entity.User;
import com.example.loanova.entity.UserProfile;
import com.example.loanova.exception.BusinessException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.UserProfileRepository;
import com.example.loanova.repository.UserRepository;
import com.example.loanova.util.FileStorageUtil;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** USER PROFILE SERVICE - Menangani logika bisnis untuk profil pengguna. */
@Service
@RequiredArgsConstructor
public class UserProfileService {

  private final UserProfileRepository userProfileRepository;
  private final UserRepository userRepository;
  private final FileStorageUtil fileStorageUtil;

  /** LENGKAPI PROFIL - Untuk pengguna role CUSTOMER yang baru mendaftar. */
  @Transactional
  public UserProfileResponse completeProfile(String username, UserProfileCompleteRequest request) {
    // 1. Ambil data User yang sedang login
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

    // 2. Pastikan user belum memiliki profil
    if (userProfileRepository.findByUser(user).isPresent()) {
      throw new BusinessException(
          "Profil sudah dilengkapi. Gunakan fitur update untuk mengubah data.");
    }

    // 3. Validasi keunikan data
    validateUniqueness(request.getNik(), request.getPhoneNumber(), request.getNpwpNumber(), null);

    try {
      // 4. Simpan file-file foto
      String ktpPath = fileStorageUtil.saveFile(request.getKtpPhoto(), "ktp");
      String profilePath = fileStorageUtil.saveFile(request.getProfilePhoto(), "profiles");
      String npwpPath = fileStorageUtil.saveFile(request.getNpwpPhoto(), "npwp");

      // 5. Buat entity UserProfile
      UserProfile userProfile =
          UserProfile.builder()
              .user(user)
              .fullName(request.getFullName())
              .phoneNumber(request.getPhoneNumber())
              .userAddress(request.getUserAddress())
              .nik(request.getNik())
              .birthDate(request.getBirthDate())
              .npwpNumber(request.getNpwpNumber())
              .ktpPhoto(ktpPath)
              .profilePhoto(profilePath)
              .npwpPhoto(npwpPath)
              .build();

      UserProfile savedProfile = userProfileRepository.save(userProfile);
      return toResponse(savedProfile);

    } catch (IOException e) {
      throw new BusinessException("Gagal menyimpan file: " + e.getMessage());
    }
  }

  /** UPDATE PROFIL - Untuk memperbarui data profil yang ada. */
  @Transactional
  public UserProfileResponse updateProfile(String username, UserProfileUpdateRequest request) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

    UserProfile userProfile =
        userProfileRepository
            .findByUser(user)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Profil belum dilengkapi. Silakan lengkapi profil terlebih dahulu."));

    // Validasi keunikan data (kecuali data milik user sendiri)
    validateUniqueness(
        request.getNik(), request.getPhoneNumber(), request.getNpwpNumber(), userProfile.getId());

    // Update data dasar
    userProfile.setFullName(request.getFullName());
    userProfile.setPhoneNumber(request.getPhoneNumber());
    userProfile.setUserAddress(request.getUserAddress());
    userProfile.setNik(request.getNik());
    userProfile.setBirthDate(request.getBirthDate());
    userProfile.setNpwpNumber(request.getNpwpNumber());

    try {
      // Update foto jika ada yang diunggah baru (opsional)
      if (request.getKtpPhoto() != null && !request.getKtpPhoto().isEmpty()) {
        // Hapus file lama sebelum save file baru
        fileStorageUtil.deleteFile(userProfile.getKtpPhoto());
        userProfile.setKtpPhoto(fileStorageUtil.saveFile(request.getKtpPhoto(), "ktp"));
      }
      if (request.getProfilePhoto() != null && !request.getProfilePhoto().isEmpty()) {
        // Hapus file lama sebelum save file baru
        fileStorageUtil.deleteFile(userProfile.getProfilePhoto());
        userProfile.setProfilePhoto(
            fileStorageUtil.saveFile(request.getProfilePhoto(), "profiles"));
      }
      if (request.getNpwpPhoto() != null && !request.getNpwpPhoto().isEmpty()) {
        // Hapus file lama sebelum save file baru
        fileStorageUtil.deleteFile(userProfile.getNpwpPhoto());
        userProfile.setNpwpPhoto(fileStorageUtil.saveFile(request.getNpwpPhoto(), "npwp"));
      }

      UserProfile updatedProfile = userProfileRepository.save(userProfile);
      return toResponse(updatedProfile);

    } catch (IOException e) {
      throw new BusinessException("Gagal menyimpan file: " + e.getMessage());
    }
  }

  /** AMBIL PROFIL SAYA - Mendapatkan data profil user yang sedang login. */
  @Transactional(readOnly = true)
  public UserProfileResponse getMyProfile(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

    UserProfile userProfile =
        userProfileRepository
            .findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Profil belum dilengkapi"));

    return toResponse(userProfile);
  }

  /** Validasi keunikan NIK, Phone, dan NPWP. */
  private void validateUniqueness(
      String nik, String phoneNumber, String npwpNumber, Long currentProfileId) {
    // Check NIK
    userProfileRepository
        .findByNik(nik)
        .ifPresent(
            existing -> {
              if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new BusinessException("NIK sudah digunakan oleh pengguna lain");
              }
            });

    // Check Phone
    userProfileRepository
        .findByPhoneNumber(phoneNumber)
        .ifPresent(
            existing -> {
              if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new BusinessException("Nomor telepon sudah digunakan oleh pengguna lain");
              }
            });

    // Check NPWP
    userProfileRepository
        .findByNpwpNumber(npwpNumber)
        .ifPresent(
            existing -> {
              if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new BusinessException("Nomor NPWP sudah digunakan oleh pengguna lain");
              }
            });
  }

  /** Mapper Entity to Response DTO. */
  private UserProfileResponse toResponse(UserProfile profile) {
    return UserProfileResponse.builder()
        .id(profile.getId())
        .userId(profile.getUser().getId())
        .username(profile.getUser().getUsername())
        .fullName(profile.getFullName())
        .phoneNumber(profile.getPhoneNumber())
        .userAddress(profile.getUserAddress())
        .nik(profile.getNik())
        .birthDate(profile.getBirthDate())
        .npwpNumber(profile.getNpwpNumber())
        .ktpPhoto(profile.getKtpPhoto())
        .profilePhoto(profile.getProfilePhoto())
        .npwpPhoto(profile.getNpwpPhoto())
        .createdAt(profile.getCreatedAt())
        .updatedAt(profile.getUpdatedAt())
        .build();
  }
}

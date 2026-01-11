package com.example.loanova.service;

import com.example.loanova.dto.request.AssignUserPlafondRequest;
import com.example.loanova.dto.response.UserPlafondResponse;
import com.example.loanova.entity.Plafond;
import com.example.loanova.entity.User;
import com.example.loanova.entity.UserPlafond;
import com.example.loanova.exception.BusinessException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.PlafondRepository;
import com.example.loanova.repository.UserPlafondRepository;
import com.example.loanova.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * USER PLAFOND SERVICE Menangani logika bisnis untuk manajemen plafond user.
 */
@Service
@RequiredArgsConstructor
public class UserPlafondService {

   private final UserPlafondRepository userPlafondRepository;
   private final UserRepository userRepository;
   private final PlafondRepository plafondRepository;

   /**
    * ASSIGN PLAFOND KE USER Digunakan oleh SUPERADMIN untuk assign plafond baru ke
    * user. Logic: 1.
    * Validasi user dan plafond exist 2. Nonaktifkan plafond lama yang masih aktif
    * (jika ada) 3.
    * Create user plafond baru dengan status aktif
    *
    * @param request AssignUserPlafondRequest dengan userId, plafondId, dan
    *                maxAmount
    * @return UserPlafondResponse dengan data plafond yang baru di-assign
    */
   @Transactional
   public UserPlafondResponse assignPlafondToUser(AssignUserPlafondRequest request) {
      // 1. Validasi user exist
      User user = userRepository
            .findById(request.getUserId())
            .orElseThrow(
                  () -> new ResourceNotFoundException(
                        "User dengan ID " + request.getUserId() + " tidak ditemukan"));

      // 2. Validasi plafond exist
      Plafond plafond = plafondRepository
            .findById(request.getPlafondId())
            .orElseThrow(
                  () -> new ResourceNotFoundException(
                        "Plafond dengan ID " + request.getPlafondId() + " tidak ditemukan"));

      // 3. Validasi maxAmount tidak melebihi max amount dari plafond
      if (request.getMaxAmount().compareTo(plafond.getMaxAmount()) > 0) {
         throw new BusinessException(
               "Max amount yang diberikan ("
                     + request.getMaxAmount()
                     + ") melebihi max amount plafond "
                     + plafond.getName()
                     + " ("
                     + plafond.getMaxAmount()
                     + ")");
      }

      // 4. Nonaktifkan plafond lama yang masih aktif (jika ada)
      userPlafondRepository
            .findByUserAndIsActive(user, true)
            .ifPresent(
                  oldPlafond -> {
                     oldPlafond.setIsActive(false);
                     userPlafondRepository.save(oldPlafond);
                  });

      // 5. Create user plafond baru
      UserPlafond userPlafond = UserPlafond.builder()
            .user(user)
            .plafond(plafond)
            .maxAmount(request.getMaxAmount())
            .remainingAmount(request.getMaxAmount()) // Awal sama dengan maxAmount
            .isActive(true)
            .build();

      UserPlafond savedPlafond = userPlafondRepository.save(userPlafond);
      return toResponse(savedPlafond);
   }

   /**
    * GET ACTIVE USER PLAFOND Mendapatkan plafond aktif dari user. User hanya bisa
    * memiliki 1
    * plafond aktif pada satu waktu.
    *
    * @param userId ID user
    * @return UserPlafondResponse
    */
   @Transactional(readOnly = true)
   public UserPlafondResponse getActiveUserPlafond(Long userId) {
      User user = userRepository
            .findById(userId)
            .orElseThrow(
                  () -> new ResourceNotFoundException("User dengan ID " + userId + " tidak ditemukan"));

      UserPlafond userPlafond = userPlafondRepository
            .findByUserAndIsActive(user, true)
            .orElseThrow(
                  () -> new ResourceNotFoundException(
                        "User tidak memiliki plafond aktif. Silakan assign plafond terlebih dahulu"));

      return toResponse(userPlafond);
   }

   /** Mapper Entity to Response DTO. */
   private UserPlafondResponse toResponse(UserPlafond userPlafond) {
      return UserPlafondResponse.builder()
            .id(userPlafond.getId())
            .userId(userPlafond.getUser().getId())
            .username(userPlafond.getUser().getUsername())
            .plafondId(userPlafond.getPlafond().getId())
            .plafondName(userPlafond.getPlafond().getName())
            .maxAmount(userPlafond.getMaxAmount())
            .remainingAmount(userPlafond.getRemainingAmount())
            .isActive(userPlafond.getIsActive())
            .assignedAt(userPlafond.getAssignedAt())
            .build();
   }
}

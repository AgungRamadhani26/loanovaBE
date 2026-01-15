package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.request.AssignUserPlafondRequest;
import com.example.loanova.dto.response.UserPlafondResponse;
import com.example.loanova.service.UserPlafondService;
import com.example.loanova.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * USER PLAFOND CONTROLLER Endpoint untuk mengelola assignment plafond ke user.
 * Hanya SUPERADMIN
 * yang dapat assign plafond ke user.
 */
@RestController
@RequestMapping("/api/user-plafonds")
@RequiredArgsConstructor
public class UserPlafondController {

   private final UserPlafondService userPlafondService;

   /**
    * ASSIGN PLAFOND KE USER Endpoint untuk SUPERADMIN assign plafond baru ke user.
    * Plafond lama
    * yang masih aktif akan otomatis dinonaktifkan.
    */
   // Yang bisa akses hanya SUPERADMIN dan BACKOFFICE
   @PreAuthorize("hasAuthority('USER_PLAFOND:ASSIGN')")
   @PostMapping("/assign")
   public ResponseEntity<ApiResponse<UserPlafondResponse>> assignPlafond(
         @Valid @RequestBody AssignUserPlafondRequest request) {
      UserPlafondResponse response = userPlafondService.assignPlafondToUser(request);
      return ResponseUtil.created(response, "Plafond berhasil di-assign ke user");
   }

   /**
    * GET ACTIVE USER PLAFOND Mendapatkan plafond aktif dari user tertentu.
    * SUPERADMIN dan BACKOFFICE
    * bisa melihat plafond user lain.
    */
   // Yang bisa akses hanya SUPERADMIN dan BACKOFFICE
   @PreAuthorize("hasAuthority('USER_PLAFOND:READ')")
   @GetMapping("/users/{userId}/active")
   public ResponseEntity<ApiResponse<UserPlafondResponse>> getActiveUserPlafond(
         @PathVariable Long userId) {
      UserPlafondResponse response = userPlafondService.getActiveUserPlafond(userId);
      return ResponseUtil.ok(response, "Berhasil mengambil data plafond user");
   }
}

package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.request.UserProfileCompleteRequest;
import com.example.loanova.dto.request.UserProfileUpdateRequest;
import com.example.loanova.dto.response.UserProfileResponse;
import com.example.loanova.service.UserProfileService;
import com.example.loanova.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** USER PROFILE CONTROLLER - Endpoint untuk mengelola profil pengguna (khusus CUSTOMER). */
@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserProfileService userProfileService;

  /** LENGKAPI PROFIL Khusus role CUSTOMER. Menggunakan multipart/form-data untuk unggahn file. */
  @PreAuthorize("hasRole('CUSTOMER')")
  @PostMapping(value = "/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<UserProfileResponse>> completeProfile(
      @Valid @ModelAttribute UserProfileCompleteRequest request, Authentication authentication) {
    UserProfileResponse response =
        userProfileService.completeProfile(authentication.getName(), request);
    return ResponseUtil.created(response, "Profil berhasil dilengkapi");
  }

  /** UPDATE PROFIL Memperbarui data profil yang ada. */
  @PreAuthorize("hasRole('CUSTOMER')")
  @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
      @Valid @ModelAttribute UserProfileUpdateRequest request, Authentication authentication) {
    UserProfileResponse response =
        userProfileService.updateProfile(authentication.getName(), request);
    return ResponseUtil.ok(response, "Profil berhasil diperbarui");
  }

  /** AMBIL PROFIL SAYA */
  @PreAuthorize("hasRole('CUSTOMER')")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
      Authentication authentication) {
    UserProfileResponse response = userProfileService.getMyProfile(authentication.getName());
    return ResponseUtil.ok(response, "Berhasil mengambil data profil");
  }
}

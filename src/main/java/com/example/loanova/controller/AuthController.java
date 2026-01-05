package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.request.ChangePasswordRequest;
import com.example.loanova.dto.request.ForgotPasswordRequest;
import com.example.loanova.dto.request.LoginRequest;
import com.example.loanova.dto.request.RefreshTokenRequest;
import com.example.loanova.dto.request.RegisterRequest;
import com.example.loanova.dto.request.ResetPasswordRequest;
import com.example.loanova.dto.response.AuthResponse;
import com.example.loanova.dto.response.RegisterResponse;
import com.example.loanova.service.AuthService;
import com.example.loanova.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * REGISTER ENDPOINT
   *
   * <p>Endpoint untuk pendaftaran customer baru
   *
   * <p>Request: POST /api/auth/register Body: { "username": "...", "email": "...", "password":
   * "..." }
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    RegisterResponse registerResponse = authService.register(request);
    return ResponseUtil.success(registerResponse, "Pendaftaran berhasil", HttpStatus.CREATED);
  }

  /**
   * LOGIN ENDPOINT
   *
   * <p>Endpoint untuk login user
   *
   * <p>Request: POST /api/auth/login Body: { "username": "...", "password": "..." }
   *
   * <p>Response: { "success": true, "message": "Login berhasil", "data": { "accessToken":
   * "eyJhbGc...", "refreshToken": "abc123...", "type": "Bearer", "username": "SUMUT01", "roles":
   * ["ADMIN"] } }
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse authResponse = authService.login(request);
    return ResponseUtil.success(authResponse, "Login berhasil", HttpStatus.OK);
  }

  /**
   * REFRESH TOKEN ENDPOINT
   *
   * <p>Endpoint untuk generate access token baru pakai refresh token
   *
   * <p>Flow: 1. Access token expired (15 menit) 2. Frontend detect 401 error 3. Frontend call POST
   * /api/auth/refresh dengan refresh token 4. Backend validate refresh token (signature,
   * expiration) 5. Backend generate access token baru 6. Frontend pakai access token baru
   *
   * <p>Request: POST /api/auth/refresh Body: { "refreshToken": "abc123..." }
   *
   * <p>Response Success: { "success": true, "message": "Token berhasil di-refresh", "data": {
   * "accessToken": "eyJhbGc...", // Access token BARU "refreshToken": "abc123...", // Refresh token
   * SAMA (tidak perlu generate ulang) "type": "Bearer", "username": "SUMUT01", "roles": ["ADMIN"] }
   * }
   *
   * <p>Response Error (401): - Refresh token invalid/expired - User tidak ditemukan
   */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    AuthResponse authResponse = authService.refreshAccessToken(request.getRefreshToken());
    return ResponseUtil.success(authResponse, "Token berhasil di-refresh", HttpStatus.OK);
  }

  /**
   * LOGOUT ENDPOINT
   *
   * <p>Request: POST /api/auth/logout Header: Authorization: Bearer <accessToken> Body: {
   * "refreshToken": "..." }
   */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody RefreshTokenRequest request) {
    String accessToken = authHeader.substring(7);
    authService.logout(accessToken, request.getRefreshToken());
    return ResponseUtil.success(null, "Logout berhasil", HttpStatus.OK);
  }

  /** ENDPOINT LUPA KATA SANDI */
  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request.getEmail());
    return ResponseUtil.success(
        null, "Link reset password telah dikirim ke email Anda", HttpStatus.OK);
  }

  /** ENDPOINT RESET KATA SANDI */
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.getToken(), request.getNewPassword());
    return ResponseUtil.success(null, "Password berhasil diubah", HttpStatus.OK);
  }

  /** ENDPOINT GANTI PASSWORD (SAAT LOGIN) */
  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody ChangePasswordRequest request) {

    // Ambil username dari context (user yang sedang login)
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // Ambil raw access token (hilangkan "Bearer ")
    String accessToken = authHeader.substring(7);

    authService.changePassword(username, accessToken, request);

    return ResponseUtil.success(
        null, "Password berhasil diubah. Silakan login kembali.", HttpStatus.OK);
  }
}

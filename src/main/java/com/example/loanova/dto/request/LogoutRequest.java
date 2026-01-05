package com.example.loanova.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOGOUT REQUEST DTO
 *
 * <p>DTO untuk request logout
 *
 * <p>Flow logout: 1. User click logout button 2. Frontend kirim POST /api/auth/logout dengan: -
 * Access token di header Authorization - Refresh token di request body 3. Backend blacklist both
 * tokens di Redis 4. Token tidak bisa dipakai lagi (rejected di filter)
 *
 * <p>Kenapa perlu refresh token di body? - Access token ada di header (auto-sent via interceptor) -
 * Refresh token harus dikirim manual dari frontend - Perlu blacklist BOTH tokens untuk security
 * maksimal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

  /**
   * Refresh token yang mau di-blacklist
   *
   * <p>Validasi: - @NotBlank: tidak boleh null, empty, atau whitespace
   *
   * <p>Note: - Access token diambil dari header Authorization di controller - Refresh token diambil
   * dari request body ini
   */
  @NotBlank(message = "Refresh token tidak boleh kosong")
  private String refreshToken;
}

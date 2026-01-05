package com.example.loanova.dto.request;

import com.example.loanova.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
  @NotBlank(message = "Token wajib diisi")
  private String token;

  @NotBlank(message = "Password baru wajib diisi")
  @StrongPassword
  private String newPassword;
}

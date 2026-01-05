package com.example.loanova.dto.request;

import com.example.loanova.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  @NotBlank(message = "Username tidak boleh kosong")
  @Size(min = 3, max = 50, message = "Username harus antara 3 sampai 50 karakter")
  private String username;

  @NotBlank(message = "Email tidak boleh kosong")
  @Email(message = "Format email tidak valid")
  @Size(max = 100, message = "Email maksimal 100 karakter")
  private String email;

  @NotBlank(message = "Password tidak boleh kosong")
  @StrongPassword
  private String password;
}

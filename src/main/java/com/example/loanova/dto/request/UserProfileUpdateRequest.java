package com.example.loanova.dto.request;

import com.example.loanova.validation.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

/**
 * USER PROFILE UPDATE REQUEST - DTO khusus untuk memperbarui profil. Data pribadi tetap divalidasi,
 * namun foto bersifat OPSIONAL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

  @NotBlank(message = "Nama lengkap wajib diisi")
  @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
  private String fullName;

  @NotBlank(message = "Nomor telepon wajib diisi")
  @Size(max = 20, message = "Nomor telepon maksimal 20 karakter")
  private String phoneNumber;

  @NotBlank(message = "Alamat wajib diisi")
  private String userAddress;

  @NotBlank(message = "NIK wajib diisi")
  @Size(min = 16, max = 16, message = "NIK harus 16 karakter")
  private String nik;

  @NotNull(message = "Tanggal lahir wajib diisi")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate birthDate;

  @NotBlank(message = "Nomor NPWP wajib diisi")
  @Size(max = 16, message = "Nomor NPWP maksimal 16 karakter")
  private String npwpNumber;

  // Foto-foto bersifat opsional saat update, tapi tetap divalidasi jika diunggah
  @ValidFile(required = false)
  private MultipartFile ktpPhoto;

  @ValidFile(required = false)
  private MultipartFile profilePhoto;

  @ValidFile(required = false)
  private MultipartFile npwpPhoto;
}

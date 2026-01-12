package com.example.loanova.dto.request;

import com.example.loanova.validation.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * LOAN APPLICATION REQUEST DTO untuk pengajuan pinjaman baru oleh CUSTOMER. Beberapa field
 * snapshot akan diambil otomatis dari user profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {

  @NotBlank(message = "Branch ID wajib diisi")
  private String branchId;

  @NotBlank(message = "Plafond ID wajib diisi")
  private String plafondId;

  @NotBlank(message = "Jumlah pinjaman wajib diisi")
  private String amount;

  @NotBlank(message = "Tenor wajib diisi")
  private String tenor;

  @NotBlank(message = "Pekerjaan wajib diisi")
  @Size(max = 50, message = "Pekerjaan maksimal 50 karakter")
  private String occupation;

  @Size(max = 50, message = "Nama perusahaan maksimal 50 karakter")
  private String companyName;

  @NotBlank(message = "Nomor rekening wajib diisi")
  @Size(max = 50, message = "Nomor rekening maksimal 50 karakter")
  private String rekeningNumber;

  // Dokumen yang wajib
  @ValidFile(message = "Foto cover buku tabungan wajib diunggah", required = true)
  private MultipartFile savingBookCover;

  @ValidFile(message = "Foto slip gaji wajib diunggah", required = true)
  private MultipartFile payslipPhoto;
}

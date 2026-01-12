package com.example.loanova.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOAN APPLICATION RESPONSE DTO untuk mengirimkan data loan application ke
 * client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {
  private Long id;
  private Long userId;
  private String username;
  private Long branchId;
  private String branchCode;
  private Long plafondId;
  private String plafondName;
  private BigDecimal amount;
  private Integer tenor;
  private String status;
  private LocalDateTime submittedAt;

  // Snapshot data pribadi
  private String fullNameSnapshot;
  private String phoneNumberSnapshot;
  private String userAddressSnapshot;
  private String nikSnapshot;
  private LocalDate birthDateSnapshot;
  private String npwpNumberSnapshot;

  // Data pekerjaan
  private String occupation;
  private String companyName;

  // Data keuangan
  private String rekeningNumber;

  // Dokumen foto
  private String ktpPhotoSnapshot;
  private String npwpPhotoSnapshot;
  private String savingBookCover;
  private String payslipPhoto;
}

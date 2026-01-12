package com.example.loanova.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOAN REVIEW REQUEST DTO untuk review pinjaman oleh MARKETING atau BRANCH_MANAGER. Berisi action
 * (PROCEED/APPROVE/REJECT) dan komentar opsional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReviewRequest {

  @NotBlank(message = "Action wajib diisi (PROCEED/APPROVE/REJECT)")
  private String action; // PROCEED, APPROVE, REJECT

  private String comment; // Opsional, tapi wajib jika REJECT
}

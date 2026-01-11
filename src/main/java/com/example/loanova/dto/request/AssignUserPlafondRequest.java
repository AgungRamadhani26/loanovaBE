package com.example.loanova.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ASSIGN USER PLAFOND REQUEST DTO untuk request assign plafond ke user oleh
 * SUPERADMIN. Request
 * ini berisi user ID, plafond ID, dan custom max amount yang akan diberikan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserPlafondRequest {

   @NotNull(message = "User ID wajib diisi")
   private Long userId;

   @NotNull(message = "Plafond ID wajib diisi")
   private Long plafondId;

   @NotNull(message = "Max amount wajib diisi")
   @Positive(message = "Max amount harus lebih besar dari 0")
   private BigDecimal maxAmount;
}

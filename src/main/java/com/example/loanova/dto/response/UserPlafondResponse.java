package com.example.loanova.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** USER PLAFOND RESPONSE DTO untuk mengirimkan data user plafond ke client. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPlafondResponse {
   private Long id;
   private Long userId;
   private String username;
   private Long plafondId;
   private String plafondName;
   private BigDecimal maxAmount;
   private BigDecimal remainingAmount;
   private Boolean isActive;
   private LocalDateTime assignedAt;
}

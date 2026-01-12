package com.example.loanova.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** APPLICATION HISTORY RESPONSE DTO untuk mengirimkan data history ke client. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationHistoryResponse {
  private Long id;
  private Long loanApplicationId;
  private Long actionByUserId;
  private String actionByUsername;
  private String actionByRole;
  private String status;
  private String comment;
  private LocalDateTime createdAt;
}

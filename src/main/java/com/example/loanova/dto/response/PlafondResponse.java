package com.example.loanova.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlafondResponse {
  private Long id;
  private String name;
  private String description;
  private BigDecimal maxAmount;
  private BigDecimal interestRate;
  private Integer tenorMin;
  private Integer tenorMax;
}

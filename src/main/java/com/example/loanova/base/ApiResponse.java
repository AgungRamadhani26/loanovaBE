package com.example.loanova.base;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  private Boolean success;
  private String message;
  private T data;
  private Integer code;
  private Instant timestamp;
}

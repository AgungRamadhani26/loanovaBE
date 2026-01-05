package com.example.loanova.dto.response;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
  private Long id;
  private String username;
  private String email;
  private Set<String> roles;
  private Boolean isActive;
}

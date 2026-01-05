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
public class AuthResponse {

  private String accessToken;

  private String refreshToken;

  private String type; // Bearer

  private String username;

  private Set<String> roles;
}

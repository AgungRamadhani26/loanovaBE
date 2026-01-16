package com.example.loanova.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Data;

@Data
public class RoleUpdateRequest {
  @NotBlank(message = "Role description wajib diisi")
  @Size(max = 255, message = "Role description maksimal 255 karakter")
  private String roleDescription;

  private Set<Integer> permissionIds;
}

package com.example.loanova.dto.request;

import com.example.loanova.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Password lama wajib diisi")
    private String currentPassword;

    @NotBlank(message = "Password baru wajib diisi")
    @StrongPassword
    private String newPassword;
}

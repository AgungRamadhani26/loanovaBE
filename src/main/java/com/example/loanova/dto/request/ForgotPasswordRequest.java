package com.example.loanova.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Email harus valid")
    private String email;
}

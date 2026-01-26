package com.example.loanova.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PushNotificationRequest {

    @NotBlank(message = "Username tidak boleh kosong")
    private String username;

    @NotBlank(message = "Title tidak boleh kosong")
    private String title;

    @NotBlank(message = "Message tidak boleh kosong")
    private String message;
}

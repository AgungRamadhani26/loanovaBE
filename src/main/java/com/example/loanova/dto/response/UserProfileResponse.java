package com.example.loanova.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * USER PROFILE RESPONSE - DTO untuk mengirimkan data profil pengguna ke client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String userAddress;
    private String nik;
    private LocalDate birthDate;
    private String npwpNumber;
    private String ktpPhoto;
    private String profilePhoto;
    private String npwpPhoto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

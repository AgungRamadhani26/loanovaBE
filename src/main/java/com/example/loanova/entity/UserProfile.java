package com.example.loanova.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

/**
 * USER PROFILE ENTITY Represents user profile information including personal
 * data and document
 * photos. This entity doesn't use soft delete since user profiles should never
 * be deleted, only
 * updated for data accuracy and compliance purposes.
 */
@Entity
@Table(name = "users_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Column(name = "phone_number", nullable = false, unique = true, length = 20)
  private String phoneNumber;

  @Column(name = "user_address", nullable = false)
  private String userAddress;

  @Column(name = "nik", nullable = false, unique = true, length = 16)
  private String nik;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "npwp_number", nullable = false, unique = true, length = 16)
  private String npwpNumber;

  @Column(name = "ktp_photo", nullable = false, length = 255)
  private String ktpPhoto;

  @Column(name = "profile_photo", nullable = false, length = 255)
  private String profilePhoto;

  @Column(name = "npwp_photo", nullable = false, length = 255)
  private String npwpPhoto;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}

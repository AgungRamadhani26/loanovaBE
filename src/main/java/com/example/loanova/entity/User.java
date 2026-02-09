package com.example.loanova.entity;

import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

  @Column(name = "username", nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "password", nullable = true, length = 255) // Nullable untuk Google users
  private String password;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "branch_id", nullable = true)
  private Branch branch;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @Column(name = "fcm_token")
  private String fcmToken;

  /**
   * Google User ID dari Firebase Authentication. Digunakan untuk account linking dan identifikasi
   * user Google.
   */
  @Column(name = "google_id", unique = true)
  private String googleId;

  /**
   * Provider autentikasi: LOCAL (email/password) atau GOOGLE (Google Sign-In). Default adalah LOCAL
   * untuk backward compatibility.
   */
  @Column(name = "auth_provider", length = 20)
  @Builder.Default
  private String authProvider = "LOCAL";

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles;
}

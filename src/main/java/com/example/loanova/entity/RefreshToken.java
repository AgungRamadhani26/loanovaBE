package com.example.loanova.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   private Long id;

   @Column(name = "token", nullable = false, unique = true, length = 500)
   private String token;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id", nullable = false)
   private User user;

   @Column(name = "expiry_date", nullable = false)
   private LocalDateTime expiryDate;

   @Column(name = "created_at", nullable = false, updatable = false)
   private LocalDateTime createdAt;

   @Column(name = "revoked_at")
   private LocalDateTime revokedAt;

   @PrePersist
   protected void onCreate() {
      createdAt = LocalDateTime.now();
   }

   /**
    * Check apakah refresh token sudah expired
    */
   public boolean isExpired() {
      return LocalDateTime.now().isAfter(expiryDate);
   }

   /**
    * Check apakah refresh token sudah di-revoke
    */
   public boolean isRevoked() {
      return revokedAt != null;
   }

   /**
    * Check apakah refresh token masih valid (tidak expired dan tidak revoked)
    */
   public boolean isValid() {
      return !isExpired() && !isRevoked();
   }

   /**
    * Revoke refresh token (untuk logout)
    */
   public void revoke() {
      this.revokedAt = LocalDateTime.now();
   }
}

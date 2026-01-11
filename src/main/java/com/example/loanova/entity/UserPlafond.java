package com.example.loanova.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/**
 * USER PLAFOND ENTITY Merepresentasikan mapping antara User (CUSTOMER) dengan
 * Plafond yang
 * diberikan. Entity ini menyimpan informasi plafond yang diberikan kepada
 * customer.
 *
 * <p>
 * Relasi: - User (One) -> UserPlafond (Many): Satu user bisa memiliki banyak
 * plafond - Plafond
 * (One) -> UserPlafond (Many): Satu paket plafond bisa diberikan ke banyak user
 */
@Entity
@Table(name = "user_plafond")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPlafond {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "user_id", nullable = false)
   private User user;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "plafond_id", nullable = false)
   private Plafond plafond;

   @Column(name = "max_amount", nullable = false, precision = 18, scale = 2)
   private BigDecimal maxAmount;

   @Column(name = "remaining_amount", nullable = false, precision = 18, scale = 2)
   private BigDecimal remainingAmount;

   @Column(name = "is_active", nullable = false)
   private Boolean isActive;

   @Column(name = "assigned_at", nullable = false, updatable = false)
   private LocalDateTime assignedAt;

   @PrePersist
   protected void onCreate() {
      assignedAt = LocalDateTime.now();
   }
}

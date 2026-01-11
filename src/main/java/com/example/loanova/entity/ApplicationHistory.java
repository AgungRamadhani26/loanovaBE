package com.example.loanova.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * APPLICATION HISTORY ENTITY Merepresentasikan history/tracking perubahan
 * status dari loan
 * application. Setiap kali status aplikasi berubah, akan tercatat di tabel ini
 * siapa yang
 * mengubah, kapan, dan komentar apa.
 *
 * <p>
 * Relasi: - LoanApplication (One) -> ApplicationHistory (Many): Satu aplikasi
 * punya banyak
 * history
 */
@Entity
@Table(name = "application_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationHistory {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "loan_application_id", nullable = false)
   private LoanApplication loanApplication;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "action_by_user_id", nullable = false)
   private User actionByUser;

   @Column(name = "status", nullable = false, length = 30)
   private String status;

   @Column(name = "comment", columnDefinition = "TEXT")
   private String comment;

   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt;

   @Column(name = "action_by_role", nullable = false, length = 15)
   private String actionByRole;

   @PrePersist
   protected void onCreate() {
      if (createdAt == null) {
         createdAt = LocalDateTime.now();
      }
   }
}

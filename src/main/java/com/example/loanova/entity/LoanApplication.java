package com.example.loanova.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

/**
 * LOAN APPLICATION ENTITY Merepresentasikan pengajuan pinjaman dari customer.
 * Entity ini menyimpan
 * data aplikasi pinjaman beserta snapshot data customer pada saat pengajuan
 * untuk keperluan audit
 * dan tracking perubahan data.
 *
 * <p>
 * Relasi: - Branch (One) -> LoanApplication (Many): Satu branch mengelola
 * banyak aplikasi - User
 * (One) -> LoanApplication (Many): Satu user (customer) bisa mengajukan banyak
 * pinjaman - Plafond
 * (One) -> LoanApplication (Many): Satu paket plafond bisa digunakan untuk
 * banyak aplikasi -
 * LoanApplication (One) -> ApplicationHistory (Many): Satu aplikasi punya
 * banyak history status
 */
@Entity
@Table(name = "loan_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "user_id", nullable = false)
   private User user;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "branch_id", nullable = false)
   private Branch branch;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "plafond_id", nullable = false)
   private Plafond plafond;

   @Column(name = "amount", nullable = false, precision = 18, scale = 2)
   private BigDecimal amount;

   @Column(name = "tenor", nullable = false)
   private Integer tenor;

   @Column(name = "status", nullable = false, length = 30)
   private String status;

   @Column(name = "submitted_at", nullable = false)
   private LocalDateTime submittedAt;

   // SNAPSHOT DATA PRIBADI - Data customer saat pengajuan
   @Column(name = "full_name_snapshot", nullable = false, length = 100)
   private String fullNameSnapshot;

   @Column(name = "phone_number_snapshot", nullable = false, length = 20)
   private String phoneNumberSnapshot;

   @Column(name = "user_address_snapshot", nullable = false)
   private String userAddressSnapshot;

   @Column(name = "nik_snapshot", nullable = false, length = 16)
   private String nikSnapshot;

   @Column(name = "birth_date_snapshot", nullable = false)
   private LocalDate birthDateSnapshot;

   @Column(name = "npwp_number_snapshot", length = 16)
   private String npwpNumberSnapshot;

   // DATA PEKERJAAN
   @Column(name = "occupation", nullable = false, length = 50)
   private String occupation;

   @Column(name = "company_name", length = 50)
   private String companyName;

   // DATA KEUANGAN
   @Column(name = "rekening_number", nullable = false, length = 50)
   private String rekeningNumber;

   // DOKUMEN FOTO - Snapshot foto saat pengajuan
   @Column(name = "ktp_photo_snapshot", nullable = false, length = 255)
   private String ktpPhotoSnapshot;

   @Column(name = "spouse_ktp_photo", length = 255)
   private String spouseKtpPhoto;

   @Column(name = "npwp_photo_snapshot", length = 255)
   private String npwpPhotoSnapshot;

   @Column(name = "saving_book_cover", nullable = false, length = 255)
   private String savingBookCover;

   @Column(name = "payslip_photo", nullable = false, length = 255)
   private String payslipPhoto;

   @Column(name = "marriage_certificate_photo", length = 255)
   private String marriageCertificatePhoto;

   @Column(name = "employee_id_photo", length = 255)
   private String employeeIdPhoto;

   @PrePersist
   protected void onCreate() {
      if (submittedAt == null) {
         submittedAt = LocalDateTime.now();
      }
   }
}

package com.example.loanova.entity;

/**
 * LOAN APPLICATION STATUS ENUM Status-status yang dapat dimiliki oleh loan
 * application selama
 * proses pengajuan hingga pencairan.
 */
public enum LoanApplicationStatus {
   PENDING_REVIEW, // Status awal setelah customer submit
   WAITING_APPROVAL, // Setelah marketing proceed
   WAITING_DISBURSEMENT, // Setelah branch manager approve
   DISBURSED, // Setelah backoffice melakukan pencairan
   REJECTED // Jika ditolak oleh marketing atau branch manager
}

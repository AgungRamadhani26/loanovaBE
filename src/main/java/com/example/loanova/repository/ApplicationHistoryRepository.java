package com.example.loanova.repository;

import com.example.loanova.entity.ApplicationHistory;
import com.example.loanova.entity.LoanApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * APPLICATION HISTORY REPOSITORY - Interface untuk operasi database pada entity
 * ApplicationHistory.
 */
@Repository
public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {

  /**
   * Mencari semua history dari loan application tertentu, diurutkan dari yang terbaru.
   */
  List<ApplicationHistory> findByLoanApplicationOrderByCreatedAtDesc(LoanApplication loanApplication);
}

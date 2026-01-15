package com.example.loanova.repository;

import com.example.loanova.entity.LoanApplication;
import com.example.loanova.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** LOAN APPLICATION REPOSITORY - Interface untuk operasi database pada entity LoanApplication. */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

  /**
   * Cek apakah user memiliki pinjaman yang sedang diproses (belum selesai). Pinjaman dianggap
   * selesai jika statusnya DISBURSED atau REJECTED.
   */
  @Query(
      "SELECT CASE WHEN COUNT(la) > 0 THEN true ELSE false END "
          + "FROM LoanApplication la "
          + "WHERE la.user = :user "
          + "AND la.status NOT IN ('DISBURSED', 'REJECTED')")
  boolean existsActiveApplicationByUser(@Param("user") User user);

  /**
   * Mencari loan application berdasarkan user dan status tertentu.
   */
  List<LoanApplication> findByUserAndStatus(User user, String status);

  /**
   * Mencari loan application berdasarkan status untuk branch tertentu.
   */
  @Query(
      "SELECT la FROM LoanApplication la "
          + "WHERE la.status = :status "
          + "AND la.branch.id = :branchId "
          + "ORDER BY la.submittedAt ASC")
  List<LoanApplication> findByStatusAndBranch(
      @Param("status") String status, @Param("branchId") Long branchId);

  /**
   * Mencari loan application berdasarkan status (untuk BACKOFFICE yang bisa lihat semua branch).
   */
  List<LoanApplication> findByStatusOrderBySubmittedAtAsc(String status);

  /**
   * Mencari semua loan application dari user tertentu.
   */
  List<LoanApplication> findByUserOrderBySubmittedAtDesc(User user);

  /**
   * Mencari loan application berdasarkan ID dan branch (untuk validasi akses MARKETING/BRANCHMANAGER).
   */
  Optional<LoanApplication> findByIdAndBranchId(Long id, Long branchId);
}

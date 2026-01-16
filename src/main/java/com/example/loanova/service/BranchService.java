package com.example.loanova.service;

import com.example.loanova.dto.request.BranchRequest;
import com.example.loanova.dto.response.BranchResponse;
import com.example.loanova.entity.Branch;
import com.example.loanova.exception.BusinessException;
import com.example.loanova.exception.DuplicateResourceException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.BranchRepository;
import com.example.loanova.repository.LoanApplicationRepository;
import com.example.loanova.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BranchService {

  private final BranchRepository branchRepository;
  private final UserRepository userRepository;
  private final LoanApplicationRepository loanApplicationRepository;

  public BranchService(
      BranchRepository branchRepository,
      UserRepository userRepository,
      LoanApplicationRepository loanApplicationRepository) {
    this.branchRepository = branchRepository;
    this.userRepository = userRepository;
    this.loanApplicationRepository = loanApplicationRepository;
  }

  /*
   * Mendapatkan semua Branch yang ada di sistem (auto exclude deleted via @Where)
   */
  public List<BranchResponse> getAllBranches() {
    return branchRepository.findAll().stream().map(this::toResponse).toList();
  }

  /* Menambahkan Branch baru ke dalam sistem */
  public BranchResponse createBranch(BranchRequest request) {
    /*
     * Pengecekan jika data baru mempunyai branchCode yang sama dengan yang sudah
     * terdaftar pada sistem (Active)
     */
    if (branchRepository.existsByBranchCode(request.getBranchCode())) {
      throw new DuplicateResourceException(
          "Branch code " + request.getBranchCode() + " sudah digunakan");
    }

    /*
     * Pengecekan jika data baru mempunyai branchCode yang sama dengan yang sudah
     * terdaftar pada sistem (Deleted/Any Status)
     */
    if (branchRepository.existsByBranchCodeAnyStatus(request.getBranchCode())) {
      throw new DuplicateResourceException(
          "Branch code "
              + request.getBranchCode()
              + " sudah dihapus namun masih tersimpan di sistem, silahkan restore data jika ingin mengembalikannya.");
    }

    /*
     * Pengecekan Branch Name (Active)
     */
    if (branchRepository.existsByBranchName(request.getBranchName())) {
      throw new DuplicateResourceException(
          "Branch name " + request.getBranchName() + " sudah digunakan");
    }

    /*
     * Pengecekan Branch Name (Deleted/Any Status)
     */
    if (branchRepository.existsByBranchNameAnyStatus(request.getBranchName())) {
      throw new DuplicateResourceException(
          "Branch name "
              + request.getBranchName()
              + " sudah dihapus namun masih tersimpan di sistem, silahkan restore data jika ingin mengembalikannya.");
    }

    Branch branch =
        Branch.builder()
            .branchCode(request.getBranchCode())
            .branchName(request.getBranchName())
            .address(request.getAddress())
            .build();
    return toResponse(branchRepository.save(branch));
  }

  /* Restore Branch yang sudah di soft-delete */
  public BranchResponse restoreBranch(Long id) {
    Branch branch =
        branchRepository
            .findByIdIncludeDeleted(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Maaf, tidak ada data branch dengan id " + id));

    branch.restore();
    return toResponse(branchRepository.save(branch));
  }

  /* Mengupdate data branch */
  public BranchResponse updateBranch(Long id, BranchRequest request) {
    Branch branch =
        branchRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Maaf, tidak ada data branch dengan id " + id));
    if (!branch.getBranchCode().equals(request.getBranchCode())
        && branchRepository.existsByBranchCode(request.getBranchCode())) {
      throw new DuplicateResourceException(
          "Branch code " + request.getBranchCode() + " sudah digunakan");
    }
    branch.setBranchCode(request.getBranchCode());
    branch.setBranchName(request.getBranchName());
    branch.setAddress(request.getAddress());
    return toResponse(branchRepository.save(branch));
  }

  /*
   * Soft delete - menandai branch sebagai deleted tanpa menghapus dari database
   */
  @org.springframework.transaction.annotation.Transactional
  public void deleteBranch(Long id) {
    Branch branch =
        branchRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Maaf, tidak ada data branch dengan id " + id));

    // VALIDASI SAFE-DELETE 1: Cek apakah masih ada staff aktif di cabang ini
    if (userRepository.existsByBranchIdAndIsActiveTrue(id)) {
      throw new BusinessException(
          "Cabang '" + branch.getBranchName() + "' tidak bisa dihapus karena masih memiliki staff/pengguna aktif.");
    }

    // VALIDASI SAFE-DELETE 2: Cek apakah masih ada pinjaman berjalan (non-final status)
    if (loanApplicationRepository.existsByBranchIdAndStatusNotIn(id)) {
      throw new BusinessException(
          "Cabang '" + branch.getBranchName() + "' tidak bisa dihapus karena masih memiliki pengajuan pinjaman yang sedang diproses.");
    }

    branch.softDelete();
    branchRepository.save(branch);
  }

  /* Method helper untuk membantu mapping Entity ke DTO */
  private BranchResponse toResponse(Branch branch) {
    return BranchResponse.builder()
        .id(branch.getId())
        .branchCode(branch.getBranchCode())
        .branchName(branch.getBranchName())
        .address(branch.getAddress())
        .build();
  }
}

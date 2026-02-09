package com.example.loanova.service;

import com.example.loanova.dto.request.PlafondRequest;
import com.example.loanova.dto.response.PlafondResponse;
import com.example.loanova.entity.Plafond;
import com.example.loanova.exception.BusinessException;
import com.example.loanova.exception.DuplicateResourceException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.LoanApplicationRepository;
import com.example.loanova.repository.PlafondRepository;
import com.example.loanova.repository.UserPlafondRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PLAFOND SERVICE Layer bisnis untuk mengelola data plafond pinjaman. Menangani logika validasi
 * duplikasi, soft delete, dan pemulihan data.
 */
@Service
@RequiredArgsConstructor
public class PlafondService {

  private final PlafondRepository plafondRepository;
  private final UserPlafondRepository userPlafondRepository;
  private final LoanApplicationRepository loanApplicationRepository;

  /** Mendapatkan semua plafond yang aktif */
  public List<PlafondResponse> getAllPlafonds() {
    return plafondRepository.findAll().stream().map(this::toResponse).toList();
  }

  /** Mendapatkan detail plafond berdasarkan ID */
  public PlafondResponse getPlafondById(Long id) {
    return plafondRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(
            () -> new ResourceNotFoundException("Maaf, tidak ada data plafond dengan id " + id));
  }

  /**
   * Menambahkan plafond baru. Melakukan pengecekan duplikasi nama baik pada data aktif maupun yang
   * sudah dihapus.
   */
  @Transactional
  public PlafondResponse createPlafond(PlafondRequest request) {
    if (plafondRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException(
          "Nama plafond " + request.getName() + " sudah digunakan");
    }

    if (plafondRepository.existsByNameAnyStatus(request.getName())) {
      throw new DuplicateResourceException(
          "Nama plafond "
              + request.getName()
              + " sudah dihapus namun masih tersimpan di sistem. Silakan gunakan nama lain.");
    }

    Plafond plafond =
        Plafond.builder()
            .name(request.getName())
            .description(request.getDescription())
            .maxAmount(request.getMaxAmount())
            .interestRate(request.getInterestRate())
            .tenorMin(request.getTenorMin())
            .tenorMax(request.getTenorMax())
            .build();

    return toResponse(plafondRepository.save(plafond));
  }

  /** Menghapus plafond (soft delete) */
  @Transactional
  public void deletePlafond(Long id) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Maaf, tidak ada data plafond dengan id " + id));

    // VALIDASI SAFE-DELETE 1: Cek apakah masih digunakan oleh user di mapping
    // UserPlafond
    if (userPlafondRepository.existsByPlafondId(id)) {
      throw new BusinessException(
          "Plafond '"
              + plafond.getName()
              + "' tidak bisa dihapus karena masih digunakan oleh beberapa customer.");
    }

    // VALIDASI SAFE-DELETE 2: Cek apakah ada riwayat pinjaman yang merujuk paket
    // ini
    if (loanApplicationRepository.existsByPlafondId(id)) {
      throw new BusinessException(
          "Plafond '"
              + plafond.getName()
              + "' tidak bisa dihapus karena memiliki riwayat pengajuan pinjaman.");
    }

    plafond.softDelete();
    plafondRepository.save(plafond);
  }

  /** Mapping Entity ke Response DTO */
  private PlafondResponse toResponse(Plafond plafond) {
    return PlafondResponse.builder()
        .id(plafond.getId())
        .name(plafond.getName())
        .description(plafond.getDescription())
        .maxAmount(plafond.getMaxAmount())
        .interestRate(plafond.getInterestRate())
        .tenorMin(plafond.getTenorMin())
        .tenorMax(plafond.getTenorMax())
        .build();
  }
}

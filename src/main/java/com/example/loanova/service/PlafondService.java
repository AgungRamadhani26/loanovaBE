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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
  @Cacheable(value = "plafonds")
  public List<PlafondResponse> getAllPlafonds() {
    return plafondRepository.findAll().stream().map(this::toResponse).toList();
  }

  /** Mendapatkan detail plafond berdasarkan ID */
  @Cacheable(value = "plafond", key = "#id")
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
  @CacheEvict(value = "plafonds", allEntries = true)
  public PlafondResponse createPlafond(PlafondRequest request) {
    if (plafondRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException(
          "Nama plafond " + request.getName() + " sudah digunakan");
    }

    if (plafondRepository.existsByNameAnyStatus(request.getName())) {
      throw new DuplicateResourceException(
          "Nama plafond "
              + request.getName()
              + " sudah dihapus namun masih tersimpan di sistem, silahkan restore data jika ingin mengembalikannya.");
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

  /**
   * Mengupdate data plafond yang sudah ada. Memastikan nama baru tidak bentrok dengan plafond lain
   * yang sedang aktif.
   */
  @Transactional
  @CacheEvict(value = { "plafond", "plafonds" }, key = "#id", allEntries = true)
  public PlafondResponse updatePlafond(Long id, PlafondRequest request) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Maaf, tidak ada data plafond dengan id " + id));

    if (!plafond.getName().equalsIgnoreCase(request.getName())
        && plafondRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException(
          "Nama plafond " + request.getName() + " sudah digunakan");
    }

    plafond.setName(request.getName());
    plafond.setDescription(request.getDescription());
    plafond.setMaxAmount(request.getMaxAmount());
    plafond.setInterestRate(request.getInterestRate());
    plafond.setTenorMin(request.getTenorMin());
    plafond.setTenorMax(request.getTenorMax());

    return toResponse(plafondRepository.save(plafond));
  }

  /** Menghapus plafond (soft delete) */
  @Transactional
  @CacheEvict(value = { "plafond", "plafonds" }, key = "#id", allEntries = true)
  public void deletePlafond(Long id) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Maaf, tidak ada data plafond dengan id " + id));

    // VALIDASI SAFE-DELETE 1: Cek apakah masih digunakan oleh user di mapping UserPlafond
    if (userPlafondRepository.existsByPlafondId(id)) {
      throw new BusinessException(
          "Plafond '" + plafond.getName() + "' tidak bisa dihapus karena masih digunakan oleh beberapa customer.");
    }

    // VALIDASI SAFE-DELETE 2: Cek apakah ada riwayat pinjaman yang merujuk paket ini
    if (loanApplicationRepository.existsByPlafondId(id)) {
      throw new BusinessException(
          "Plafond '" + plafond.getName() + "' tidak bisa dihapus karena memiliki riwayat pengajuan pinjaman.");
    }

    plafond.softDelete();
    plafondRepository.save(plafond);
  }

  /** Restore plafond yang sudah di-soft delete */
  @Transactional
  @CacheEvict(value = { "plafond", "plafonds" }, key = "#id", allEntries = true)
  public PlafondResponse restorePlafond(Long id) {
    Plafond plafond =
        plafondRepository
            .findByIdIncludeDeleted(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Maaf, tidak ada data plafond dengan id " + id));

    plafond.restore();
    return toResponse(plafondRepository.save(plafond));
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

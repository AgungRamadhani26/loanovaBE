package com.example.loanova.service;

import com.example.loanova.dto.request.PlafondRequest;
import com.example.loanova.dto.response.PlafondResponse;
import com.example.loanova.entity.Plafond;
import com.example.loanova.exception.DuplicateResourceException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.PlafondRepository;
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
  public void deletePlafond(Long id) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Maaf, tidak ada data plafond dengan id " + id));
    plafond.softDelete();
    plafondRepository.save(plafond);
  }

  /** Restore plafond yang sudah di-soft delete */
  @Transactional
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

package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.request.PlafondRequest;
import com.example.loanova.dto.response.PlafondResponse;
import com.example.loanova.service.PlafondService;
import com.example.loanova.util.ResponseUtil;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * PLAFOND CONTROLLER REST API untuk manajemen plafon pinjaman.
 *
 * <p>
 * Base URL: /api/plafonds
 *
 * <p>
 * Otorisasi: Hanya dapat diakses oleh SUPERADMIN.
 */
@RestController
@RequestMapping("/api/plafonds")
@RequiredArgsConstructor
public class PlafondController {

  private final PlafondService plafondService;

  /**
   * GET ALL PLAFONDS (PUBLIC) Endpoint publik untuk melihat daftar plafond tanpa
   * perlu login.
   * Berguna untuk landing page atau halaman informasi produk.
   */
  //Plafond Public
  @GetMapping("/public")
  public ResponseEntity<ApiResponse<List<PlafondResponse>>> getPublicPlafonds() {
    List<PlafondResponse> plafonds = plafondService.getAllPlafonds();
    return ResponseUtil.ok(plafonds, "Berhasil mengambil daftar plafond");
  }

  /** GET ALL PLAFONDS (SUPERADMIN) */
  // Yang bisa akses getAllPlafonds hanya SUPERADMIN
  @PreAuthorize("hasRole('SUPERADMIN')")
  @GetMapping
  public ResponseEntity<ApiResponse<List<PlafondResponse>>> getAllPlafonds() {
    List<PlafondResponse> plafonds = plafondService.getAllPlafonds();
    return ResponseUtil.ok(plafonds, "Berhasil mengambil daftar plafond");
  }

  /** GET PLAFOND BY ID */
  // Yang bisa akses getPlafondById hanya SUPERADMIN
  @PreAuthorize("hasRole('SUPERADMIN')")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PlafondResponse>> getPlafondById(@PathVariable Long id) {
    PlafondResponse plafond = plafondService.getPlafondById(id);
    return ResponseUtil.ok(plafond, "Berhasil mengambil detail plafond");
  }

  /** CREATE PLAFOND */
  // Yang bisa akses createPlafond hanya SUPERADMIN
  @PreAuthorize("hasRole('SUPERADMIN')")
  @PostMapping
  public ResponseEntity<ApiResponse<PlafondResponse>> createPlafond(
      @Valid @RequestBody PlafondRequest request) {
    PlafondResponse plafond = plafondService.createPlafond(request);
    return ResponseUtil.created(plafond, "Berhasil membuat plafond baru");
  }

  /**
   * MENGUPDATE DATA PLAFOND
   *
   * @param id      ID plafond yang akan diupdate
   * @param request Data baru plafond
   * @return Data plafond yang telah berhasil diupdate
   */
  // Yang bisa akses updatePlafond hanya SUPERADMIN
  @PreAuthorize("hasRole('SUPERADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PlafondResponse>> updatePlafond(
      @PathVariable Long id, @Valid @RequestBody PlafondRequest request) {
    PlafondResponse plafond = plafondService.updatePlafond(id, request);
    return ResponseUtil.ok(plafond, "Berhasil memperbarui data plafond");
  }

  /**
   * MENGHAPUS PLAFOND (SOFT DELETE) Menandai data sebagai terhapus tanpa
   * menghilangkannya dari
   * database.
   */
  // Yang bisa akses deletePlafond hanya SUPERADMIN
  @PreAuthorize("hasRole('SUPERADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deletePlafond(@PathVariable Long id) {
    plafondService.deletePlafond(id);
    return ResponseUtil.ok(null, "Berhasil menghapus plafond");
  }

  /**
   * MEMULIHKAN PLAFOND (RESTORE) Mengaktifkan kembali data yang sebelumnya telah
   * di-soft delete.
   */
  // Yang bisa akses restorePlafond hanya SUPERADMIN
  @PreAuthorize("hasRole('SUPERADMIN')")
  @PutMapping("/{id}/restore")
  public ResponseEntity<ApiResponse<PlafondResponse>> restorePlafond(@PathVariable Long id) {
    PlafondResponse plafond = plafondService.restorePlafond(id);
    return ResponseUtil.ok(plafond, "Berhasil me-restore data plafond");
  }
}

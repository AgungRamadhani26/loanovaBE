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
 * <p>Base URL: /api/plafonds
 *
 * <p>Otorisasi: Hanya dapat diakses oleh SUPERADMIN.
 */
@RestController
@RequestMapping("/api/plafonds")
@RequiredArgsConstructor
public class PlafondController {

  private final PlafondService plafondService;

  /**
   * GET ALL PLAFONDS (PUBLIC) Endpoint publik untuk melihat daftar plafond tanpa perlu login.
   * Berguna untuk landing page atau halaman informasi produk.
   */
  // Plafond Public
  @GetMapping("/public")
  public ResponseEntity<ApiResponse<List<PlafondResponse>>> getPublicPlafonds() {
    List<PlafondResponse> plafonds = plafondService.getAllPlafonds();
    return ResponseUtil.ok(plafonds, "Berhasil mengambil daftar plafond");
  }

  /** GET ALL PLAFONDS (SUPERADMIN) */
  // Yang bisa akses getAllPlafonds hanya SUPERADMIN
  @PreAuthorize("hasAuthority('PLAFOND:READ')")
  @GetMapping
  public ResponseEntity<ApiResponse<List<PlafondResponse>>> getAllPlafonds() {
    List<PlafondResponse> plafonds = plafondService.getAllPlafonds();
    return ResponseUtil.ok(plafonds, "Berhasil mengambil daftar plafond");
  }

  /** GET PLAFOND BY ID */
  // Yang bisa akses getPlafondById hanya SUPERADMIN
  @PreAuthorize("hasAuthority('PLAFOND:DETAILS')")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PlafondResponse>> getPlafondById(@PathVariable Long id) {
    PlafondResponse plafond = plafondService.getPlafondById(id);
    return ResponseUtil.ok(plafond, "Berhasil mengambil detail plafond");
  }

  /** CREATE PLAFOND */
  // Yang bisa akses createPlafond hanya SUPERADMIN
  @PreAuthorize("hasAuthority('PLAFOND:CREATE')")
  @PostMapping
  public ResponseEntity<ApiResponse<PlafondResponse>> createPlafond(
      @Valid @RequestBody PlafondRequest request) {
    PlafondResponse plafond = plafondService.createPlafond(request);
    return ResponseUtil.created(plafond, "Berhasil membuat plafond baru");
  }

  /**
   * MENGHAPUS PLAFOND (SOFT DELETE) Menandai data sebagai terhapus tanpa menghilangkannya dari
   * database.
   */
  // Yang bisa akses deletePlafond hanya SUPERADMIN
  @PreAuthorize("hasAuthority('PLAFOND:DELETE')")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deletePlafond(@PathVariable Long id) {
    plafondService.deletePlafond(id);
    return ResponseUtil.ok(null, "Berhasil menghapus plafond");
  }
}

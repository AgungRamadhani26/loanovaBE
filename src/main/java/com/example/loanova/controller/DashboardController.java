package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.response.DashboardStatisticsResponse;
import com.example.loanova.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DASHBOARD CONTROLLER
 * REST endpoints untuk statistik dashboard
 * 
 * Role-based data filtering:
 * - SUPERADMIN/BACKOFFICE: Semua data
 * - MARKETING/BRANCHMANAGER: Data cabang sendiri
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET DASHBOARD STATISTICS
     * Mengembalikan statistik untuk ditampilkan di dashboard
     * 
     * @param authentication - User yang login
     * @return ApiResponse dengan DashboardStatisticsResponse
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('DASHBOARD:STATISTICS')")
    public ResponseEntity<ApiResponse<DashboardStatisticsResponse>> getStatistics(
            Authentication authentication) {
        
        String username = authentication.getName();
        DashboardStatisticsResponse statistics = dashboardService.getStatistics(username);
        
        return ResponseEntity.ok(
            ApiResponse.<DashboardStatisticsResponse>builder()
                .success(true)
                .message("Berhasil mengambil statistik dashboard")
                .data(statistics)
                .code(200)
                .timestamp(java.time.Instant.now())
                .build()
        );
    }
}

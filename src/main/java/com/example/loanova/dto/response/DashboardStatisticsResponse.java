package com.example.loanova.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DASHBOARD STATISTICS RESPONSE DTO
 * Response untuk statistik dashboard berdasarkan role user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {
    
    // Total nilai pengajuan (semua status)
    private BigDecimal totalSubmissionAmount;
    
    // Total nilai pencairan (status DISBURSED)
    private BigDecimal totalDisbursedAmount;
    
    // Estimasi pendapatan
    private BigDecimal estimatedPrincipal;
    private BigDecimal estimatedInterest;
    private BigDecimal estimatedTotalIncome;
    
    // Distribusi status pengajuan
    private List<StatusDistribution> statusDistribution;
    
    // Distribusi plafond aktif
    private List<PlafondDistribution> plafondDistribution;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistribution {
        private String status;
        private Long count;
        private Double percentage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlafondDistribution {
        private String plafondName;
        private Long count;
    }
}

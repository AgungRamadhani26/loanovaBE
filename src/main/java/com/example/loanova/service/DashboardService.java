package com.example.loanova.service;

import com.example.loanova.dto.response.DashboardStatisticsResponse;
import com.example.loanova.dto.response.DashboardStatisticsResponse.PlafondDistribution;
import com.example.loanova.dto.response.DashboardStatisticsResponse.StatusDistribution;
import com.example.loanova.entity.LoanApplication;
import com.example.loanova.entity.User;
import com.example.loanova.entity.UserPlafond;
import com.example.loanova.repository.LoanApplicationRepository;
import com.example.loanova.repository.UserPlafondRepository;
import com.example.loanova.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * DASHBOARD SERVICE Menangani logika bisnis untuk statistik dashboard Role-based filtering: -
 * SUPERADMIN/BACKOFFICE: Semua data - MARKETING/BRANCHMANAGER: Data cabang sendiri
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

  private final LoanApplicationRepository loanApplicationRepository;
  private final UserPlafondRepository userPlafondRepository;
  private final UserRepository userRepository;

  private static final List<String> ALL_STATUSES =
      Arrays.asList(
          "PENDING_REVIEW", "WAITING_APPROVAL", "WAITING_DISBURSEMENT", "DISBURSED", "REJECTED");

  /** Get dashboard statistics based on user role */
  public DashboardStatisticsResponse getStatistics(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

    // Determine if user should see all data or branch-only
    boolean isGlobalRole =
        user.getRoles().stream()
            .anyMatch(
                role ->
                    role.getRoleName().equals("SUPERADMIN")
                        || role.getRoleName().equals("BACKOFFICE"));

    List<LoanApplication> applications;
    if (isGlobalRole) {
      applications = loanApplicationRepository.findAll();
    } else {
      // MARKETING or BRANCHMANAGER - get branch data only
      Long branchId = user.getBranch() != null ? user.getBranch().getId() : null;
      if (branchId == null) {
        applications = new ArrayList<>();
      } else {
        applications = loanApplicationRepository.findByBranchId(branchId);
      }
    }

    // Calculate amounts
    BigDecimal totalSubmissionAmount =
        applications.stream()
            .map(LoanApplication::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    List<LoanApplication> disbursedApps =
        applications.stream()
            .filter(app -> "DISBURSED".equals(app.getStatus()))
            .collect(Collectors.toList());

    BigDecimal totalDisbursedAmount =
        disbursedApps.stream()
            .map(LoanApplication::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Calculate estimated income (principal + interest)
    BigDecimal estimatedPrincipal = totalDisbursedAmount;
    BigDecimal estimatedInterest = calculateTotalInterest(disbursedApps);
    BigDecimal estimatedTotalIncome = estimatedPrincipal.add(estimatedInterest);

    // Calculate status distribution
    List<StatusDistribution> statusDistribution = calculateStatusDistribution(applications);

    // Calculate plafond distribution (all active plafonds)
    List<PlafondDistribution> plafondDistribution = calculatePlafondDistribution();

    return DashboardStatisticsResponse.builder()
        .totalSubmissionAmount(totalSubmissionAmount)
        .totalDisbursedAmount(totalDisbursedAmount)
        .estimatedPrincipal(estimatedPrincipal)
        .estimatedInterest(estimatedInterest)
        .estimatedTotalIncome(estimatedTotalIncome)
        .statusDistribution(statusDistribution)
        .plafondDistribution(plafondDistribution)
        .build();
  }

  /**
   * Calculate total interest from disbursed loans Formula: amount * (interestRate/100) * tenor
   * Note: interestRate is MONTHLY rate, tenor in months
   */
  private BigDecimal calculateTotalInterest(List<LoanApplication> disbursedApps) {
    return disbursedApps.stream()
        .map(
            app -> {
              BigDecimal amount = app.getAmount() != null ? app.getAmount() : BigDecimal.ZERO;
              BigDecimal rate =
                  app.getInterestRateSnapshot() != null
                      ? app.getInterestRateSnapshot()
                      : BigDecimal.ZERO;
              Integer tenor = app.getTenor() != null ? app.getTenor() : 0;

              // interest = amount * (rate/100) * tenor (rate is monthly)
              return amount
                  .multiply(rate)
                  .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                  .multiply(BigDecimal.valueOf(tenor));
            })
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /** Calculate status distribution with percentages */
  private List<StatusDistribution> calculateStatusDistribution(List<LoanApplication> applications) {
    long totalCount = applications.size();

    Map<String, Long> statusCounts =
        applications.stream()
            .filter(app -> app.getStatus() != null) // Filter robust null
            .collect(Collectors.groupingBy(LoanApplication::getStatus, Collectors.counting()));

    return ALL_STATUSES.stream()
        .map(
            status -> {
              Long count = statusCounts.getOrDefault(status, 0L);
              Double percentage = totalCount > 0 ? (count.doubleValue() / totalCount) * 100 : 0.0;
              return StatusDistribution.builder()
                  .status(status)
                  .count(count)
                  .percentage(Math.round(percentage * 100.0) / 100.0) // 2 decimal places
                  .build();
            })
        .collect(Collectors.toList());
  }

  /** Calculate plafond distribution (count of active users per plafond type) */
  private List<PlafondDistribution> calculatePlafondDistribution() {
    List<UserPlafond> activePlafonds = userPlafondRepository.findByIsActiveTrue();

    Map<String, Long> plafondCounts =
        activePlafonds.stream()
            .filter(up -> up.getPlafond() != null && up.getPlafond().getName() != null) // Filter robust null
            .collect(Collectors.groupingBy(up -> up.getPlafond().getName(), Collectors.counting()));

    return plafondCounts.entrySet().stream()
        .map(
            entry ->
                PlafondDistribution.builder()
                    .plafondName(entry.getKey())
                    .count(entry.getValue())
                    .build())
        .sorted((a, b) -> b.getCount().compareTo(a.getCount())) // Sort by count desc
        .collect(Collectors.toList());
  }
}

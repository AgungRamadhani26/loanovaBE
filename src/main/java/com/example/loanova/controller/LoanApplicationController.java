package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.request.LoanApplicationRequest;
import com.example.loanova.dto.request.LoanReviewRequest;
import com.example.loanova.dto.response.ApplicationHistoryResponse;
import com.example.loanova.dto.response.LoanApplicationResponse;
import com.example.loanova.service.LoanApplicationService;
import com.example.loanova.util.ResponseUtil;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * LOAN APPLICATION CONTROLLER - REST endpoints untuk proses pengajuan pinjaman
 * Fitur: 1. Customer:
 * submit loan, view own applications 2. Marketing: review pending applications
 * (PROCEED/REJECT) 3.
 * Branch Manager: approve applications (APPROVE/REJECT) 4. Backoffice: disburse
 * approved
 * applications
 */
@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {

  private final LoanApplicationService loanApplicationService;

  /**
   * CUSTOMER - Submit loan application
   *
   * @param authentication - User yang login (CUSTOMER)
   * @param request        - Data pengajuan pinjaman
   * @return ApiResponse dengan LoanApplicationResponse
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse<LoanApplicationResponse>> submitLoanApplication(
      Authentication authentication, @Valid @ModelAttribute LoanApplicationRequest request) {
    String username = authentication.getName();
    LoanApplicationResponse response = loanApplicationService.submitLoanApplication(username, request);
    return ResponseUtil.created(response, "Pengajuan pinjaman berhasil disubmit");
  }

  /**
   * CUSTOMER - Get my loan applications
   *
   * @param authentication - User yang login (CUSTOMER)
   * @return ApiResponse dengan list LoanApplicationResponse
   */
  @GetMapping("/my")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getMyApplications(
      Authentication authentication) {
    String username = authentication.getName();
    List<LoanApplicationResponse> responses = loanApplicationService.getMyApplications(username);
    return ResponseUtil.ok(responses, "Berhasil mengambil data pengajuan pinjaman");
  }

  /**
   * CUSTOMER/MARKETING/BRANCH_MANAGER/BACKOFFICE - Get application detail by ID
   *
   * @param authentication - User yang login
   * @param id             - ID loan application
   * @return ApiResponse dengan LoanApplicationResponse
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'MARKETING', 'BRANCHMANAGER', 'BACKOFFICE', 'SUPERADMIN')")
  public ResponseEntity<ApiResponse<LoanApplicationResponse>> getApplicationDetail(
      Authentication authentication, @PathVariable Long id) {
    String username = authentication.getName();
    LoanApplicationResponse response = loanApplicationService.getApplicationDetail(username, id);
    return ResponseUtil.ok(response, "Berhasil mengambil detail pengajuan pinjaman");
  }

  /**
   * ALL ROLES - Get application history
   *
   * @param id - ID loan application
   * @return ApiResponse dengan list ApplicationHistoryResponse
   */
  @GetMapping("/{id}/history")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'MARKETING', 'BRANCHMANAGER', 'BACKOFFICE', 'SUPERADMIN')")
  public ResponseEntity<ApiResponse<List<ApplicationHistoryResponse>>> getApplicationHistory(
      @PathVariable Long id) {
    List<ApplicationHistoryResponse> responses = loanApplicationService.getApplicationHistory(id);
    return ResponseUtil.ok(responses, "Berhasil mengambil history pengajuan pinjaman");
  }

  /**
   * MARKETING - Get pending applications for review (status PENDING_REVIEW)
   *
   * @param authentication - User yang login (MARKETING)
   * @return ApiResponse dengan list LoanApplicationResponse
   */
  @GetMapping("/pending-review")
  @PreAuthorize("hasRole('MARKETING')")
  public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getPendingApplications(
      Authentication authentication) {
    String username = authentication.getName();
    List<LoanApplicationResponse> responses = loanApplicationService.getPendingApplicationsForMarketing(username);
    return ResponseUtil.ok(responses, "Berhasil mengambil daftar pengajuan pending review");
  }

  /**
   * MARKETING - Review loan application (PROCEED/REJECT)
   *
   * @param authentication - User yang login (MARKETING)
   * @param id             - ID loan application
   * @param request        - Action PROCEED atau REJECT dengan optional comment
   * @return ApiResponse dengan LoanApplicationResponse
   */
  @PutMapping("/{id}/review")
  @PreAuthorize("hasRole('MARKETING')")
  public ResponseEntity<ApiResponse<LoanApplicationResponse>> reviewApplication(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody LoanReviewRequest request) {
    String username = authentication.getName();
    LoanApplicationResponse response = loanApplicationService.reviewByMarketing(username, id, request);
    return ResponseUtil.ok(response, "Review berhasil diproses");
  }

  /**
   * BRANCH_MANAGER - Get waiting approval applications (status WAITING_APPROVAL)
   *
   * @param authentication - User yang login (BRANCH_MANAGER)
   * @return ApiResponse dengan list LoanApplicationResponse
   */
  @GetMapping("/waiting-approval")
  @PreAuthorize("hasRole('BRANCHMANAGER')")
  public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getWaitingApprovalApplications(
      Authentication authentication) {
    String username = authentication.getName();
    List<LoanApplicationResponse> responses = loanApplicationService
        .getWaitingApprovalApplicationsForBranchManager(username);
    return ResponseUtil.ok(responses, "Berhasil mengambil daftar pengajuan waiting approval");
  }

  /**
   * BRANCH_MANAGER - Approve loan application (APPROVE/REJECT)
   *
   * @param authentication - User yang login (BRANCH_MANAGER)
   * @param id             - ID loan application
   * @param request        - Action APPROVE atau REJECT dengan optional comment
   * @return ApiResponse dengan LoanApplicationResponse
   */
  @PutMapping("/{id}/approve")
  @PreAuthorize("hasRole('BRANCHMANAGER')")
  public ResponseEntity<ApiResponse<LoanApplicationResponse>> approveApplication(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody LoanReviewRequest request) {
    String username = authentication.getName();
    LoanApplicationResponse response = loanApplicationService.approveByBranchManager(username, id, request);
    return ResponseUtil.ok(response, "Approval berhasil diproses");
  }

  /**
   * BACKOFFICE - Get waiting disbursement applications (status
   * WAITING_DISBURSEMENT)
   *
   * @return ApiResponse dengan list LoanApplicationResponse
   */
  @GetMapping("/waiting-disbursement")
  @PreAuthorize("hasAnyRole('BACKOFFICE', 'SUPERADMIN')")
  public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getWaitingDisbursementApplications() {
    List<LoanApplicationResponse> responses = loanApplicationService.getWaitingDisbursementApplications();
    return ResponseUtil.ok(responses, "Berhasil mengambil daftar pengajuan waiting disbursement");
  }

  /**
   * BACKOFFICE - Disburse loan application (status jadi DISBURSED)
   *
   * @param authentication - User yang login (BACKOFFICE)
   * @param id             - ID loan application
   * @return ApiResponse dengan LoanApplicationResponse
   */
  @PutMapping("/{id}/disburse")
  @PreAuthorize("hasAnyRole('BACKOFFICE', 'SUPERADMIN')")
  public ResponseEntity<ApiResponse<LoanApplicationResponse>> disburseApplication(
      Authentication authentication, @PathVariable Long id) {
    String username = authentication.getName();
    LoanApplicationResponse response = loanApplicationService.disburseByBackoffice(username, id);
    return ResponseUtil.ok(response, "Pinjaman berhasil dicairkan");
  }
}

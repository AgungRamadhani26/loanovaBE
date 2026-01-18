package com.example.loanova.service;

import com.example.loanova.dto.request.LoanApplicationRequest;
import com.example.loanova.dto.request.LoanReviewRequest;
import com.example.loanova.dto.response.ApplicationHistoryResponse;
import com.example.loanova.dto.response.LoanApplicationResponse;
import com.example.loanova.entity.*;
import com.example.loanova.exception.BusinessException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.*;
import com.example.loanova.util.FileStorageUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * LOAN APPLICATION SERVICE - Menangani logika bisnis pengajuan dan proses
 * pinjaman.
 */
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

      private final LoanApplicationRepository loanApplicationRepository;
      private final ApplicationHistoryRepository applicationHistoryRepository;
      private final UserRepository userRepository;
      private final UserProfileRepository userProfileRepository;
      private final UserPlafondRepository userPlafondRepository;
      private final BranchRepository branchRepository;
      private final FileStorageUtil fileStorageUtil;
      private final NotificationService notificationService;

      @org.springframework.beans.factory.annotation.Value("${file.upload-dir}")
      private String uploadDir;

      /**
       * SUBMIT LOAN APPLICATION - Customer mengajukan pinjaman baru Logic: 1.
       * Validasi user profile
       * sudah lengkap 2. Validasi tidak ada pinjaman aktif 3. Validasi plafond dan
       * amount 4. Validasi
       * tenor 5. Snapshot data dari user profile 6. Upload dokumen 7. Kurangi
       * remaining amount 8.
       * Simpan loan application 9. Catat history
       */
      @Transactional
      public LoanApplicationResponse submitLoanApplication(
                  String username, LoanApplicationRequest request) {
            // 1. Convert dan validasi input numerik dari String
            Long branchId;
            Long plafondId;
            BigDecimal amount;
            Integer tenor;

            try {
                  branchId = Long.parseLong(request.getBranchId().trim());
                  plafondId = Long.parseLong(request.getPlafondId().trim());
                  amount = new BigDecimal(request.getAmount().trim());
                  tenor = Integer.parseInt(request.getTenor().trim());
            } catch (NumberFormatException e) {
                  throw new BusinessException(
                              "Format input tidak valid. Pastikan branch ID, plafond ID, amount, dan tenor berisi angka yang valid");
            }

            // Validasi nilai harus positif
            if (branchId <= 0) {
                  throw new BusinessException("Branch ID harus lebih besar dari 0");
            }
            if (plafondId <= 0) {
                  throw new BusinessException("Plafond ID harus lebih besar dari 0");
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                  throw new BusinessException("Jumlah pinjaman harus lebih besar dari 0");
            }
            if (tenor <= 0) {
                  throw new BusinessException("Tenor harus lebih besar dari 0");
            }

            // 2. Ambil user yang login
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            // 3. Validasi branch yang dipilih exists
            Branch branch = branchRepository
                        .findById(branchId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                    "Branch tidak ditemukan dengan ID: " + branchId));

            // 4. Validasi user profile sudah lengkap
            UserProfile userProfile = userProfileRepository
                        .findByUser(user)
                        .orElseThrow(
                                    () -> new BusinessException(
                                                "Anda belum melengkapi profil. Silakan lengkapi profil terlebih dahulu"));

            // 5. Validasi tidak ada pinjaman yang sedang diproses
            if (loanApplicationRepository.existsActiveApplicationByUser(user)) {
                  throw new BusinessException(
                              "Anda masih memiliki pengajuan pinjaman yang sedang diproses. "
                                          + "Silakan tunggu hingga proses selesai sebelum mengajukan pinjaman baru");
            }

            // 6. Ambil plafond aktif user
            UserPlafond userPlafond = userPlafondRepository
                        .findByUserAndIsActive(user, true)
                        .orElseThrow(
                                    () -> new BusinessException(
                                                "Anda belum memiliki plafond aktif. Silakan hubungi marketing agar dibantu proses plafond"));

            // 7. Validasi plafond yang dipilih sesuai dengan plafond aktif
            if (!userPlafond.getPlafond().getId().equals(plafondId)) {
                  throw new BusinessException(
                              "Plafond yang dipilih tidak sesuai dengan plafond aktif Anda");
            }

            Plafond plafond = userPlafond.getPlafond();

            // 8. Validasi amount tidak melebihi remaining amount
            if (amount.compareTo(userPlafond.getRemainingAmount()) > 0) {
                  throw new BusinessException(
                              "Jumlah pinjaman ("
                                          + amount
                                          + ") melebihi sisa plafond Anda ("
                                          + userPlafond.getRemainingAmount()
                                          + ")");
            }

            // 9. Validasi tenor sesuai dengan min dan max plafond
            if (tenor < plafond.getTenorMin()
                        || tenor > plafond.getTenorMax()) {
                  throw new BusinessException(
                              "Tenor harus antara "
                                          + plafond.getTenorMin()
                                          + " - "
                                          + plafond.getTenorMax()
                                          + " bulan untuk plafond "
                                          + plafond.getName());
            }

            try {
                  // 10. Copy snapshot foto dari user profile ke folder loan-snapshots
                  String ktpPhotoSnapshotPath = copyFileToSnapshot(userProfile.getKtpPhoto(), "ktp");
                  String npwpPhotoSnapshotPath = null;
                  if (userProfile.getNpwpPhoto() != null) {
                        npwpPhotoSnapshotPath = copyFileToSnapshot(userProfile.getNpwpPhoto(), "npwp");
                  }

                  // 11. Upload dokumen-dokumen baru
                  String savingBookCoverPath = fileStorageUtil.saveFile(request.getSavingBookCover(), "loan-documents");
                  String payslipPhotoPath = fileStorageUtil.saveFile(request.getPayslipPhoto(), "loan-documents");

                  // 12. Buat loan application dengan snapshot data dari user profile
                  LoanApplication loanApplication = LoanApplication.builder()
                              .user(user)
                              .branch(branch) // Branch yang dipilih customer
                              .plafond(plafond)
                              .amount(amount)
                              .tenor(tenor)
                              .status(LoanApplicationStatus.PENDING_REVIEW.name())
                              // Snapshot data pribadi dari user profile
                              .fullNameSnapshot(userProfile.getFullName())
                              .phoneNumberSnapshot(userProfile.getPhoneNumber())
                              .userAddressSnapshot(userProfile.getUserAddress())
                              .nikSnapshot(userProfile.getNik())
                              .birthDateSnapshot(userProfile.getBirthDate())
                              .npwpNumberSnapshot(userProfile.getNpwpNumber())
                              // Data pekerjaan dari request
                              .occupation(request.getOccupation())
                              .companyName(request.getCompanyName())
                              // Data keuangan
                              .rekeningNumber(request.getRekeningNumber())
                              // Snapshot foto dari user profile (di-copy ke folder loan-snapshots)
                              .ktpPhotoSnapshot(ktpPhotoSnapshotPath)
                              .npwpPhotoSnapshot(npwpPhotoSnapshotPath)
                              // Foto dokumen baru
                              .savingBookCover(savingBookCoverPath)
                              .payslipPhoto(payslipPhotoPath)
                              .build();

                  LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);

                  // 13. Kurangi remaining amount
                  userPlafond.setRemainingAmount(
                              userPlafond.getRemainingAmount().subtract(amount));
                  userPlafondRepository.save(userPlafond);

                  // 14. Catat di history
                  createHistory(
                              savedApplication,
                              user,
                              LoanApplicationStatus.PENDING_REVIEW.name(),
                              "Pengajuan pinjaman berhasil disubmit",
                              "CUSTOMER");

                  return toResponse(savedApplication);

            } catch (IOException e) {
                  throw new BusinessException("Gagal menyimpan dokumen: " + e.getMessage());
            }
      }

      /**
       * GET MY APPLICATIONS - Customer melihat daftar pengajuan pinjaman sendiri
       */
      @Transactional(readOnly = true)
      public List<LoanApplicationResponse> getMyApplications(String username) {
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            return loanApplicationRepository.findByUserOrderBySubmittedAtDesc(user).stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
      }

      /**
       * GET APPLICATION DETAIL - Melihat detail loan application
       */
      @Transactional(readOnly = true)
      public LoanApplicationResponse getApplicationDetail(String username, Long applicationId) {
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            LoanApplication application = loanApplicationRepository
                        .findById(applicationId)
                        .orElseThrow(
                                    () -> new ResourceNotFoundException("Loan application tidak ditemukan"));

            // Customer hanya bisa lihat aplikasi miliknya sendiri
            // Marketing/Branch Manager/Backoffice bisa lihat sesuai akses
            if (user.getRoles().stream().anyMatch(r -> r.getRoleName().equalsIgnoreCase("CUSTOMER"))) {
                  if (!application.getUser().getId().equals(user.getId())) {
                        throw new BusinessException("Anda tidak memiliki akses ke aplikasi ini");
                  }
            }

            return toResponse(application);
      }

      /**
       * GET APPLICATION HISTORY - Melihat history perubahan status loan application
       */
      @Transactional(readOnly = true)
      public List<ApplicationHistoryResponse> getApplicationHistory(String username, Long applicationId) {
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            LoanApplication application = loanApplicationRepository
                        .findById(applicationId)
                        .orElseThrow(
                                    () -> new ResourceNotFoundException("Loan application tidak ditemukan"));

            // Validasi akses
            boolean hasAccess = false;

            // 1. Cek SUPERADMIN / BACKOFFICE (Bebas akses)
            if (user.getRoles().stream()
                        .anyMatch(r -> r.getRoleName().equalsIgnoreCase("SUPERADMIN") || r.getRoleName().equalsIgnoreCase("BACKOFFICE"))) {
                  hasAccess = true;
            }
            // 2. Cek MARKETING / BRANCHMANAGER (Sesuai branch)
            else if (user.getRoles().stream().anyMatch(
                        r -> r.getRoleName().equalsIgnoreCase("MARKETING") || r.getRoleName().equalsIgnoreCase("BRANCHMANAGER"))) {
                  if (user.getBranch() != null &&
                              application.getBranch().getId().equals(user.getBranch().getId())) {
                        hasAccess = true;
                  }
            }
            // 3. Cek CUSTOMER (Hanya punya sendiri)
            else if (user.getRoles().stream().anyMatch(r -> r.getRoleName().equalsIgnoreCase("CUSTOMER"))) {
                  if (application.getUser().getId().equals(user.getId())) {
                        hasAccess = true;
                  }
            }

            if (!hasAccess) {
                  throw new BusinessException("Anda tidak memiliki akses untuk melihat history aplikasi ini");
            }

            return applicationHistoryRepository
                        .findByLoanApplicationOrderByCreatedAtDesc(application).stream()
                        .map(this::toHistoryResponse)
                        .collect(Collectors.toList());
      }

      /**
       * GET PENDING APPLICATIONS FOR MARKETING - Marketing melihat list pinjaman
       * dengan status
       * PENDING_REVIEW di branch nya
       */
      @Transactional(readOnly = true)
      public List<LoanApplicationResponse> getPendingApplicationsForMarketing(String username) {
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            if (user.getBranch() == null) {
                  throw new BusinessException("User tidak memiliki branch");
            }

            return loanApplicationRepository
                        .findByStatusAndBranch(LoanApplicationStatus.PENDING_REVIEW.name(), user.getBranch().getId())
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
      }

      /**
       * REVIEW BY MARKETING - Marketing melakukan review (PROCEED/REJECT)
       */
      @Transactional
      public LoanApplicationResponse reviewByMarketing(
                  String username, Long applicationId, LoanReviewRequest request) {
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            if (user.getBranch() == null) {
                  throw new BusinessException("User tidak memiliki branch");
            }

            // Validasi loan application exist dan sesuai branch
            LoanApplication application = loanApplicationRepository
                        .findByIdAndBranchId(applicationId, user.getBranch().getId())
                        .orElseThrow(
                                    () -> new ResourceNotFoundException(
                                                "Loan application tidak ditemukan atau bukan di branch Anda"));

            // Validasi status harus PENDING_REVIEW
            if (!application.getStatus().equals(LoanApplicationStatus.PENDING_REVIEW.name())) {
                  throw new BusinessException(
                              "Loan application tidak dalam status PENDING_REVIEW. Status saat ini: "
                                          + application.getStatus());
            }

            // Validasi action
            if (!request.getAction().equalsIgnoreCase("PROCEED")
                        && !request.getAction().equalsIgnoreCase("REJECT")) {
                  throw new BusinessException("Action harus PROCEED atau REJECT");
            }

            if (request.getAction().equalsIgnoreCase("PROCEED")) {
                  // Proceed -> ubah status jadi WAITING_APPROVAL
                  application.setStatus(LoanApplicationStatus.WAITING_APPROVAL.name());
                  createHistory(
                              application,
                              user,
                              LoanApplicationStatus.WAITING_APPROVAL.name(),
                              request.getComment() != null ? request.getComment() : "Diproses oleh Marketing",
                              "MARKETING");

                  // NOTIFIKASI CUSTOMER
                  notificationService.createNotification(
                        application.getUser(),
                        "Pengajuan Pinjaman Diproses",
                        "Pengajuan pinjaman Anda telah diproses oleh Marketing dan sekarang menunggu persetujuan Branch Manager.");
            } else {
                  // Reject -> kembalikan remaining amount
                  application.setStatus(LoanApplicationStatus.REJECTED.name());

                  // Kembalikan remaining amount ke user plafond
                  UserPlafond userPlafond = userPlafondRepository
                              .findByUserAndIsActive(application.getUser(), true)
                              .orElseThrow(() -> new BusinessException("User plafond tidak ditemukan"));

                  userPlafond.setRemainingAmount(
                              userPlafond.getRemainingAmount().add(application.getAmount()));
                  userPlafondRepository.save(userPlafond);

                  // Validasi comment wajib jika reject
                  if (request.getComment() == null || request.getComment().trim().isEmpty()) {
                        throw new BusinessException("Comment wajib diisi jika melakukan reject");
                  }

                  createHistory(
                              application,
                              user,
                              LoanApplicationStatus.REJECTED.name(),
                              request.getComment(),
                              "MARKETING");

                  // NOTIFIKASI CUSTOMER
                  notificationService.createNotification(
                        application.getUser(),
                        "Pengajuan Pinjaman Ditolak",
                        "Mohon maaf, pengajuan pinjaman Anda ditolak oleh Marketing. Alasan: " + request.getComment());
            }

            LoanApplication savedApplication = loanApplicationRepository.save(application);
            return toResponse(savedApplication);
      }

      /**
       * GET ALL APPLICATIONS - Get all loan applications based on user role
       * SUPERADMIN/BACKOFFICE: See ALL
       * MARKETING/BRANCHMANAGER: See Branch Only
       * CUSTOMER: See Own Only
       */
      @Transactional(readOnly = true)
      public List<LoanApplicationResponse> getAllApplications(String username) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            List<String> roles = user.getRoles().stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
            
            List<LoanApplication> applications;

            if (roles.contains("SUPERADMIN") || roles.contains("BACKOFFICE")) {
                // SUPERADMIN & BACKOFFICE: See ALL
                applications = loanApplicationRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "submittedAt"));
            } else if (roles.contains("MARKETING") || roles.contains("BRANCHMANAGER")) {
                // MARKETING & BRANCHMANAGER: See Branch Only
                if (user.getBranch() == null) {
                    throw new BusinessException("User staff tidak memiliki assignment branch");
                }
                applications = loanApplicationRepository.findByBranchIdOrderBySubmittedAtDesc(user.getBranch().getId());
            } else {
                // CUSTOMER (or others): See Own Only
                applications = loanApplicationRepository.findByUserOrderBySubmittedAtDesc(user);
            }

            return applications.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
      }

      /**
       * GET WAITING APPROVAL APPLICATIONS FOR BRANCH MANAGER - Branch Manager melihat
       * list pinjaman
       * dengan status WAITING_APPROVAL di branch nya
       */
      @Transactional(readOnly = true)
      public List<LoanApplicationResponse> getWaitingApprovalApplicationsForBranchManager(
                  String username) {
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            if (user.getBranch() == null) {
                  throw new BusinessException("User tidak memiliki branch");
            }

            return loanApplicationRepository
                        .findByStatusAndBranch(
                                    LoanApplicationStatus.WAITING_APPROVAL.name(), user.getBranch().getId())
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
      }

      /**
       * APPROVE BY BRANCH MANAGER - Branch Manager melakukan approval
       * (APPROVE/REJECT)
       */
      @Transactional
      public LoanApplicationResponse approveByBranchManager(
                  String username, Long applicationId, LoanReviewRequest request) {
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            if (user.getBranch() == null) {
                  throw new BusinessException("User tidak memiliki branch");
            }

            // Validasi loan application exist dan sesuai branch
            LoanApplication application = loanApplicationRepository
                        .findByIdAndBranchId(applicationId, user.getBranch().getId())
                        .orElseThrow(
                                    () -> new ResourceNotFoundException(
                                                "Loan application tidak ditemukan atau bukan di branch Anda"));

            // Validasi status harus WAITING_APPROVAL
            if (!application.getStatus().equals(LoanApplicationStatus.WAITING_APPROVAL.name())) {
                  throw new BusinessException(
                              "Loan application tidak dalam status WAITING_APPROVAL. Status saat ini: "
                                          + application.getStatus());
            }

            // Validasi action
            if (!request.getAction().equalsIgnoreCase("APPROVE")
                        && !request.getAction().equalsIgnoreCase("REJECT")) {
                  throw new BusinessException("Action harus APPROVE atau REJECT");
            }

            if (request.getAction().equalsIgnoreCase("APPROVE")) {
                  // Approve -> ubah status jadi WAITING_DISBURSEMENT
                  application.setStatus(LoanApplicationStatus.WAITING_DISBURSEMENT.name());
                  createHistory(
                              application,
                              user,
                              LoanApplicationStatus.WAITING_DISBURSEMENT.name(),
                              request.getComment() != null ? request.getComment() : "Disetujui oleh Branch Manager",
                              "BRANCHMANAGER");

                  // NOTIFIKASI CUSTOMER
                  notificationService.createNotification(
                        application.getUser(),
                        "Pengajuan Pinjaman Disetujui",
                        "Selamat! Pengajuan pinjaman Anda telah disetujui oleh Branch Manager dan sedang menunggu pencairan dana.");
            } else {
                  // Reject -> kembalikan remaining amount
                  application.setStatus(LoanApplicationStatus.REJECTED.name());

                  // Kembalikan remaining amount ke user plafond
                  UserPlafond userPlafond = userPlafondRepository
                              .findByUserAndIsActive(application.getUser(), true)
                              .orElseThrow(() -> new BusinessException("User plafond tidak ditemukan"));

                  userPlafond.setRemainingAmount(
                              userPlafond.getRemainingAmount().add(application.getAmount()));
                  userPlafondRepository.save(userPlafond);

                  // Validasi comment wajib jika reject
                  if (request.getComment() == null || request.getComment().trim().isEmpty()) {
                        throw new BusinessException("Comment wajib diisi jika melakukan reject");
                  }

                  createHistory(
                              application,
                              user,
                              LoanApplicationStatus.REJECTED.name(),
                              request.getComment(),
                              "BRANCHMANAGER");

                  // NOTIFIKASI CUSTOMER
                  notificationService.createNotification(
                        application.getUser(),
                        "Pengajuan Pinjaman Ditolak",
                        "Mohon maaf, pengajuan pinjaman Anda ditolak oleh Branch Manager. Alasan: " + request.getComment());
            }

            LoanApplication savedApplication = loanApplicationRepository.save(application);
            return toResponse(savedApplication);
      }

      /**
       * GET WAITING DISBURSEMENT APPLICATIONS FOR BACKOFFICE - Backoffice melihat
       * semua pinjaman dengan
       * status WAITING_DISBURSEMENT dari semua branch
       */
      @Transactional(readOnly = true)
      public List<LoanApplicationResponse> getWaitingDisbursementApplications() {
            return loanApplicationRepository
                        .findByStatusOrderBySubmittedAtAsc(LoanApplicationStatus.WAITING_DISBURSEMENT.name())
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
      }

      /**
       * DISBURSE BY BACKOFFICE - Backoffice melakukan pencairan
       */
      @Transactional
      public LoanApplicationResponse disburseByBackoffice(String username, Long applicationId) {
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            LoanApplication application = loanApplicationRepository
                        .findById(applicationId)
                        .orElseThrow(
                                    () -> new ResourceNotFoundException("Loan application tidak ditemukan"));

            // Validasi status harus WAITING_DISBURSEMENT
            if (!application.getStatus().equals(LoanApplicationStatus.WAITING_DISBURSEMENT.name())) {
                  throw new BusinessException(
                              "Loan application tidak dalam status WAITING_DISBURSEMENT. Status saat ini: "
                                          + application.getStatus());
            }

            // Ubah status jadi DISBURSED
            application.setStatus(LoanApplicationStatus.DISBURSED.name());
            createHistory(
                        application,
                        user,
                        LoanApplicationStatus.DISBURSED.name(),
                        "Pinjaman berhasil dicairkan",
                        "BACKOFFICE");

            // NOTIFIKASI CUSTOMER
            notificationService.createNotification(
                  application.getUser(),
                  "Dana Pinjaman Cair!",
                  "Kabar gembira! Dana pinjaman Anda sebesar Rp " + application.getAmount() + " telah berhasil dicairkan. Silakan cek rekening Anda.");
            // TODO: Kirim notifikasi via Email/WA real (Future Improvement)

            LoanApplication savedApplication = loanApplicationRepository.save(application);
            return toResponse(savedApplication);
      }

      /**
       * REJECT BY BACKOFFICE - Backoffice menolak pencairan
       */
      @Transactional
      public LoanApplicationResponse rejectByBackoffice(String username, Long applicationId, LoanReviewRequest request) {
            User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

            LoanApplication application = loanApplicationRepository
                        .findById(applicationId)
                        .orElseThrow(
                                    () -> new ResourceNotFoundException("Loan application tidak ditemukan"));

            // Validasi status harus WAITING_DISBURSEMENT
            if (!application.getStatus().equals(LoanApplicationStatus.WAITING_DISBURSEMENT.name())) {
                  throw new BusinessException(
                              "Loan application tidak dalam status WAITING_DISBURSEMENT. Status saat ini: "
                                          + application.getStatus());
            }

            // Validasi comment wajib jika reject
            if (request.getComment() == null || request.getComment().trim().isEmpty()) {
                  throw new BusinessException("Comment wajib diisi jika melakukan reject");
            }

            // Ubah status jadi REJECTED
            application.setStatus(LoanApplicationStatus.REJECTED.name());

            // KEMBALIKAN PLAFOND: Karena belum dicairkan, limit harus dikembalikan
            UserPlafond userPlafond = userPlafondRepository
                        .findByUserAndIsActive(application.getUser(), true)
                        .orElseThrow(() -> new BusinessException("User plafond tidak ditemukan"));

            userPlafond.setRemainingAmount(
                        userPlafond.getRemainingAmount().add(application.getAmount()));
            userPlafondRepository.save(userPlafond);

            createHistory(
                        application,
                        user,
                        LoanApplicationStatus.REJECTED.name(),
                        request.getComment(),
                        "BACKOFFICE");

            // NOTIFIKASI CUSTOMER
            notificationService.createNotification(
                  application.getUser(),
                  "Pencairan Pinjaman Ditolak",
                  "Mohon maaf, proses pencairan pinjaman Anda ditolak oleh Backoffice. Alasan: " + request.getComment());

            LoanApplication savedApplication = loanApplicationRepository.save(application);
            return toResponse(savedApplication);
      }

      /** Helper method untuk create history */
      private void createHistory(
                  LoanApplication application, User actionBy, String status, String comment, String role) {
            ApplicationHistory history = ApplicationHistory.builder()
                        .loanApplication(application)
                        .actionByUser(actionBy)
                        .status(status)
                        .comment(comment)
                        .actionByRole(role)
                        .build();
            applicationHistoryRepository.save(history);
      }

      /**
       * Helper method untuk copy file snapshot dari user profile ke folder
       * loan-snapshots Tujuan:
       * Isolasi data agar perubahan di user profile tidak mempengaruhi snapshot loan
       */
      private String copyFileToSnapshot(String originalPath, String fileType) throws IOException {
            if (originalPath == null || originalPath.isEmpty()) {
                  return null;
            }

            // Sub-direktori untuk snapshot
            String snapshotSubDir = "loan-snapshots";
            Path snapshotDir = Paths.get(uploadDir, snapshotSubDir);

            // Buat folder jika belum ada
            if (!Files.exists(snapshotDir)) {
                  Files.createDirectories(snapshotDir);
            }

            // Generate nama file baru dengan UUID
            String fileExtension = "";
            if (originalPath.contains(".")) {
                  fileExtension = originalPath.substring(originalPath.lastIndexOf("."));
            }
            String newFileName = fileType + "_" + UUID.randomUUID().toString() + fileExtension;
            Path targetPath = snapshotDir.resolve(newFileName);

            // Path sumber file (originalPath biasanya berisi "profiles/uuid.jpg")
            // Kita harus gabungkan dengan uploadDir agar filenya ketemu
            Path sourcePath = Paths.get(uploadDir, originalPath);

            if (Files.exists(sourcePath)) {
                  Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                  // Return format yang konsisten dengan FileStorageUtil: "subDir/fileName"
                  return snapshotSubDir + "/" + newFileName;
            }

            // Jika file source tidak ada (misal data dummy), return null agar tidak error
            // saat fetching
            return null;
      }

      /** Mapper Entity to Response DTO */
      private LoanApplicationResponse toResponse(LoanApplication application) {
            return LoanApplicationResponse.builder()
                        .id(application.getId())
                        .userId(application.getUser().getId())
                        .username(application.getUser().getUsername())
                        .branchId(application.getBranch().getId())
                        .branchCode(application.getBranch().getBranchCode())
                        .plafondId(application.getPlafond().getId())
                        .plafondName(application.getPlafond().getName())
                        .amount(application.getAmount())
                        .tenor(application.getTenor())
                        .status(application.getStatus())
                        .submittedAt(application.getSubmittedAt())
                        .fullNameSnapshot(application.getFullNameSnapshot())
                        .phoneNumberSnapshot(application.getPhoneNumberSnapshot())
                        .userAddressSnapshot(application.getUserAddressSnapshot())
                        .nikSnapshot(application.getNikSnapshot())
                        .birthDateSnapshot(application.getBirthDateSnapshot())
                        .npwpNumberSnapshot(application.getNpwpNumberSnapshot())
                        .occupation(application.getOccupation())
                        .companyName(application.getCompanyName())
                        .rekeningNumber(application.getRekeningNumber())
                        .ktpPhotoSnapshot(application.getKtpPhotoSnapshot())
                        .npwpPhotoSnapshot(application.getNpwpPhotoSnapshot())
                        .savingBookCover(application.getSavingBookCover())
                        .payslipPhoto(application.getPayslipPhoto())
                        .build();
      }

      /** Mapper Entity to History Response DTO */
      private ApplicationHistoryResponse toHistoryResponse(ApplicationHistory history) {
            return ApplicationHistoryResponse.builder()
                        .id(history.getId())
                        .loanApplicationId(history.getLoanApplication().getId())
                        .actionByUserId(history.getActionByUser().getId())
                        .actionByUsername(history.getActionByUser().getUsername())
                        .actionByRole(history.getActionByRole())
                        .status(history.getStatus())
                        .comment(history.getComment())
                        .createdAt(history.getCreatedAt())
                        .build();
      }
}

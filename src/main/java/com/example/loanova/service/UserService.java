package com.example.loanova.service;

import com.example.loanova.dto.request.UserRequest;
import com.example.loanova.dto.request.UserUpdateRequest;
import com.example.loanova.dto.response.UserResponse;
import com.example.loanova.entity.Branch;
import com.example.loanova.entity.Plafond;
import com.example.loanova.entity.Role;
import com.example.loanova.entity.User;
import com.example.loanova.entity.UserPlafond;
import com.example.loanova.exception.BusinessException;
import com.example.loanova.exception.DuplicateResourceException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.BranchRepository;
import com.example.loanova.repository.PlafondRepository;
import com.example.loanova.repository.RoleRepository;
import com.example.loanova.repository.UserPlafondRepository;
import com.example.loanova.repository.UserRepository;
import com.example.loanova.repository.LoanApplicationRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final BranchRepository branchRepository;
  private final RoleRepository roleRepository;
  private final UserPlafondRepository userPlafondRepository;
  private final PlafondRepository plafondRepository;
  private final PasswordEncoder passwordEncoder;
  private final LoanApplicationRepository loanApplicationRepository;

  public UserService(
      UserRepository userRepository,
      BranchRepository branchRepository,
      RoleRepository roleRepository,
      UserPlafondRepository userPlafondRepository,
      PlafondRepository plafondRepository,
      PasswordEncoder passwordEncoder,
      LoanApplicationRepository loanApplicationRepository) {
    this.userRepository = userRepository;
    this.branchRepository = branchRepository;
    this.roleRepository = roleRepository;
    this.userPlafondRepository = userPlafondRepository;
    this.plafondRepository = plafondRepository;
    this.passwordEncoder = passwordEncoder;
    this.loanApplicationRepository = loanApplicationRepository;
  }

  /*
   * Mendapatkan semua User yang ada di sistem (auto exclude deleted via @Where)
   */
  public List<UserResponse> getAllUser() {
    return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
  }

  /*
   * Mendapatkan User berdasarkan ID
   */
  public UserResponse getUserById(Long id) {
    User user = userRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Maaf, tidak ada data user dengan id " + id));
    return toResponse(user);
  }

  /* Menambahkan User baru ke dalam sistem */
  @Transactional
  public UserResponse createUser(UserRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("Username sudah digunakan");
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Email sudah digunakan");
    }
    // Ambil roles
    Set<Role> roles = roleRepository.findAllById(request.getRoleIds()).stream().collect(Collectors.toSet());
    if (roles.isEmpty()) {
      throw new BusinessException("Role wajib diisi minimal 1");
    }
    // Cek apakah ada role MARKETING atau BRANCHMANAGER
    boolean requiresBranch = roles.stream()
        .anyMatch(
            r -> r.getRoleName().equalsIgnoreCase("MARKETING")
                || r.getRoleName().equalsIgnoreCase("BRANCHMANAGER"));
    Branch branch = null;
    if (requiresBranch) {
      if (request.getBranchId() == null) {
        throw new BusinessException("Branch wajib diisi untuk role MARKETING dan BRANCHMANAGER");
      }
      branch = branchRepository
          .findById(request.getBranchId())
          .orElseThrow(() -> new ResourceNotFoundException("Branch tidak ditemukan"));
    } else if (request.getBranchId() != null) {
      // Jika branch diisi untuk role lain (opsional), validasi apakah exist
      branch = branchRepository
          .findById(request.getBranchId())
          .orElseThrow(() -> new ResourceNotFoundException("Branch tidak ditemukan"));
    }
    User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .branch(branch)
        .roles(roles)
        .isActive(request.getIsActive())
        .build();
    User savedUser = userRepository.save(user);

    // Auto-assign Bronze plafond jika user memiliki role CUSTOMER
    if (hasCustomerRole(savedUser)) {
      createDefaultUserPlafondIfNotExists(savedUser);
    }

    return toResponse(savedUser);
  }

  /* Mengupdate data user */
  @Transactional
  public UserResponse updateUser(Long id, UserUpdateRequest request) {
    User user = userRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Maaf, tidak ada data user dengan id " + id));
    // Cek duplikasi username kecuali diri sendiri
    if (!user.getUsername().equals(request.getUsername())
        && userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("Username sudah digunakan");
    }

    // Cek duplikasi email kecuali diri sendiri
    if (!user.getEmail().equals(request.getEmail())
        && userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Email sudah digunakan");
    }
    // Ambil roles
    Set<Role> roles = roleRepository.findAllById(request.getRoleIds()).stream().collect(Collectors.toSet());
    if (roles.isEmpty()) {
      throw new BusinessException("Role wajib diisi minimal 1");
    }
    // Cek apakah ada role MARKETING atau BRANCHMANAGER
    boolean requiresBranch = roles.stream()
        .anyMatch(
            r -> r.getRoleName().equalsIgnoreCase("MARKETING")
                || r.getRoleName().equalsIgnoreCase("BRANCHMANAGER"));
    Branch branch = null;
    if (requiresBranch) {
      if (request.getBranchId() == null) {
        throw new BusinessException("Branch wajib diisi untuk role MARKETING dan BRANCHMANAGER");
      }
      branch = branchRepository
          .findById(request.getBranchId())
          .orElseThrow(() -> new ResourceNotFoundException("Branch tidak ditemukan"));
    } else if (request.getBranchId() != null) {
      // Jika branch diisi untuk role lain (opsional), validasi apakah exist
      branch = branchRepository
          .findById(request.getBranchId())
          .orElseThrow(() -> new ResourceNotFoundException("Branch tidak ditemukan"));
    }

    // update field tanpa password
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setRoles(roles);
    user.setBranch(branch);
    user.setIsActive(request.getIsActive());

    User savedUser = userRepository.save(user);

    // Auto-assign Bronze plafond jika user diubah menjadi CUSTOMER dan belum punya
    // plafond
    if (hasCustomerRole(savedUser)) {
      createDefaultUserPlafondIfNotExists(savedUser);
    }

    return toResponse(savedUser);
  }

  /* Soft delete - menandai user sebagai deleted tanpa menghapus dari database */
  @Transactional
  public void deleteUser(Long id) {
    User user = userRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Maaf, tidak ada data user dengan id " + id));

    // VALIDASI SAFE-DELETE 1: Proteksi Superadmin Terakhir
    boolean isAdmin = user.getRoles().stream()
        .anyMatch(r -> r.getRoleName().equalsIgnoreCase("SUPERADMIN"));

    if (isAdmin && user.getIsActive()) {
        long activeAdminCount = userRepository.countByRolesRoleNameAndIsActiveTrue("SUPERADMIN");
        if (activeAdminCount <= 1) {
            throw new BusinessException("Gagal menghapus. Harus ada minimal satu SUPERADMIN aktif di sistem.");
        }
    }

    // VALIDASI SAFE-DELETE 2: Cek Pinjaman Aktif
    if (loanApplicationRepository.existsActiveApplicationByUser(user.getId())) {
        throw new BusinessException("User tidak bisa dihapus karena masih memiliki pengajuan pinjaman yang sedang diproses.");
    }

    user.setIsActive(false); // Otomatis nonaktifkan saat didelete
    user.softDelete();
    userRepository.save(user);
  }

  /**
   * Helper method untuk cek apakah user memiliki role CUSTOMER
   */
  private boolean hasCustomerRole(User user) {
    return user.getRoles().stream()
        .anyMatch(role -> role.getRoleName().equalsIgnoreCase("CUSTOMER"));
  }

  /**
   * Helper method untuk auto-create default user plafond (Bronze) jika belum ada.
   * Plafond Bronze dengan ID 3 akan otomatis di-assign ke setiap customer.
   * Method ini mengecek terlebih dahulu apakah user sudah punya plafond aktif,
   * jika sudah ada maka tidak membuat yang baru.
   * 
   * @param user User yang akan diberi plafond
   */
  private void createDefaultUserPlafondIfNotExists(User user) {
    // Cek apakah user sudah memiliki plafond aktif
    boolean hasActivePlafond = userPlafondRepository
        .findByUserAndIsActive(user, true)
        .isPresent();

    if (hasActivePlafond) {
      // User sudah punya plafond aktif, skip
      return;
    }

    // Ambil plafond Bronze berdasarkan nama
    Plafond bronzePlafond = plafondRepository
        .findByName("BRONZE")
        .orElseThrow(() -> new BusinessException("Plafond 'BRONZE' tidak ditemukan di database. Pastikan data master sudah di-seed."));

    // Create user plafond dengan max_amount dari Bronze
    UserPlafond userPlafond = UserPlafond.builder()
        .user(user)
        .plafond(bronzePlafond)
        .maxAmount(bronzePlafond.getMaxAmount())
        .remainingAmount(bronzePlafond.getMaxAmount()) // Awal sama dengan max_amount
        .isActive(true)
        .build();

    userPlafondRepository.save(userPlafond);
  }

  /* Method helper untuk membantu mapping Entity ke DTO */
  private UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .branchCode(user.getBranch() != null ? user.getBranch().getBranchCode() : null)
        .isActive(user.getIsActive())
        .roles(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet()))
        .build();
  }
}

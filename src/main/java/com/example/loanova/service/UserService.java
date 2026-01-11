package com.example.loanova.service;

import com.example.loanova.dto.request.UserRequest;
import com.example.loanova.dto.request.UserUpdateRequest;
import com.example.loanova.dto.response.UserResponse;
import com.example.loanova.entity.Branch;
import com.example.loanova.entity.Role;
import com.example.loanova.entity.User;
import com.example.loanova.exception.BusinessException;
import com.example.loanova.exception.DuplicateResourceException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.BranchRepository;
import com.example.loanova.repository.RoleRepository;
import com.example.loanova.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final BranchRepository branchRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      BranchRepository branchRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.branchRepository = branchRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /*
   * Mendapatkan semua User yang ada di sistem (auto exclude deleted via @Where)
   */
  @Cacheable(value = "users")
  public List<UserResponse> getAllUser() {
    return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
  }

  /*
   * Mendapatkan User berdasarkan ID
   */
  @Cacheable(value = "user", key = "#id")
  public UserResponse getUserById(Long id) {
    User user = userRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Maaf, tidak ada data user dengan id " + id));
    return toResponse(user);
  }

  /* Menambahkan User baru ke dalam sistem */
  @Transactional
  @CacheEvict(value = "users", allEntries = true)
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
    // Cek apakah ada role MARKETING atau BRANCH_MANAGER
    boolean requiresBranch = roles.stream()
        .anyMatch(
            r -> r.getRoleName().equalsIgnoreCase("MARKETING")
                || r.getRoleName().equalsIgnoreCase("BRANCH_MANAGER"));
    Branch branch = null;
    if (requiresBranch) {
      if (request.getBranchId() == null) {
        throw new BusinessException("Branch wajib diisi untuk role MARKETING dan BRANCH_MANAGER");
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
    return toResponse(userRepository.save(user));
  }

  /* Mengupdate data user */
  @Transactional
  @CacheEvict(value = { "user", "users" }, key = "#id", allEntries = true)
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
    // Cek apakah ada role MARKETING atau BRANCH_MANAGER
    boolean requiresBranch = roles.stream()
        .anyMatch(
            r -> r.getRoleName().equalsIgnoreCase("MARKETING")
                || r.getRoleName().equalsIgnoreCase("BRANCH_MANAGER"));
    Branch branch = null;
    if (requiresBranch) {
      if (request.getBranchId() == null) {
        throw new BusinessException("Branch wajib diisi untuk role MARKETING dan BRANCH_MANAGER");
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
    return toResponse(userRepository.save(user));
  }

  /* Soft delete - menandai user sebagai deleted tanpa menghapus dari database */
  @Transactional
  @CacheEvict(value = { "user", "users" }, key = "#id", allEntries = true)
  public void deleteUser(Long id) {
    User user = userRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Maaf, tidak ada data user dengan id " + id));

    user.softDelete();
    userRepository.save(user);
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

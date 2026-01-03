package com.example.loanova.service;

import com.example.loanova.dto.request.LoginRequest;
import com.example.loanova.dto.request.RegisterRequest;
import com.example.loanova.dto.response.AuthResponse;
import com.example.loanova.dto.response.RegisterResponse;
import com.example.loanova.entity.RefreshToken;
import com.example.loanova.entity.Role;
import com.example.loanova.entity.User;
import com.example.loanova.exception.BusinessException;
import com.example.loanova.exception.DuplicateResourceException;
import com.example.loanova.repository.RefreshTokenRepository;
import com.example.loanova.repository.RoleRepository;
import com.example.loanova.repository.RoleRepository;
import com.example.loanova.repository.UserRepository;
import com.example.loanova.repository.PasswordResetTokenRepository;
import com.example.loanova.entity.PasswordResetToken;
import com.example.loanova.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * AUTH SERVICE - Service untuk handle authentication (login, logout, refresh,
 * dll)
 * 
 * Class ini implements UserDetailsService:
 * - Interface dari Spring Security untuk load user dari database
 * - Method loadUserByUsername() dipanggil saat authentication
 * 
 * Fungsi service ini:
 * 1. Handle login user
 * 2. Handle refresh token (generate access token baru)
 * 3. Generate access token & refresh token
 * 4. Load user dari database untuk Spring Security
 * 5. Convert entity User → UserDetails (format Spring Security)
 */
@Service
public class AuthService implements UserDetailsService {

      private final UserRepository userRepository;
      private final RefreshTokenRepository refreshTokenRepository;
      private final RoleRepository roleRepository;
      private final PasswordResetTokenRepository passwordResetTokenRepository;
      private final EmailService emailService;
      private final JwtService jwtService;
      private final AuthenticationManager authenticationManager;
      private final PasswordEncoder passwordEncoder;

      @Value("${app.frontend.url:http://localhost:9091}")
      private String frontendUrl;

      /**
       * Constructor dengan manual injection
       * 
       * @Lazy pada AuthenticationManager untuk avoid circular dependency:
       *       AuthService → SecurityConfig → AuthenticationManager → AuthService
       */
      public AuthService(
                  UserRepository userRepository,
                  RefreshTokenRepository refreshTokenRepository,
                  RoleRepository roleRepository,
                  PasswordResetTokenRepository passwordResetTokenRepository,
                  EmailService emailService,
                  JwtService jwtService,
                  @Lazy AuthenticationManager authenticationManager,
                  PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.refreshTokenRepository = refreshTokenRepository;
            this.roleRepository = roleRepository;
            this.passwordResetTokenRepository = passwordResetTokenRepository;
            this.emailService = emailService;
            this.jwtService = jwtService;
            this.authenticationManager = authenticationManager;
            this.passwordEncoder = passwordEncoder;
      }

      /**
       * REGISTER CUSTOMER - Handle pendaftaran user baru dengan role CUSTOMER
       * 
       * @param request RegisterRequest dengan username, email, dan password
       * @return RegisterResponse dengan info user yang berhasil didaftarkan
       */
      @Transactional
      public RegisterResponse register(RegisterRequest request) {
            // STEP 1: Validate keunikan username & email (Style:
            // DuplicateResourceException)
            if (userRepository.existsByUsername(request.getUsername())) {
                  throw new DuplicateResourceException("Username sudah digunakan, gunakan username lain");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                  throw new DuplicateResourceException("Email sudah digunakan, gunakan email lain");
            }

            // STEP 2: Ambil role CUSTOMER
            Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                        .orElseThrow(() -> new BusinessException("Role CUSTOMER tidak ditemukan di sistem"));

            // STEP 3: Create User entity
            User user = User.builder()
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .isActive(true)
                        .roles(new HashSet<>(Collections.singletonList(customerRole)))
                        .build();

            // STEP 4: Save to database
            User savedUser = userRepository.save(user);

            // STEP 5: Return mapped response
            return toRegisterResponse(savedUser);
      }

      /**
       * Mapper helper - Convert User entity ke RegisterResponse
       */
      private RegisterResponse toRegisterResponse(User user) {
            return RegisterResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .isActive(user.getIsActive())
                        .roles(user.getRoles().stream()
                                    .map(Role::getRoleName)
                                    .collect(Collectors.toSet()))
                        .build();
      }

      /**
       * LOGIN USER - Handle login dan generate JWT tokens
       * 
       * Flow:
       * 1. Validate username & password via AuthenticationManager
       * 2. Load user dari database
       * 3. Generate access token (15 menit) & refresh token (7 hari)
       * 4. Save refresh token ke database
       * 5. Return tokens ke client
       * 
       * @param request LoginRequest dengan username & password
       * @return AuthResponse dengan access token & refresh token
       */
      @Transactional
      public AuthResponse login(LoginRequest request) {
            try {
                  // STEP 1: Authenticate user (validate username & password)
                  // AuthenticationManager akan:
                  // - Call loadUserByUsername() untuk load user dari DB
                  // - Compare password input dengan password di DB pakai BCrypt
                  // - Throw BadCredentialsException kalau salah
                  authenticationManager.authenticate(
                              new UsernamePasswordAuthenticationToken(
                                          request.getUsername(),
                                          request.getPassword()));

                  // STEP 2: Get user dari database
                  // Kalau sampai sini, berarti username & password BENAR
                  User user = userRepository.findByUsername(request.getUsername())
                              .orElseThrow(() -> new BusinessException("User tidak ditemukan"));

                  // STEP 3: Check apakah user aktif
                  if (Boolean.FALSE.equals(user.getIsActive())) {
                        throw new BusinessException("User tidak aktif");
                  }

                  // STEP 4: Load UserDetails (format Spring Security)
                  // UserDetails berisi username, password, roles
                  UserDetails userDetails = loadUserByUsername(request.getUsername());

                  // STEP 5: Generate JWT tokens
                  // Access Token: 15 menit (untuk akses API)
                  // Refresh Token: 7 hari (untuk generate access token baru)
                  String accessToken = jwtService.generateAccessToken(userDetails);
                  String refreshTokenString = jwtService.generateRefreshToken(userDetails);

                  // STEP 6: Save refresh token ke database
                  // Disimpan supaya bisa di-revoke (logout, security breach, dll)
                  RefreshToken refreshToken = RefreshToken.builder()
                              .token(refreshTokenString)
                              .user(user)
                              .expiryDate(LocalDateTime.now().plusDays(7)) // 7 days
                              .build();
                  refreshTokenRepository.save(refreshToken);

                  // STEP 7: Build response
                  return AuthResponse.builder()
                              .accessToken(accessToken) // Token untuk akses API (header Authorization)
                              .refreshToken(refreshTokenString) // Token untuk generate access token baru
                              .type("Bearer") // Token type untuk header
                              .username(user.getUsername())
                              .roles(user.getRoles().stream()
                                          .map(Role::getRoleName)
                                          .collect(Collectors.toSet()))
                              .build();

            } catch (BadCredentialsException e) {
                  // Exception ini di-throw kalau username/password salah
                  throw new BusinessException("Username atau password salah");
            }
      }

      /**
       * LOAD USER BY USERNAME - Method dari interface UserDetailsService
       * 
       * Method ini dipanggil oleh Spring Security saat:
       * 1. Login - untuk validate password
       * 2. Setiap request - untuk validate JWT token
       * 
       * Convert entity User → UserDetails:
       * - User = entity JPA kita (database)
       * - UserDetails = interface Spring Security (standard format)
       * 
       * @param username Username/email user
       * @return UserDetails dengan info user & roles
       * @throws UsernameNotFoundException kalau user tidak ditemukan
       */
      @Override
      public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            // Load user dari database
            User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + username));

            // Convert entity User → Spring Security User (implements UserDetails)
            // org.springframework.security.core.userdetails.User = class dari Spring
            // Security
            // BUKAN entity User kita!
            return new org.springframework.security.core.userdetails.User(
                        user.getUsername(), // Username untuk authentication
                        user.getPassword(), // Password (BCrypt hash) untuk compare
                        user.getIsActive(), // enabled: apakah user aktif?
                        true, // accountNonExpired: akun tidak expired
                        true, // credentialsNonExpired: password tidak expired
                        true, // accountNonLocked: akun tidak di-lock
                        // authorities: roles user untuk authorization
                        // Map Role entity → GrantedAuthority dengan prefix "ROLE_"
                        // Contoh: "ADMIN" → "ROLE_ADMIN"
                        // Prefix "ROLE_" otomatis ditambah supaya bisa pakai hasRole('ADMIN')
                        user.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                                    .toList());
      }

      /**
       * REFRESH ACCESS TOKEN - Generate access token baru pakai refresh token
       * 
       * Flow:
       * 1. Validate refresh token (signature, expiration)
       * 2. Check refresh token di database
       * 3. Generate access token baru
       * 4. Return access token baru
       * 
       * Kenapa perlu refresh?
       * - Access token lifetime pendek (15 menit) untuk security
       * - Refresh token lifetime panjang (7 hari) untuk UX
       * - User tidak perlu login ulang setiap 15 menit
       * 
       * @param refreshTokenString Refresh token dari login response
       * @return AuthResponse dengan access token baru
       */
      @Transactional
      public AuthResponse refreshAccessToken(String refreshTokenString) {
            // STEP 1: Validate refresh token (signature & expiration)
            // Extract username dari token
            String username;
            try {
                  username = jwtService.extractUsername(refreshTokenString);
            } catch (Exception e) {
                  throw new BusinessException("Refresh token tidak valid");
            }

            // STEP 2: Load user dari database
            User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new BusinessException("User tidak ditemukan"));

            // STEP 3: Load UserDetails untuk validate token
            UserDetails userDetails = loadUserByUsername(username);

            // STEP 4: Validate token dengan user
            if (!jwtService.isTokenValid(refreshTokenString, userDetails)) {
                  throw new BusinessException("Refresh token tidak valid atau expired");
            }

            // STEP 5: Check refresh token di database
            // Refresh token harus ada di database dan belum expired
            RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                        .orElseThrow(() -> new BusinessException("Refresh token tidak ditemukan"));

            // STEP 6: Check expiration date & revocation status
            if (refreshToken.isExpired()) {
                  refreshTokenRepository.delete(refreshToken);
                  throw new BusinessException("Refresh token sudah expired, silakan login ulang");
            }

            if (refreshToken.isRevoked()) {
                  throw new BusinessException("Refresh token sudah tidak berlaku (revoked), silakan login ulang");
            }

            // STEP 7: Token Rotation - Generate access & refresh token BARU
            String newAccessToken = jwtService.generateAccessToken(userDetails);
            String newRefreshTokenString = jwtService.generateRefreshToken(userDetails);

            // STEP 8: Update refresh token di database (Revoke yang lama)
            // Audit Style: Mark yang lama sebagai revoked, simpan yang baru (atau update
            // row)
            // Di sini kita update row yang sama tapi mark revoked dulu sebagai histori
            // (opsional)
            // Untuk simplisitas saat ini kita update & reset revokedAt jika ada
            refreshToken.setToken(newRefreshTokenString);
            refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
            refreshToken.setRevokedAt(null); // Pastikan token baru tidak revoked
            refreshTokenRepository.save(refreshToken);

            // STEP 9: Return response dengan token baru
            return AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshTokenString)
                        .type("Bearer")
                        .username(user.getUsername())
                        .roles(user.getRoles().stream()
                                    .map(Role::getRoleName)
                                    .collect(Collectors.toSet()))
                        .build();
      }

      /**
       * LOGOUT - Mematikan session user
       * 
       * Flow:
       * 1. Blacklist access token di Redis (stateless)
       * 2. Hapus refresh token di database (stateful)
       * 
       * @param accessToken        Access token dari header (setelah dipotong 'Bearer
       *                           ')
       * @param refreshTokenString Refresh token dari body
       */
      @Transactional
      public void logout(String accessToken, String refreshTokenString) {
            // 1. Blacklist Access Token di Redis (stateless)
            jwtService.blacklistToken(accessToken);

            // 2. Revoke Refresh Token di Database (Audit Style)
            refreshTokenRepository.findByToken(refreshTokenString)
                        .ifPresent(token -> {
                              token.revoke();
                              refreshTokenRepository.save(token);
                        });
      }
      /**
       * LUPA KATA SANDI - Generate token reset password
       */
      @Transactional
      public void forgotPassword(String email) {
          User user = userRepository.findByEmail(email)
                  .orElseThrow(() -> new ResourceNotFoundException("User dengan email " + email + " tidak ditemukan"));

          // Tandai token lama yang belum terpakai sebagai terpakai (jika ada)
          passwordResetTokenRepository.findByUserAndIsUsedFalse(user)
              .ifPresent(token -> {
                  token.setIsUsed(true);
                  passwordResetTokenRepository.save(token);
              });

          String token = UUID.randomUUID().toString();
          PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                  .token(token)
                  .user(user)
                  .isUsed(false)
                  .expiratedAt(LocalDateTime.now().plusMinutes(5)) // kadaluarsa 5 menit
                  // createdAt ditangani oleh @PrePersist
                  .build();
          
          passwordResetTokenRepository.save(passwordResetToken);

          // Kirim email
          String resetUrl = frontendUrl + "/reset-password?token=" + token; // URL Frontend
          emailService.sendSimpleMessage(email, "Permintaan Reset Kata Sandi", "Untuk mereset kata sandi Anda, klik tautan di bawah ini:\n" + resetUrl);
      }

      /**
       * RESET KATA SANDI - Ganti password dengan token valid
       */
      @Transactional
      public void resetPassword(String token, String newPassword) {
          PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                  .orElseThrow(() -> new ResourceNotFoundException("Token tidak valid atau sudah kadaluarsa"));

          if (resetToken.getIsUsed()) {
                  throw new BusinessException("Token sudah pernah digunakan");
          }

          if (LocalDateTime.now().isAfter(resetToken.getExpiratedAt())) {
                  throw new BusinessException("Token telah kadaluarsa");
          }

          User user = resetToken.getUser();
          user.setPassword(passwordEncoder.encode(newPassword));
          userRepository.save(user);

          // Tandai token sebagai sudah terpakai
          resetToken.setIsUsed(true);
          passwordResetTokenRepository.save(resetToken);
      }
}

package com.example.loanova.config;

import com.example.loanova.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT AUTHENTICATION FILTER
 *
 * <p>Filter ini DIJALANKAN DI SETIAP REQUEST sebelum masuk ke controller
 *
 * <p>Fungsi filter ini: 1. Extract JWT token dari header "Authorization: Bearer <token>" 2.
 * Validate JWT token (signature, expiration, dll) 3. Kalau valid: Set Authentication di
 * SecurityContext 4. Kalau invalid/tidak ada: Request tetap lanjut (Spring Security yang block
 * kalau endpoint protected)
 *
 * <p>OncePerRequestFilter = Filter ini dijamin cuma jalan 1x per request
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  // Service untuk generate & validate JWT
  private final JwtService jwtService;

  // Service untuk load user dari database
  // Pakai interface UserDetailsService, bukan class AuthService
  // Supaya tidak circular dependency
  private final UserDetailsService userDetailsService;

  /**
   * METHOD UTAMA FILTER - Dipanggil otomatis setiap ada request
   *
   * <p>Flow: 1. Extract token dari header 2. Validate token (signature, expiration) 3. Set
   * authentication kalau valid 4. Lanjut ke filter berikutnya / controller
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // STEP 1: Ambil header Authorization
    // Format: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    // STEP 2: Check apakah header Authorization ada dan formatnya benar
    // Kalau tidak ada / format salah → skip filter ini, lanjut ke next filter
    // Spring Security akan block kalau endpoint nya protected
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // STEP 3: Extract JWT token (hapus "Bearer " prefix)
    // "Bearer eyJhbGc..." → "eyJhbGc..."
    jwt = authHeader.substring(7);

    // STEP 3.1: Check apakah token di-blacklist (Logout)
    // Kalau sudah logout, token tidak boleh dipakai lagi
    if (jwtService.isTokenBlacklisted(jwt)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // STEP 4: Extract username dari JWT
      // JWT di-decode, ambil claim "subject" (username/email)
      username = jwtService.extractUsername(jwt);

      // STEP 5: Check apakah username valid dan user belum di-authenticate
      // SecurityContext.getAuthentication() == null artinya user belum login di
      // request ini
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // STEP 6: Load user dari database
        // userDetailsService akan call authService.loadUserByUsername()
        // Return UserDetails dengan username, password, roles
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // STEP 7: Validate JWT token
        // Check: username di token = username di DB?
        // Check: token sudah expired atau belum?
        if (jwtService.isTokenValid(jwt, userDetails)) {

          // STEP 8: Create Authentication object
          // UsernamePasswordAuthenticationToken adalah implementation dari Authentication
          // Parameters:
          // - userDetails: info user (username, roles)
          // - null: credentials (tidak perlu, sudah validated via JWT)
          // - authorities: roles user (ROLE_ADMIN, ROLE_USER, dll)
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities() // Roles untuk authorization
                  );

          // Set additional details (IP address, session ID, dll)
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // STEP 9: Set authentication di SecurityContext
          // Setelah ini, Spring Security tahu user sudah login
          // @PreAuthorize bisa check roles dari getAuthorities()
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      // Kalau ada error (invalid token, user not found, dll)
      // Log error tapi tidak throw exception
      // Biar request tetap lanjut, Spring Security yang handle authorization
      logger.error("Cannot set user authentication: {}", e);
    }

    // STEP 10: Lanjutkan ke filter berikutnya / controller
    // Kalau authentication berhasil di-set, user bisa akses protected endpoints
    // Kalau gagal, Spring Security akan return 401/403
    filterChain.doFilter(request, response);
  }
}

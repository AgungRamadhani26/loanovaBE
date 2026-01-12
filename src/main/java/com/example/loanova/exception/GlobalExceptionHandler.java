package com.example.loanova.exception;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.util.ResponseUtil;
import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@RestControllerAdvice
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * GLOBAL BINDER
   * Gunanya: Jika ada field MultipartFile yang dikirim sebagai String kosong "" 
   * (alias "kerangka input" tapi gak ada filenya), maka otomatis diubah jadi NULL.
   * Ini MENCEGAH Error 500 "Failed to parse multipart/convert".
   */
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
      @Override
      public void setAsText(String text) {
        if (text == null || text.trim().isEmpty()) {
          setValue(null);
        }
      }
    });
  }

  /* Untuk exception jika ukuran file melebihi batas */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse<Object>> handleMaxUploadSize(
      MaxUploadSizeExceededException ex) {
    return ResponseUtil.error(
        HttpStatus.BAD_REQUEST, "Ukuran file terlalu besar. Maksimal 3MB per file.");
  }

  /* Untuk exception jika data tidak ditemukan 404 */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
    return ResponseUtil.error(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  /* Untuk exception jika ada data yang duplikat 409 */
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiResponse<Object>> handleDuplicate(DuplicateResourceException ex) {
    return ResponseUtil.error(HttpStatus.CONFLICT, ex.getMessage());
  }

  /* Untuk exception validasi input 400 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidationError(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return ResponseUtil.error(HttpStatus.BAD_REQUEST, "Validasi gagal", Map.of("errors", errors));
  }

  /**
   * Handle BindException untuk validasi form-data (multipart/form-data) Terjadi
   * saat menggunakan
   * 
   * @ModelAttribute dengan @Valid pada form-data request
   */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Object>> handleBindException(BindException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return ResponseUtil.error(HttpStatus.BAD_REQUEST, "Validasi gagal", Map.of("errors", errors));
  }

  /* Untuk exception jika ada kesalahan bisnis / validasi 400 */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Object>> handleBusiness(BusinessException ex) {
    if (ex.getErrors() != null && !ex.getErrors().isEmpty()) {
      return ResponseUtil.error(
          HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of("errors", ex.getErrors()));
    }
    return ResponseUtil.error(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  /**
   * Handle Access Denied - 403 Forbidden Ketika user tidak punya akses ke
   * endpoint (role tidak
   * sesuai) Contoh: MARKETING coba akses endpoint SUPERADMIN
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
    return ResponseUtil.error(
        HttpStatus.FORBIDDEN, "Anda tidak memiliki akses untuk mengakses resource ini");
  }

  /**
   * Handle Authentication - 401 Unauthorized Ketika user belum login / token
   * invalid
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Object>> handleAuthentication(AuthenticationException ex) {
    return ResponseUtil.error(
        HttpStatus.UNAUTHORIZED, "Autentikasi gagal. Silakan login terlebih dahulu");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
    ex.printStackTrace(); // DEBUGGING: Print error stack trace to console
    // Return error message untuk debugging (di production sebaiknya generic message
    // saja)
    String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Terjadi kesalahan pada server";
    return ResponseUtil.error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Terjadi kesalahan pada server: " + errorMessage);
  }
}

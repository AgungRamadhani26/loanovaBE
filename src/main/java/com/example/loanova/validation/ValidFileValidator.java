package com.example.loanova.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validator untuk annotation @ValidFile. Memvalidasi MultipartFile berdasarkan kriteria yang
 * ditentukan.
 */
public class ValidFileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

  private boolean required;
  private long maxSize;
  private String[] allowedTypes;

  @Override
  public void initialize(ValidFile constraintAnnotation) {
    this.required = constraintAnnotation.required();
    this.maxSize = constraintAnnotation.maxSize();
    this.allowedTypes = constraintAnnotation.allowedTypes();
  }

  @Override
  public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
    // Jika file tidak required dan null/empty, maka valid
    if (!required && (file == null || file.isEmpty())) {
      return true;
    }

    // Jika file required tetapi null atau empty, maka invalid
    if (required && (file == null || file.isEmpty())) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("File wajib diunggah").addConstraintViolation();
      return false;
    }

    // Validasi ukuran file
    if (file.getSize() > maxSize) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Ukuran file terlalu besar. Maksimal " + (maxSize / (1024 * 1024)) + "MB")
          .addConstraintViolation();
      return false;
    }

    // Validasi tipe file
    String contentType = file.getContentType();
    if (contentType == null) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Tipe file tidak valid")
          .addConstraintViolation();
      return false;
    }

    boolean isValidType = false;
    for (String allowedType : allowedTypes) {
      if (allowedType.endsWith("/*")) {
        // Wildcard matching (contoh: image/*)
        String prefix = allowedType.substring(0, allowedType.length() - 1);
        if (contentType.startsWith(prefix)) {
          isValidType = true;
          break;
        }
      } else if (contentType.equals(allowedType)) {
        isValidType = true;
        break;
      }
    }

    if (!isValidType) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("File harus berupa gambar (JPG/PNG)")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}

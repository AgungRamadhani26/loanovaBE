package com.example.loanova.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation untuk validasi MultipartFile.
 * Memvalidasi bahwa file tidak null dan tidak kosong.
 */
@Documented
@Constraint(validatedBy = ValidFileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {
    String message() default "File wajib diunggah";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Apakah file wajib diisi (required).
     * Default: true
     */
    boolean required() default true;
    
    /**
     * Maksimal ukuran file dalam bytes.
     * Default: 3MB (3 * 1024 * 1024 bytes)
     */
    long maxSize() default 3 * 1024 * 1024;
    
    /**
     * Content type yang diizinkan.
     * Default: semua jenis gambar (image/*)
     */
    String[] allowedTypes() default {"image/*"};
}

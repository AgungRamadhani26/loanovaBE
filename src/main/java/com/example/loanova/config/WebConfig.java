package com.example.loanova.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WEB CONFIGURATION - Konfigurasi untuk mengakses file static (uploaded files). Mengatur mapping
 * URL /uploads/** ke direktori fisik tempat file disimpan.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${file.upload-dir}")
  private String uploadDir;

  /**
   * Menambahkan resource handler untuk mengakses file yang diupload. URL Pattern:
   * http://localhost:9091/uploads/ktp/uuid.jpg File Location: uploads/ktp/uuid.jpg
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadDir + "/");
  }

  /** Konfigurasi Global CORS untuk mengizinkan frontend mengakses API */
  @Override
  public void addCorsMappings(
      org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
    registry
        .addMapping("/**") // Berlaku semua endpoint
        .allowedOrigins(
            "http://localhost:4200", // Frontend Local Development
            "http://10.55.44.44:4200", // Frontend Local via IP (untuk HP/Android)
            "https://loanova-fe.vercel.app" // Frontend Production
            )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Method yang diizinkan
        .allowedHeaders("*") // Header yang diizinkan
        .allowCredentials(true) // Izinkan cookies/auth headers
        .maxAge(3600); // Cache preflight request selama 1 jam
  }
}

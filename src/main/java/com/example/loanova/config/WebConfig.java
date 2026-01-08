package com.example.loanova.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WEB CONFIGURATION - Konfigurasi untuk mengakses file static (uploaded files).
 * Mengatur mapping URL /uploads/** ke direktori fisik tempat file disimpan.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Menambahkan resource handler untuk mengakses file yang diupload.
     * URL Pattern: http://localhost:9091/uploads/ktp/uuid.jpg
     * File Location: uploads/ktp/uuid.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}

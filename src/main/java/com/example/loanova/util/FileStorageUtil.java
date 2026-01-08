package com.example.loanova.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * FILE STORAGE UTIL - Utilitas untuk mengelola penyimpanan file di server.
 * Digunakan untuk menyimpan foto profil, KTP, dan NPWP dengan nama acak.
 */
@Component
public class FileStorageUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Menyimpan file ke direktori lokal dengan nama acak (UUID).
     * 
     * @param file MultiPartFile yang diunggah
     * @param subDir Sub-direktori di dalam uploads (misal: "profiles", "ktp")
     * @return Path relatif file yang disimpan (format: subDir/fileName)
     * @throws IOException Jika terjadi kesalahan saat menyimpan file
     */
    public String saveFile(MultipartFile file, String subDir) throws IOException {
        // Path lengkap ke direktori tujuan
        Path directoryPath = Paths.get(uploadDir, subDir);
        
        // Buat direktori jika belum ada
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Generate nama file acak untuk keamanan
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // Path tujuan file
        Path filePath = directoryPath.resolve(fileName);

        // Salin file ke storage
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return path relatif: subDir/fileName (contoh: "ktp/uuid.jpg")
        return subDir + "/" + fileName;
    }

    /**
     * Menghapus file dari storage.
     * 
     * @param filePath Path relatif file (contoh: "ktp/uuid.jpg")
     * @return true jika berhasil dihapus, false jika file tidak ada
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            Path fullPath = Paths.get(uploadDir, filePath);
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            // Log error tapi tidak throw exception
            System.err.println("Gagal menghapus file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }
}

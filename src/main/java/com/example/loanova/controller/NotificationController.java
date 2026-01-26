package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.response.NotificationResponse;
import com.example.loanova.service.NotificationService;
import com.example.loanova.util.ResponseUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final com.example.loanova.repository.UserRepository userRepository;

    /**
     * Get list notifikasi user yang sedang login
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(Authentication authentication) {
        String username = authentication.getName();
        List<NotificationResponse> notifications = notificationService.getUserNotifications(username);
        return ResponseUtil.ok(notifications, "Berhasil mengambil data notifikasi");
    }

    /**
     * Tandai spesifik notifikasi sebagai sudah dibaca
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            Authentication authentication,
            @PathVariable Long id) {
        String username = authentication.getName();
        notificationService.markAsRead(username, id);
        return ResponseUtil.ok(null, "Notifikasi ditandai sudah dibaca");
    }

    /**
     * Tandai SEMUA notifikasi sebagai sudah dibaca
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        notificationService.markAllAsRead(username);
        return ResponseUtil.ok(null, "Semua notifikasi ditandai sudah dibaca");
    }

    /**
     * TEST: Kirim Push Notification Manual (Admin Only)
     */
    @PostMapping("/test-push")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<String>> testPushNotification(
            @org.springframework.web.bind.annotation.RequestBody com.example.loanova.dto.request.PushNotificationRequest request) {
        
        com.example.loanova.entity.User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new com.example.loanova.exception.ResourceNotFoundException("User tidak ditemukan"));

        notificationService.createNotification(user, request.getTitle(), request.getMessage());
        
        return ResponseUtil.ok("Notifikasi berhasil dikirim ke user: " + request.getUsername(), "Success");
    }
}

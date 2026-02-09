package com.example.loanova.service;

import com.example.loanova.dto.response.NotificationResponse;
import com.example.loanova.entity.Notification;
import com.example.loanova.entity.User;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.NotificationRepository;
import com.example.loanova.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final FcmService fcmService;

  /** Membuat notifikasi baru untuk user tertentu (tanpa loan application ID) */
  @Transactional
  public void createNotification(User user, String title, String message) {
    createNotification(user, title, message, null);
  }

  /** Membuat notifikasi baru untuk user tertentu dengan loan application ID */
  @Transactional
  public void createNotification(User user, String title, String message, Long loanApplicationId) {
    Notification notification =
        Notification.builder()
            .user(user)
            .title(title)
            .message(message)
            .loanApplicationId(loanApplicationId)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();

    notificationRepository.save(notification);

    // Send Push Notification if user has FCM token
    if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
      fcmService.sendNotification(user.getFcmToken(), title, message, loanApplicationId);
    }
  }

  /** Mengambil daftar notifikasi milik user yang login */
  @Transactional(readOnly = true)
  public List<NotificationResponse> getUserNotifications(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

    return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  /** Menandai notifikasi sebagai sudah dibaca */
  @Transactional
  public void markAsRead(String username, Long notificationId) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notifikasi tidak ditemukan"));

    // Validasi kepemilikan notifikasi
    if (!notification.getUser().getId().equals(user.getId())) {
      throw new ResourceNotFoundException("Notifikasi tidak ditemukan atau bukan milik Anda");
    }

    notification.setIsRead(true);
    notificationRepository.save(notification);
  }

  /** Menandai semua notifikasi sebagai sudah dibaca */
  @Transactional
  public void markAllAsRead(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

    List<Notification> unreadNotifications =
        notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

    unreadNotifications.forEach(notification -> notification.setIsRead(true));

    notificationRepository.saveAll(unreadNotifications);
  }

  private NotificationResponse toResponse(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .title(notification.getTitle())
        .message(notification.getMessage())
        .loanApplicationId(notification.getLoanApplicationId())
        .isRead(notification.getIsRead())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}

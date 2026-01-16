package com.example.loanova.repository;

import com.example.loanova.entity.Notification;
import com.example.loanova.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a specific user, ordered by creation time descending
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Find unread notifications for a user
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
}

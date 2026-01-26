package com.example.loanova.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {

  public void sendNotification(String token, String title, String body) {
    if (token == null || token.isEmpty()) {
      log.warn("Cannot send notification: FCM token is null or empty");
      return;
    }

    try {
      Notification notification = Notification.builder()
          .setTitle(title)
          .setBody(body)
          .build();

      Message message = Message.builder()
          .setToken(token)
          .setNotification(notification)
          .build();

      String response = FirebaseMessaging.getInstance().send(message);
      log.info("Successfully sent message: {}", response);
    } catch (Exception e) {
      log.error("Failed to send FCM message. Title: {}, Token: {}, Error: {}", title, token, e.getMessage());
      e.printStackTrace(); // Print full stack trace to console
    }
  }
}

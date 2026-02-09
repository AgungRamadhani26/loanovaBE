package com.example.loanova.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Slf4j
public class FirebaseConfig {

  @PostConstruct
  public void initialize() {
    try {
      ClassPathResource serviceAccount = new ClassPathResource("firebase-service-account.json");
      if (!serviceAccount.exists()) {
        log.warn("Firebase service account file not found. Push notifications will not work.");
        return;
      }

      InputStream serviceAccountStream = serviceAccount.getInputStream();

      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
              .build();

      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options);
        log.info("Firebase application has been initialized");
      }
    } catch (IOException e) {
      log.error("Failed to initialize Firebase: {}", e.getMessage());
    }
  }
}

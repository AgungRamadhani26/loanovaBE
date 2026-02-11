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
      InputStream serviceAccountStream = null;

      // 1. Cek Environment Variable (untuk Production / Docker)
      String externalPath = System.getenv("FIREBASE_CONFIG_PATH");
      if (externalPath != null && !externalPath.isEmpty()) {
        java.io.File file = new java.io.File(externalPath);
        if (file.exists()) {
          log.info("Loading Firebase config from external file: {}", externalPath);
          serviceAccountStream = new java.io.FileInputStream(file);
        } else {
          log.warn("FIREBASE_CONFIG_PATH is set to {}, but file not found.", externalPath);
        }
      }

      // 2. Fallback ke Classpath (untuk Local Dev)
      if (serviceAccountStream == null) {
        log.info("Loading Firebase config from classpath");
        ClassPathResource serviceAccount = new ClassPathResource("firebase-service-account.json");
        if (serviceAccount.exists()) {
          serviceAccountStream = serviceAccount.getInputStream();
        }
      }

      if (serviceAccountStream == null) {
        log.warn(
            "Firebase service account file not found in ENV or Classpath. Push notifications will not work.");
        return;
      }

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

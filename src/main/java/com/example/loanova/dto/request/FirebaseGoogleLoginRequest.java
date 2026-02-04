package com.example.loanova.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO untuk login menggunakan Google via Firebase Authentication.
 * 
 * Flow:
 * 1. Android app melakukan Google Sign-In via Firebase
 * 2. Firebase return ID Token
 * 3. Android kirim ID Token ke backend via endpoint ini
 * 4. Backend verify ID Token dengan Firebase Admin SDK
 * 5. Backend create/link user dan return JWT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseGoogleLoginRequest {

   /**
    * Firebase ID Token yang didapat setelah Google Sign-In di Android.
    * Token ini akan diverifikasi oleh backend menggunakan Firebase Admin SDK.
    */
   @NotBlank(message = "Firebase ID Token tidak boleh kosong")
   private String idToken;

   /**
    * FCM Token untuk push notification (optional).
    * Sama seperti login biasa, token ini disimpan untuk kirim notifikasi.
    */
   private String fcmToken;
}
